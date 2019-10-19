/*
 * Copyright (c) 2019 5zig Reborn
 *
 * This file is part of 5zig-fabric
 * 5zig-fabric is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * 5zig-fabric is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with 5zig-fabric.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.the5zig.fabric.util;

import net.fabricmc.tinyremapper.TinyRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MethodUtils {
    private static Method mapClass;
    private static Field fieldMap, methodMap;

    public static String cachedMixinName;

    public static void init() throws Exception {
        mapClass = TinyRemapper.class.getDeclaredMethod("mapClass", String.class);
        mapClass.setAccessible(true);

        fieldMap = TinyRemapper.class.getDeclaredField("fieldMap");
        fieldMap.setAccessible(true);

        methodMap = TinyRemapper.class.getDeclaredField("methodMap");
        methodMap.setAccessible(true);
    }

    public static String remapMethod(TinyRemapper remapper, String desc) {
        String[] splitSemi = desc.split(";", 2);
        String className = splitSemi[0].substring(1);
        String methodInfo = splitSemi[1];
        String[] splitPar = methodInfo.split("\\(");
        String methodName = splitPar[0];
        String[] splitRight = splitPar[1].split("\\)");
        String returnType = splitRight[1];
        String args = splitRight[0];
        args = parseArgs(remapper, args);
        returnType = parseArgs(remapper, returnType);
        String newMethod = mapMethod(remapper, className, methodInfo);
        String newClass = mapClass(remapper, className);
        if(newMethod == null) {
            if(ForcedMappings.mappings.has(desc)) {
                String mapping = ForcedMappings.mappings.get(desc).getAsString();
                String[] data = mapping.split("/");
                newMethod = data[1];
                newClass = "net/minecraft/" + data[0];
            }
            else {
                newMethod = methodName;
            }
        }

        return "L" + newClass + ";" + newMethod + "(" + args + ")" + returnType;
    }

    public static String parseArgs(TinyRemapper remapper, String args) {
        Pattern regex = Pattern.compile("L[a-z/_0-9A-Z]+;");
        Matcher matcher = regex.matcher(args);
        while(matcher.find()) {
            String res = matcher.group();
            if(res.contains("/") && !res.contains("net/minecraft")) continue; // ex. java/lang/String
            String name = res.substring(1, res.length() - 1);
            args = args.replace(res, "L" + mapClass(remapper, name) + ";");
        }
        return args;
    }

    public static String remapField(TinyRemapper remapper, String desc, String containerClass, File file) {
        String[] splitSemi = desc.split(";", 2);
        String className = null;
        String[] splitCol = null;
        if(splitSemi[splitSemi.length - 1].isEmpty()) {
            try {
                getAnnotatedMixinName(file, containerClass);
                className = cachedMixinName.substring(1, cachedMixinName.length() - 1);
                cachedMixinName = null;
                splitCol = splitSemi[0].split(":");
                if(!splitCol[1].endsWith(";")) splitCol[1] = splitCol[1] + ";";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            className = splitSemi[0].substring(1);
            splitCol = splitSemi[1].split(":");
        }

        String fieldName = splitCol[0];
        String returnType = splitCol[1];

        String newField = mapField(remapper, className, fieldName, returnType);
        if(newField == null) newField = fieldName;

        returnType = parseArgs(remapper, returnType);
        if(returnType.startsWith("[") && returnType.endsWith(";"))
            returnType = returnType.substring(0, returnType.length() - 1);

        return "L" + mapClass(remapper, className) + ";" + newField + ":" + returnType;
    }

    public static void getAnnotatedMixinName(File file, String className) throws IOException {
        JarFile jar = new JarFile(file);
        JarEntry entry = jar.getJarEntry(className + ".class");

        InputStream classFileInputStream = jar.getInputStream(entry);
        try {
            ClassReader classReader = new ClassReader(classFileInputStream);
            classReader.accept(new AnnotationVisitor(), 0);
        } finally {
            classFileInputStream.close();
        }
        jar.close();
    }

    public static void getAnnotatedMixinName(ZipFile jar, ZipEntry entry) throws IOException {
        InputStream classFileInputStream = jar.getInputStream(entry);
        try {
            ClassReader classReader = new ClassReader(classFileInputStream);
            classReader.accept(new AnnotationVisitor(), 0);
        } finally {
            classFileInputStream.close();
        }
    }

    public static String mapClass(TinyRemapper remapper, String old) {
        try {
            return (String) mapClass.invoke(remapper, old);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String mapField(TinyRemapper remapper, String className, String name, String returnType) {
        String id = className + "/" + name + ";;" + returnType;
        try {
            Map<String, String> map = (Map<String, String>) fieldMap.get(remapper);
            return map.get(id);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String mapMethod(TinyRemapper remapper, String className, String desc) {
        String id = className + "/" + desc;
        try {
            Map<String, String> map = (Map<String, String>) methodMap.get(remapper);
            return map.get(id);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class AnnotationVisitor extends ClassVisitor {

        public AnnotationVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public org.objectweb.asm.AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            return new AnnotationInfoVisitor();
        }
    }

    private static class AnnotationInfoVisitor extends org.objectweb.asm.AnnotationVisitor {

        public AnnotationInfoVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visit(String name, Object value) {
            cachedMixinName = value.toString();
            super.visit(name, value);
        }

        @Override
        public org.objectweb.asm.AnnotationVisitor visitArray(String name) {
            return new AnnotationInfoVisitor();
        }
    }
}

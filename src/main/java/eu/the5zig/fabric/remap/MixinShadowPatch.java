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

package eu.the5zig.fabric.remap;

import eu.the5zig.fabric.util.FileLocator;
import eu.the5zig.fabric.util.MethodUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MixinShadowPatch {
    private static List<String> fieldsWithShadow = new ArrayList<>();
    private static List<String> methodsWithOverwrite = new ArrayList<>();

    public static void patchMixins(File source) throws IOException {
        ZipFile file = new ZipFile(source);
        URI uri = URI.create("jar:file:" + FileLocator.getAbsolutePath(source));
        Enumeration<? extends ZipEntry> elems = file.entries();
        while (elems.hasMoreElements()) {
            ZipEntry entry = elems.nextElement();
            if (entry.getName().startsWith("eu/the5zig/mod/mixin/Mixin")) {
                MethodUtils.getAnnotatedMixinName(file, entry);
                try (FileSystem zipfs = FileSystems.newFileSystem(uri, new HashMap<>())) {
                    Path pathInZipfile = zipfs.getPath(entry.getName());
                    Files.write(pathInZipfile, visitMixinClass(file, entry, entry.getName()), StandardOpenOption.TRUNCATE_EXISTING);
                }
            }
        }
    }

    public static byte[] visitMixinClass(ZipFile jar, ZipEntry entry, String className) throws IOException {

        InputStream classFileInputStream = jar.getInputStream(entry);
        try {
            ClassReader classReader = new ClassReader(classFileInputStream);
            classReader.accept(new ShadowClassVisitor(), 0);

            ClassWriter writer = new ClassWriter(0);
            ClassRemapper mapper = new ClassRemapper(writer, new Remapper() {
                @Override
                public String mapFieldName(String owner, String name, String descriptor) {
                    if (fieldsWithShadow.contains(name)) {
                        String newOwner = MethodUtils.cachedMixinName.substring(1, MethodUtils.cachedMixinName.length() - 1);
                        newOwner = MethodUtils.mapClass(MixinRemapper.inverse, newOwner);
                        descriptor = MethodUtils.parseArgs(MixinRemapper.inverse, descriptor);
                        return MethodUtils.mapField(MixinRemapper.remapper, newOwner, name, descriptor);
                    }
                    return super.mapFieldName(owner, name, descriptor);
                }

                @Override
                public String mapMethodName(String owner, String name, String descriptor) {
                    if (methodsWithOverwrite.contains(name)) {
                        String newOwner = MethodUtils.cachedMixinName.substring(1, MethodUtils.cachedMixinName.length() - 1);
                        newOwner = MethodUtils.mapClass(MixinRemapper.inverse, newOwner);
                        descriptor = MethodUtils.parseArgs(MixinRemapper.inverse, descriptor);
                        return MethodUtils.mapMethod(MixinRemapper.remapper, newOwner, name + descriptor);
                    }
                    return super.mapMethodName(owner, name, descriptor);
                }
            });
            classReader.accept(mapper, ClassReader.EXPAND_FRAMES);

            writer.visitEnd();
            return writer.toByteArray();
        } finally {
            classFileInputStream.close();
            fieldsWithShadow.clear();
            methodsWithOverwrite.clear();
        }
    }

    private static class ShadowClassVisitor extends ClassVisitor {

        public ShadowClassVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            return new ShadowFieldVisitor(name);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            return new ShadowMethodVisitor(name);
        }
    }

    private static class ShadowFieldVisitor extends FieldVisitor {

        public ShadowFieldVisitor(String name) {
            super(Opcodes.ASM5);
            this.name = name;
        }

        private String name;

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (descriptor.contains("org/spongepowered/asm/mixin/Shadow")) {
                fieldsWithShadow.add(name);
            }
            return super.visitAnnotation(descriptor, visible);
        }
    }

    private static class ShadowMethodVisitor extends MethodVisitor {
        public ShadowMethodVisitor(String name) {
            super(Opcodes.ASM5);
            this.name = name;
        }

        private String name;

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (descriptor.contains("org/spongepowered/asm/mixin/Overwrite")) {
                methodsWithOverwrite.add(name);
            }
            return super.visitAnnotation(descriptor, visible);
        }
    }
}

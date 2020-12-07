/*
 * Copyright (c) 2019-2020 5zig Reborn
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

package eu.the5zig.fabric.boot;

import eu.the5zig.fabric.TransformerMain;
import eu.the5zig.fabric.remap.MixinRemapper;
import eu.the5zig.fabric.remap.MixinShadowPatch;
import eu.the5zig.fabric.remap.RemapCache;
import eu.the5zig.fabric.remap.RemapperUtils;
import eu.the5zig.fabric.util.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FabricTransformer implements Runnable {
    private final String mcVersion;
    private final String[] args;

    public FabricTransformer(String[] args, String mcVersion) {
        this.args = args;
        this.mcVersion = mcVersion;
    }

    /**
     * This is called every time the game boots.
     * 5zig is, when compiled, reobfuscated to Minecraft's obfuscated mappings.
     * As such, we need to act as a runtime intermediary between 5zig and Fabric.
     */
    @Override
    public void run() {
        FileSystem jarFs = null;
        System.out.println("abc");
        System.out.println("Preparing environment...");
        File gameDir = new File("").getAbsoluteFile();
        RemapCache.init(gameDir);

        System.out.println("Looking for 5zig installations...");
        try {
            MethodUtils.init();
            ForcedMappings.loadMappings(mcVersion);

            ModFile mod = FileLocator.getModJar(gameDir);
            if (mod == null) {
                System.out.println("No 5zig installations found. Done!");
                return;
            }
            String jarName = mod.getFile().getName().replace(".jar", "");
            File newJar = new File(mod.getFile().getParentFile(), jarName + "-Fabric.jar");

            String namespace = TransformerMain.mappings.getTargetNamespace();

            System.out.println("Remapping mixins...");
            MixinRemapper mixin = MixinRemapper.fromFile(mod.getFile());
            mixin.setNewFile(newJar);
            mixin.remap();

            System.out.println("Remapping from 'official' to '" + namespace + "'");
            RemapperUtils.remap(newJar.toPath(), mod.getFile().toPath(),
                    RemapperUtils.getMappings("official", namespace),
                    FileLocator.getLibs(args));

            jarFs = FileSystems.newFileSystem(newJar.toPath(), null);

            mixin.write(jarFs);
            MixinShadowPatch.patchMixins(newJar, jarFs);

            removeMixinLib(jarFs);
            ModManifest.injectManifest(mod.getVersion(), jarFs);
            System.out.println("Remap done.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't apply transformations", e);
        } finally {
            if (jarFs != null) {
                try {
                    jarFs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Transform done.");
    }

    private static void removeMixinLib(FileSystem zipfs) throws IOException {
        Path pathInZipfile = zipfs.getPath("org/spongepowered");
        Files.walkFileTree(pathInZipfile, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
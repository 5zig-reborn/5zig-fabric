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

package eu.the5zig.fabric.boot;

import eu.the5zig.fabric.FabricMod;
import eu.the5zig.fabric.remap.MixinRemapper;
import eu.the5zig.fabric.remap.MixinShadowPatch;
import eu.the5zig.fabric.remap.RemapCache;
import eu.the5zig.fabric.remap.RemapperUtils;
import eu.the5zig.fabric.util.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

public class FabricTransformer implements Runnable {

    /**
     * This is called every time the game boots.
     * 5zig is, when compiled, reobfuscated to Minecraft's obfuscated mappings.
     * As such, we need to act as a runtime intermediary between 5zig and Fabric.
     */
    @Override
    public void run() {
        FabricMod.LOGGER.info("Preparing environment...");
        File gameDir = FabricLoader.getInstance().getGameDirectory();
        RemapCache.init(gameDir);

        FabricMod.LOGGER.info("Looking for 5zig installations...");
        try {
            MethodUtils.init();
            ForcedMappings.loadMappings();

            ModFile mod = FileLocator.getModJar(gameDir);
            if (mod == null) {
                FabricMod.LOGGER.info("No 5zig installations found. Done!");
                return;
            }
            String jarName = mod.getFile().getName().replace(".jar", "");
            File newJar = new File(mod.getFile().getParentFile(), jarName + "-Fabric.jar");

            String namespace = FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace();

            FabricMod.LOGGER.info("Remapping mixins...");
            MixinRemapper mixin = MixinRemapper.fromFile(mod.getFile());
            mixin.setNewFile(newJar);
            mixin.remap();

            FabricMod.LOGGER.info("Remapping from 'official' to '" + namespace + "'");
            RemapperUtils.remap(newJar.toPath(), mod.getFile().toPath(), RemapperUtils.getMappings("official", namespace),
                    FileLocator.getLibs());

            MixinShadowPatch.patchMixins(newJar);
            mixin.write();

            removeMixinLib(newJar);
            ModManifest.injectManifest(mod.getVersion(), newJar);
            // mod.getFile().deleteOnExit();
            RemapCache.sealJar(newJar);

        } catch (Exception e) {
            throw new RuntimeException("Couldn't apply transformations", e);
        }
    }

    private static void removeMixinLib(File file) throws IOException {
        URI uri = URI.create("jar:file:" + file.getAbsolutePath());

        try (FileSystem zipfs = FileSystems.newFileSystem(uri, new HashMap<>())) {
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
}
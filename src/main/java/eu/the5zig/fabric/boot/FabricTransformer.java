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
import eu.the5zig.fabric.util.FileLocator;
import eu.the5zig.fabric.util.MethodUtils;
import eu.the5zig.fabric.util.ModFile;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.SecureClassLoader;

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

            ModFile mod = FileLocator.getModJar(gameDir);
            File target;
            if(mod == null) {
                FabricMod.LOGGER.info("No 5zig installations found. Done!");
                return;
            }
            if(RemapCache.checkSeal(mod.getFile())) {
                target = mod.getFile();
                FabricMod.LOGGER.info("Found already patched JAR.");
            }
            else {
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

                target = newJar;
               // mod.getFile().deleteOnExit();
                RemapCache.sealJar(newJar);
            }
            finishBoot(target);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't apply transformations", e);
        }
    }

    /**
     * Should be called after all configuration is done.
     * This loads The 5zig Mod.
     */
    public static void finishBoot(File jarFile) {
        try {
            Method addUrl = FabricTransformer.class.getClassLoader().getClass().getDeclaredMethod("addURL", URL.class);
            addUrl.setAccessible(true);
            addUrl.invoke(FabricTransformer.class.getClassLoader(), jarFile.toURL());
            Class handle = FabricTransformer.class.getClassLoader().loadClass("eu.the5zig.mod.asm.FabricHandle");
            handle.getMethod("run").invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("Fabric handle error", e);
        }
    }
}
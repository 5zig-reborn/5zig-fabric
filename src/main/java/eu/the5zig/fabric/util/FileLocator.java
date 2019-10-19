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

import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.fabricmc.loader.util.UrlConversionException;
import net.fabricmc.loader.util.UrlUtil;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class FileLocator {

    /**
     * Attempts to find the 5zig jar in the mods folder.
     * @param gameDir the .minecraft directory path
     * @return the jar or {@code null} if no jar was found.
     */
    public static ModFile getModJar(File gameDir) throws IOException {
        File modsDir = new File(gameDir, "mods");
        for(File file : Objects.requireNonNull(modsDir.listFiles())) {
            if(!file.isDirectory() && file.getName().endsWith(".jar")) {
                JarFile jar = new JarFile(file);
                String version = jar.getManifest().getMainAttributes().getValue("5zig-Version");
                if(version != null) return new ModFile(file, version);
            }
        }
        return null;
    }

    public static List<Path> getLibs() {
        return FabricLauncherBase.getLauncher().getLoadTimeDependencies().stream().map(url -> {
            try {
                return UrlUtil.asPath(url);
            } catch (UrlConversionException e) {
                throw new RuntimeException(e);
            }
        }).filter(Files::exists).collect(Collectors.toList());
    }

    public static String getAbsolutePath(File file) {
        String path = file.getAbsolutePath();
        if(SystemUtils.IS_OS_WINDOWS) {
            path = path.replace("\\", "/");
        }
        return path;
    }
}

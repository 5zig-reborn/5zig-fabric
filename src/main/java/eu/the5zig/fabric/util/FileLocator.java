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

package eu.the5zig.fabric.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
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
                if(version != null) {
                    jar.close();
                    return new ModFile(file, version);
                }
                jar.close();
            }
        }
        return null;
    }

    public static List<Path> getLibs(String[] args) {
        return Arrays.stream(args).map(File::new).filter(File::exists).map(File::toPath).collect(Collectors.toList());
    }

    public static FileSystem getZipFS(File file) throws IOException {
        return FileSystems.newFileSystem(file.toPath(), null);
    }
}

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

package eu.the5zig.fabric.remap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class RemapCache {
    public static File patchesDir;

    public static void init(File gameDir) {
        patchesDir = new File(gameDir, ".fabric_5zig/patches");
        if(!patchesDir.exists()) patchesDir.mkdirs();
    }

    public static File getPatchesFile(String version) {
        return new File(patchesDir, version + ".zig");
    }

    public static List<String> getPatches(String version) throws IOException {
        File file = getPatchesFile(version);
        if(!file.exists()) {
            file.createNewFile();
            return new ArrayList<>();
        }
        return Files.readAllLines(file.toPath());
    }

    public static void sealJar(File file) throws IOException {
        JarFile jar = new JarFile(file);
        Manifest mf = jar.getManifest();
        mf.getMainAttributes().put(new Attributes.Name("Fabric-Seal"), Long.toString(System.currentTimeMillis()));
        jar.close();
    }

    public static boolean checkSeal(File file) throws IOException {
        JarFile jar = new JarFile(file);
        Manifest mf = jar.getManifest();
        return mf.getMainAttributes().containsKey(new Attributes.Name("Fabric-Seal"));
    }
}

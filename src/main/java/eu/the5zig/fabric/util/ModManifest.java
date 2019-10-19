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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class ModManifest {
    public static void injectManifest(String version, File file) throws IOException {
        String data = IOUtils.toString(ModManifest.class.getResourceAsStream("/out.mod.json"), "UTF-8");
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();
        json.addProperty("version", version);
        URI uri = URI.create("jar:file:" + FileLocator.getAbsolutePath(file));

        try (FileSystem zipfs = FileSystems.newFileSystem(uri, new HashMap<>())) {
            Path pathInZipfile = zipfs.getPath("fabric.mod.json");
            Files.write(pathInZipfile, json.toString().getBytes("UTF-8"));
        }
    }
}

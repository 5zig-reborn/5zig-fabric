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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.the5zig.fabric.util.MethodUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.tinyremapper.IMappingProvider;
import net.fabricmc.tinyremapper.TinyRemapper;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class MixinRemapper {
    private JsonObject json;
    private JsonObject newJson;
    static TinyRemapper remapper, inverse;

    private File jarFile, newFile;

    public static MixinRemapper fromFile(File jarFile) throws IOException {
        ZipFile zipFile = new ZipFile(jarFile);
        FileInputStream is = new FileInputStream(jarFile);
        ZipInputStream zip = new ZipInputStream(is);
        ZipEntry entry;
        MixinRemapper res = null;
        while ((entry = zip.getNextEntry()) != null) {
            if (entry.getName().endsWith(".refmap.json")) {
                InputStream entryData = zipFile.getInputStream(entry);
                JsonObject json = new JsonParser().parse(new InputStreamReader(entryData, "UTF-8")).getAsJsonObject();
                entryData.close();
                res = new MixinRemapper(json);
                res.setJarFile(jarFile);
                break;
            }
        }
        zip.close();
        is.close();
        zipFile.close();
        return res;
    }

    private MixinRemapper(JsonObject json) {
        this.json = json;
        this.newJson = new JsonObject();

        String namespace = FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace();
        IMappingProvider provider = RemapperUtils.getMappings("official", namespace);
        remapper = TinyRemapper.newRemapper().withMappings(provider).build();
        remapper.getRemapper(); // Refresh
        inverse = TinyRemapper.newRemapper().withMappings(RemapperUtils.getMappings(namespace, "official")).build();
        inverse.getRemapper(); // Refresh
    }

    public void remap() {
        JsonObject notchData = json.getAsJsonObject("data").getAsJsonObject("notch");
        JsonObject newData = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : notchData.entrySet()) {
            String containerClass = entry.getKey();
            JsonObject toRemap = entry.getValue().getAsJsonObject();
            JsonObject newObj = new JsonObject();
            for (Map.Entry<String, JsonElement> entry1 : toRemap.entrySet()) {
                String methodName = entry1.getKey();
                String obfuscated = entry1.getValue().getAsString();
                String remap = obfuscated.contains(":") ? MethodUtils.remapField(remapper, obfuscated, containerClass, jarFile)
                        : MethodUtils.remapMethod(remapper, obfuscated);
                newObj.addProperty(methodName, remap);
            }
            newData.add(containerClass, newObj);
        }
        newJson.add("mappings", newData);
        JsonObject data = new JsonObject();
        data.add("named:intermediary", newData);
        newJson.add("data", data);
    }

    public void setJarFile(File jarFile) {
        this.jarFile = jarFile;
    }

    public void setNewFile(File newFile) {
        this.newFile = newFile;
    }

    public void write(FileSystem zipfs) throws IOException {
        Path pathInZipfile = zipfs.getPath("mixins.refmap.json");
        Files.write(pathInZipfile, newJson.toString().getBytes("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);
    }
}

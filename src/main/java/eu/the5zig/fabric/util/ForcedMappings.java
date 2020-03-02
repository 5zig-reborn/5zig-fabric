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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.MinecraftVersion;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class ForcedMappings {
    public static JsonObject mappings;

    public static void loadMappings() throws IOException {
        String data = IOUtils.toString(ForcedMappings.class.getResourceAsStream("/forcedMappings.json"), "UTF-8");
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();
        String version = MinecraftVersion.create().getName();
        JsonObject mappings = json.getAsJsonObject(version);
        ForcedMappings.mappings = mappings;
    }
}

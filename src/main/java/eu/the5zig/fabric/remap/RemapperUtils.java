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

import eu.the5zig.fabric.TransformerMain;
import net.fabricmc.loader.util.mappings.TinyRemapperMappingsHelper;
import net.fabricmc.tinyremapper.IMappingProvider;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RemapperUtils {
    public static void remap(Path out, Path in, IMappingProvider mappings, List<Path> libs) throws IOException {
        Files.deleteIfExists(out);
        TinyRemapper remapper = TinyRemapper.newRemapper()
                .withMappings(mappings)
                .resolveMissing(true)
                .renameInvalidLocals(true)
                .rebuildSourceFilenames(true)
                .build();
        remapper.getRemapper();
        OutputConsumerPath consumer = new OutputConsumerPath(out);
        consumer.addNonClassFiles(in);
        remapper.readInputs(in);

        for(Path path : libs) {
            remapper.readClassPath(path);
        }

        remapper.apply(consumer);
        consumer.close();
        remapper.finish();
    }

    public static IMappingProvider getMappings(String from, String to) {
        return TinyRemapperMappingsHelper.create(TransformerMain.mappings.getMappings(), from, to);
    }
}

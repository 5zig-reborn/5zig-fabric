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

package eu.the5zig.fabric;

import eu.the5zig.fabric.boot.FabricTransformer;
import eu.the5zig.fabric.builtin.MappingLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TransformerMain {
    public static MappingLoader mappings;
    public static final Logger LOGGER = LogManager.getLogger("5zig-Fabric");

    public static void main(String[] args) {
        mappings = new MappingLoader();
        new FabricTransformer(args, "1.15.2").run();
        System.out.println("Work done.");
        System.exit(0);
    }
}

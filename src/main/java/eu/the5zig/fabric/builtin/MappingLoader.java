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

package eu.the5zig.fabric.builtin;

import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MappingLoader {
    protected static Logger LOGGER = LogManager.getFormatterLogger("FabricLoader");

    private static TinyTree mappings;
    private static boolean checkedMappings;

    public TinyTree getMappings() {
        if (!checkedMappings) {
            InputStream mappingStream = FabricLauncherBase.class.getClassLoader().getResourceAsStream("mappings/mappings.tiny");

            if (mappingStream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(mappingStream))) {
                    long time = System.currentTimeMillis();
                    mappings = TinyMappingFactory.loadWithDetection(reader);
                    LOGGER.info("Loading mappings took " + (System.currentTimeMillis() - time) + " ms");
                } catch (IOException ee) {
                    ee.printStackTrace();
                }

                try {
                    mappingStream.close();
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
            }

            if (mappings == null) {
                LOGGER.info("Mappings not present!");
                mappings = TinyMappingFactory.EMPTY_TREE;
            }

            checkedMappings = true;
        }

        return mappings;
    }

    public String getTargetNamespace() {
        return "intermediary";
    }
}

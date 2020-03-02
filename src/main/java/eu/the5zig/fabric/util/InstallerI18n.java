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

import net.minecraft.client.MinecraftClient;

import java.util.Locale;
import java.util.ResourceBundle;

public class InstallerI18n {
    private static ResourceBundle strings;

    public static void init() {
        try {
        Locale currentLocale = Locale.forLanguageTag(MinecraftClient.getInstance().getLanguageManager().getLanguage()
                .getCode().replace('_', '-'));
            strings = ResourceBundle.getBundle("lang.language", currentLocale);
        } catch(Exception e) {
            strings = ResourceBundle.getBundle("lang.language", Locale.US);
        }
    }

    public static String s(String key) {
        return strings.getString(key);
    }
}

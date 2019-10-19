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

package eu.the5zig.fabric;

import eu.the5zig.fabric.util.InstallerI18n;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.Logger;


public class FabricMod implements ModInitializer {

    public static final Logger LOGGER = FabricLoader.INSTANCE.getLogger();
    public static boolean success;
    private static boolean shown;

    @Override
    public void onInitialize() {

    }

    public static void showConfirm() {
        if(shown || !success) return;
        shown = true;
        InstallerI18n.init();

        Screen current = MinecraftClient.getInstance().currentScreen;
        String title = InstallerI18n.s("installer.title");
        String info = InstallerI18n.s("installer.info");
        String now = InstallerI18n.s("installer.now");
        String later = InstallerI18n.s("installer.later");

        ConfirmScreen screen = new ConfirmScreen(t -> {
            if(t) MinecraftClient.getInstance().scheduleStop();
            else MinecraftClient.getInstance().openScreen(current);
        }, new LiteralText(title), new LiteralText(info),
                Formatting.GREEN + now,
                Formatting.YELLOW + later);
        MinecraftClient.getInstance().openScreen(screen);
    }
}

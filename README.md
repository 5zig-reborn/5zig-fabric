# 5zig -> Fabric patcher
[![Build Status](https://travis-ci.org/5zig-reborn/5zig-fabric.svg?branch=master)](https://travis-ci.org/5zig-reborn/5zig-fabric)

This makes loading 5zig on Fabric possible by patching the JAR at runtime.

### Note about OptiFabric
When patching a JAR with this mod, please remove OptiFabric from your mods.  
You can add it later once the patched JAR is created.

## Usage
1. Download the project from the [Releases](https://github.com/5zig-reborn/5zig-fabric/releases) tab.
2. Put the file you downloaded **and** the relative [5zig](https://github.com/5zig-reborn/The-5zig-Mod) file in
the `.minecraft/mods` folder.
3. Run Minecraft.
    * The patcher should run and you should receive a confirmation message prompting you to restart your game.
    * Click on `Restart Now`.
4. If everything went correctly, The 5zig Mod should now load and the "unpatched" 5zig file should be deleted
from your `mods` folder.

## Credits
Special thanks to @modmuss50 on [Fabric's Discord](https://discord.gg/v6v4pMv) for helping us figure out a way
to load 5zig on Fabric.
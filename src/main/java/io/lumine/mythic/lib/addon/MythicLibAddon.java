package io.lumine.mythic.lib.addon;

import io.lumine.mythic.lib.MythicLib;

import java.io.File;

public class MythicLibAddon {
    public static File getPlayerFolder() {
        return new File(MythicLib.plugin.getDataFolder(), "playerdata");
    }

    public static File getPlayerFile(String name) {
        return new File(getPlayerFolder(), name.toLowerCase() + ".yml");
    }
}

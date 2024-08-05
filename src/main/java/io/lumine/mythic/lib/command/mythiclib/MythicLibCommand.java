//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.lumine.mythic.lib.command.mythiclib;

import io.lumine.mythic.lib.command.api.CommandTreeRoot;
import io.lumine.mythic.lib.command.mythiclib.debug.DebugCommand;

public class MythicLibCommand extends CommandTreeRoot {
    public MythicLibCommand() {
        super("mythiclib", "mythiclib.admin");
        this.addChild(new ReloadCommand(this));
        this.addChild(new CastCommand(this));
        this.addChild(new DebugCommand(this));
        this.addChild(new StatModCommand(this));
        this.addChild(new StatAddCommand(this));
        this.addChild(new StatRemoveCommand(this));
        this.addChild(new StatModTempCommand(this));
    }
}

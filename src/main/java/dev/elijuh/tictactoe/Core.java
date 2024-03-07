package dev.elijuh.tictactoe;

import dev.elijuh.tictactoe.commands.CommandManager;
import dev.elijuh.tictactoe.commands.impl.TicTacToeCommand;
import dev.elijuh.tictactoe.invite.InviteManager;
import dev.elijuh.tictactoe.menu.Menu;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author elijuh
 */
@Getter
public class Core extends JavaPlugin {
    public static final String DISPLAY = "§a§lTic§7-§a§lTac§7-§a§lToe";
    private static Core instance;

    private final InviteManager inviteManager = new InviteManager();
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        instance = this;
        Menu.registerListener(this);
        commandManager = new CommandManager(this);
        commandManager.register(new TicTacToeCommand());
    }

    @Override
    public void onDisable() {
        Menu.closeAll();
        commandManager.unregisterAll();
    }

    public static Core i() {
        return instance;
    }
}

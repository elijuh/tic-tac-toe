package dev.elijuh.tictactoe.commands.impl;

import com.google.common.collect.ImmutableList;
import dev.elijuh.tictactoe.Core;
import dev.elijuh.tictactoe.commands.Command;
import dev.elijuh.tictactoe.menu.TicTacToeMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author elijuh
 */
public class TicTacToeCommand extends Command {
    private static final String infoMessage = Core.DISPLAY + " §7made by §5§oelijuh §c❤" +
        "\n \n§7Usage: /tictactoe <player>";

    public TicTacToeCommand() {
        super("tictactoe", ImmutableList.of(), null);
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return args.length == 1 ? null : ImmutableList.of();
    }

    @Override
    public void onExecute(Player p, String[] args) {
        if (args.length == 2 && args[0].equals("accept")) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                p.sendMessage("§cCouldn't find player: §7" + args[1]);
                return;
            }
            UUID receiver = Core.i().getInviteManager().getInviteReceiver(target.getUniqueId());
            if (!Objects.equals(p.getUniqueId(), receiver)) {
                p.sendMessage("§cYou don't have an invite from this player.");
                return;
            }

            Core.i().getInviteManager().removeInvite(target.getUniqueId());
            Core.i().getInviteManager().removeInvite(p.getUniqueId());
            TicTacToeMenu menu = new TicTacToeMenu(target, p);
            menu.open(target);
            menu.open(p);
        } else if (args.length == 1) {
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                p.sendMessage("§cCouldn't find player: §7" + args[0]);
                return;
            }

            Core.i().getInviteManager().sendInvite(p, target);
        } else {
            p.sendMessage(infoMessage);
        }
    }
}

package dev.elijuh.tictactoe.menu;

import com.cryptomorin.xseries.XMaterial;
import dev.elijuh.tictactoe.Core;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author elijuh
 */
@SuppressWarnings("DataFlowIssue")
public class TicTacToeMenu extends Menu {
    private final Player[] teams;
    private final int[] grid = {
        -1, -1, -1,
        -1, -1, -1,
        -1, -1, -1
    };

    private int moves;
    private boolean ended;

    public TicTacToeMenu(Player red, Player blue) {
        super(InventoryType.DISPENSER, "Tic-Tac-Toe");
        teams = new Player[]{red, blue};
        fillBlanks();
    }

    private int getTurn() {
        return moves % 2;
    }

    private String coloredName(int team) {
        return (team == 0 ? "§c" : "§9") + teams[team].getName();
    }

    private int getTurn(Player p) {
        for (int i = 0; i < teams.length; i++) {
            if (teams[i] == p) return i;
        }
        throw new IllegalArgumentException("no turn for player " + p.getName());
    }

    private static final ItemStack[] MARKERS = new ItemStack[2];
    static {
        ItemStack x = XMaterial.RED_WOOL.parseItem();
        ItemStack o = XMaterial.BLUE_WOOL.parseItem();
        ItemMeta xMeta = x.getItemMeta();
        ItemMeta oMeta = x.getItemMeta();
        xMeta.setDisplayName("§c§lX");
        oMeta.setDisplayName("§9§lO");
        x.setItemMeta(xMeta);
        o.setItemMeta(oMeta);
        MARKERS[0] = x;
        MARKERS[1] = o;
    }

    private void fillBlanks() {
        ItemStack blank = XMaterial.BLACK_STAINED_GLASS_PANE.parseItem();
        ItemMeta meta = blank.getItemMeta();
        meta.setDisplayName("§7It's " + coloredName(getTurn()) + "§7's turn!");
        blank.setItemMeta(meta);

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() == blank.getType()) {
                inventory.setItem(i, blank);
            }
        }
    }

    private void tryWin(int row, int column) {
        int turn = getTurn();
        //check column 3
        for (int i = 0; true; i++) {
            if (grid[row * 3 + i] != turn) break;
            if (i == 2) {
                win(turn);
                return;
            }
        }
        //check row 3
        for (int i = 0; true; i++) {
            if (grid[i * 3 + column] != turn) break;
            if (i == 2) {
                win(turn);
                return;
            }
        }
        if (row == column) {
            //top-left to bottom-right diag
            for (int i = 0; true; i++) {
                if (grid[i * 3 + i] != turn) break;
                if (i == 2) {
                    win(turn);
                    return;
                }
            }
        } else if (row + column == 2) {
            for (int i = 0; true; i++) {
                if (grid[i * 3 + 3 - 1 - i] != turn) break;
                if (i == 2) {
                    win(turn);
                    return;
                }
            }
        }
    }

    private void win(int turn) {
        ended = true;
        String coloredName = coloredName(turn);
        for (Player p : teams) {
            p.closeInventory();
            p.sendMessage(coloredName + " §ehas won the " + Core.DISPLAY + "§e!");
            p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1f, 1f);
        }
    }

    private void quit(Player quitting) {
        ended = true;
        String coloredName = coloredName(getTurn(quitting));
        for (Player p : teams) {
            p.closeInventory();
            p.sendMessage(coloredName + " §ehas quit the " + Core.DISPLAY + "§e!");
            p.playSound(p.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
        }
    }

    private void draw() {
        ended = true;
        for (Player p : teams) {
            p.closeInventory();
            p.sendMessage("§eThe " + Core.DISPLAY + " §egame ended in a draw!");
            p.playSound(p.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
        }
    }

    @Override
    protected void onClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);
        int turn = getTurn();
        Player clicker = ((Player) e.getWhoClicked());
        if (teams[turn] != clicker) return;

        int slot = e.getRawSlot();
        if (slot < 0 || slot >= inventory.getSize() || grid[slot] != -1) return;

        grid[slot] = turn;
        inventory.setItem(slot, MARKERS[turn]);
        tryWin(slot / 3, slot % 3);
        moves++;
        fillBlanks();
        clicker.playSound(clicker.getLocation(), Sound.CLICK, 1f, 1f);
        if (moves >= grid.length) {
            draw();
        }
    }

    @Override
    protected void onCloseEvent(InventoryCloseEvent e) {
        if (!ended) {
            quit(((Player) e.getPlayer()));
        }
    }
}

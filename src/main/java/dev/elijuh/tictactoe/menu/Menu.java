package dev.elijuh.tictactoe.menu;

import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author elijuh
 */
@SuppressWarnings({"unused", "DataFlowIssue"})
@Getter
public abstract class Menu implements InventoryHolder {
    protected final Inventory inventory;

    public Menu(int rows, String title) {
        ensureListenersRegistered();
        this.inventory = Bukkit.createInventory(this, rows * 9, title);
    }

    public Menu(InventoryType type, String title) {
        ensureListenersRegistered();
        this.inventory = Bukkit.createInventory(this, type, title);
    }

    private static boolean listenersRegistered = false;

    private void ensureListenersRegistered() {
        if (!listenersRegistered) {
            throw new IllegalStateException("Menu listeners have not been registered");
        }
    }

    protected void fill() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, getFiller());
        }
    }

    protected void fillEdges() {
        int rowLength = getRowLength();
        if (rowLength == -1) return;
        int rows = inventory.getSize() / rowLength;

        //top and bottom rows
        for (int i = 0; i < rowLength; i++) {
            inventory.setItem(i, getFiller());
            inventory.setItem(inventory.getSize() - rowLength + i, getFiller());
        }

        //middle rows
        for (int i = 1; i < rows - 1; i++) {
            inventory.setItem(i * rowLength, getFiller());
            inventory.setItem(i * rowLength + 8, getFiller());
        }
    }

    protected int[] getCenterSlots() {
        int rowLength = getRowLength();
        int size = inventory.getSize();
        if (rowLength == -1 || size < rowLength * 3) return new int[0];

        int rows = size / rowLength;
        int[] slots = new int[size - (rowLength * 2) - 2 * (rows - 2)];
        int slotIndex = 0;
        for (int i = 1; i < rows - 1; i++) {
            for (int j = 1; j < rowLength - 1; j++) {
                slots[slotIndex++] = i * rowLength + j;
            }
        }
        return slots;
    }

    protected int getRowLength() {
        switch (inventory.getType()) {
            case ENDER_CHEST:
            case CHEST: return 9;
            case DROPPER:
            case DISPENSER: return 3;
            default: return -1;
        }
    }

    private static final ItemStack DEFAULT_FILLER;
    static {
        DEFAULT_FILLER = XMaterial.BLACK_STAINED_GLASS_PANE.parseItem();
        ItemMeta meta = DEFAULT_FILLER.getItemMeta();
        meta.setDisplayName(" ");
        DEFAULT_FILLER.setItemMeta(meta);
    }
    private ItemStack filler;

    public void setFiller(ItemStack filler) {
        this.filler = Objects.requireNonNull(filler, "filler");
    }

    public ItemStack getFiller() {
        return filler == null ? DEFAULT_FILLER : filler;
    }

    public void open(Player p) {
        if (p.getOpenInventory().getTopInventory() != inventory) {
            p.openInventory(inventory);
        }
    }

    protected void onClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);
    }

    protected void onDragEvent(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    protected void onCloseEvent(InventoryCloseEvent e) {}

    private final Map<Integer, Consumer<InventoryClickEvent>> slotListeners = new HashMap<>();

    public void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick) {
        if (slot > inventory.getSize()) return;
        inventory.setItem(slot, item);
        slotListeners.put(slot, onClick);
    }

    public void setItem(int[] slots, ItemStack item, Consumer<InventoryClickEvent> onClick) {
        for (int slot : slots) {
            setItem(slot, item, onClick);
        }
    }

    public static void registerListener(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(new Listener() {

            @EventHandler
            private void on(InventoryClickEvent e) {
                Inventory inv = e.getView().getTopInventory();
                if (inv != null && inv.getHolder() instanceof Menu) {
                    Menu menu = (Menu) inv.getHolder();
                    Consumer<InventoryClickEvent> consumer = menu.slotListeners.get(e.getRawSlot());
                    if (consumer != null) {
                        consumer.accept(e);
                    }
                    menu.onClickEvent(e);
                }
            }

            @EventHandler
            private void on(InventoryDragEvent e) {
                if (e.getInventory().getHolder() instanceof Menu) {
                    Menu menu = (Menu) e.getInventory().getHolder();
                    menu.onDragEvent(e);
                }
            }

            @EventHandler
            private void on(InventoryCloseEvent e) {
                if (e.getInventory().getHolder() instanceof Menu) {
                    Menu menu = (Menu) e.getInventory().getHolder();
                    menu.onCloseEvent(e);
                }
            }

        }, plugin);
        listenersRegistered = true;
    }

    public static void closeAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            InventoryView view = p.getOpenInventory();
            Inventory inv = view.getTopInventory();
            if (inv != null && inv.getHolder() instanceof Menu) {
                ((Menu) inv.getHolder()).onCloseEvent(new InventoryCloseEvent(view));
                view.close();
            }
        }
    }
}

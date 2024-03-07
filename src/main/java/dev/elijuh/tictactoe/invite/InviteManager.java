package dev.elijuh.tictactoe.invite;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.elijuh.tictactoe.Core;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author elijuh
 */
@SuppressWarnings("UnstableApiUsage")
public class InviteManager {
    private final Cache<UUID, UUID> pendingInvites = CacheBuilder.newBuilder()
        .expireAfterWrite(20, TimeUnit.SECONDS)
        .build();

    private static final HoverEvent hover = new HoverEvent(
        HoverEvent.Action.SHOW_TEXT,
        TextComponent.fromLegacyText("§7Click to accept.")
    );

    public void sendInvite(Player sender, Player receiver) {
        pendingInvites.put(sender.getUniqueId(), receiver.getUniqueId());
        sender.sendMessage("§eYou have invited §6" + receiver.getName() + " §eto play " + Core.DISPLAY);
        receiver.sendMessage("§6" + sender.getName() + " §ehas invited you to play " + Core.DISPLAY);

        ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tictactoe accept " + sender.getName());
        BaseComponent[] accept = TextComponent.fromLegacyText("§aClick here to accept the invitation.");
        for (BaseComponent component : accept) {
            component.setClickEvent(click);
            component.setHoverEvent(hover);
        }
        receiver.spigot().sendMessage(accept);
        receiver.playSound(receiver.getLocation(), Sound.ORB_PICKUP, 2f, 1f);
    }

    public UUID getInviteReceiver(UUID sender) {
        return pendingInvites.getIfPresent(sender);
    }

    public void removeInvite(UUID sender) {
        pendingInvites.invalidate(sender);
    }
}

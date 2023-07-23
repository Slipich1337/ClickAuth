package me.clickism.clickauth;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Utils {

    static ClickAuth plugin;

    public static void setPlugin(ClickAuth plugin) {
        Utils.plugin = plugin;
    }

    public static void playConfirmSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f,1f);
        Bukkit.getScheduler().runTaskLater(plugin, task -> {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f,2f);
        }, 2L);
    }

    public static void playFailSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1f,.5f);
    }
}

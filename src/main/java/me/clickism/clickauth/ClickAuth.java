package me.clickism.clickauth;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClickAuth extends JavaPlugin {

    private static DataManager data;

    public static DataManager getData() {
        return data;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getLogger().info("ClickAuth activated.");
        Bukkit.getPluginManager().registerEvents(new Events(), this);
        Events.setPlugin(this);
        Utils.setPlugin(this);

        data = new DataManager(this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().info("ClickAuth deactivated.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("resetpassword")) {
            if (sender instanceof Player) {
                Events.changePassword((Player) sender);
            } else {
                sender.sendMessage("You can't use this.");
            }
        }
        return true;
    }
}

package me.clickism.clickauth;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Events implements Listener {

    private final String seed = "kellerherkesieller";
    private static ClickAuth plugin;

    public static void setPlugin(ClickAuth pl) {
        plugin = pl;
    }

    private static List<HumanEntity> notLoggedIn = new ArrayList<>();
    private static List<HumanEntity> waitingForPassword = new ArrayList<>();
    private static List<HumanEntity> waitingForNewPassword = new ArrayList<>();
    private static HashMap<HumanEntity, String> confirmPassword = new HashMap<>();

    private static HashMap<HumanEntity, Integer> falseTries = new HashMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        String ip = "";
        if (e.getPlayer().getAddress() != null) {
            ip = e.getPlayer().getAddress().getAddress().toString();
        }
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(seed);
        if (ip.equals(encryptor.decrypt((String) ClickAuth.getData().getConfig().get(e.getPlayer().getName() + ".ip")))) {
            Bukkit.getScheduler().runTaskLater(plugin, task -> {
                e.getPlayer().sendMessage(ChatColor.GOLD + ">> " + ChatColor.GREEN + "Welcome back.");
                e.getPlayer().setAllowFlight(false);
            },1L);
        } else {
            notLoggedIn.add(e.getPlayer());
            falseTries.put(e.getPlayer(), 0);
            e.getPlayer().setInvulnerable(true);
            e.getPlayer().setAllowFlight(true);
            Bukkit.getScheduler().runTaskLater(plugin, task -> {
                if (ClickAuth.getData().getConfig().contains(e.getPlayer().getName())) {
                    e.getPlayer().sendTitle("",ChatColor.GOLD + "<< " + ChatColor.RED + "Please enter your password" + ChatColor.GOLD + " >>", 20,999999, 20);
                    waitingForPassword.add(e.getPlayer());
                } else {
                    e.getPlayer().sendTitle("",ChatColor.GOLD + "<< " + ChatColor.RED + "Please enter a password" + ChatColor.GOLD + " >>",20,999999, 20);
                    waitingForNewPassword.add(e.getPlayer());
                }
            }, 1L);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        String pass = e.getMessage();
        if (waitingForPassword.contains(e.getPlayer())) {
            e.setCancelled(true);
            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            encryptor.setPassword(seed);
            if (pass.equals(encryptor.decrypt((String) ClickAuth.getData().getConfig().get(e.getPlayer().getName() + ".password")))) {
                String ip = "";
                if (e.getPlayer().getAddress() != null) {
                    ip = e.getPlayer().getAddress().getAddress().toString();
                }
                ClickAuth.getData().getConfig().set(e.getPlayer().getName() + ".ip", encryptor.encrypt(ip));
                e.getPlayer().setInvulnerable(false);
                e.getPlayer().setAllowFlight(false);
                notLoggedIn.remove(e.getPlayer());
                waitingForPassword.remove(e.getPlayer());
                e.getPlayer().resetTitle();
                e.getPlayer().sendMessage(ChatColor.GOLD + ">> " + ChatColor.GREEN + "Welcome back.");
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_PORTAL_TRAVEL, .2f,1f);
                Utils.playConfirmSound(e.getPlayer());
            } else {
                if (falseTries.get(e.getPlayer()) < 3) {
                    e.getPlayer().sendMessage(ChatColor.GOLD + ">> " + ChatColor.RED + "Wrong password, please try again.");
                    Utils.playFailSound(e.getPlayer());
                    falseTries.put(e.getPlayer(), falseTries.get(e.getPlayer()) + 1);
                } else {
                    Bukkit.getScheduler().runTask(plugin, task -> {
                        e.getPlayer().kickPlayer(ChatColor.RED + "" + ChatColor.BOLD + "Too many tries.");
                    });
                }
            }
        } else if (waitingForNewPassword.contains(e.getPlayer())) {
            e.setCancelled(true);
            if (pass.contains(" ")) {
                e.getPlayer().sendMessage(ChatColor.GOLD + ">> " + ChatColor.RED + "Please input a valid password.");
            } else {
                waitingForNewPassword.remove(e.getPlayer());
                confirmPassword.put(e.getPlayer(), pass);
                e.getPlayer().sendMessage(ChatColor.GOLD + ">> " + ChatColor.GREEN + "Please confirm your password.");
                Utils.playConfirmSound(e.getPlayer());
            }
        } else if (confirmPassword.containsKey(e.getPlayer())) {
            e.setCancelled(true);
            if (pass.equals(confirmPassword.get(e.getPlayer()))) {
                StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
                encryptor.setPassword(seed);
                String ip = "";
                if (e.getPlayer().getAddress() != null) {
                    ip = e.getPlayer().getAddress().getAddress().toString();
                }
                e.getPlayer().resetTitle();
                if (ClickAuth.getData().getConfig().contains(e.getPlayer().getName())) {
                    e.getPlayer().sendMessage(ChatColor.GOLD + ">> " + ChatColor.GREEN + "Password changed.");
                } else {
                    e.getPlayer().sendMessage(ChatColor.GOLD + ">> " + ChatColor.GREEN + "Password created.");
                    e.getPlayer().sendTitle("",ChatColor.GOLD + "<< " + ChatColor.GREEN + "Welcome" + ChatColor.GOLD + " >>", 10,50, 40);
                    e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_PORTAL_TRAVEL, .2f,1f);
                }
                ClickAuth.getData().getConfig().set(e.getPlayer().getName() + ".password", encryptor.encrypt(pass));
                ClickAuth.getData().getConfig().set(e.getPlayer().getName() + ".ip", encryptor.encrypt(ip));
                ClickAuth.getData().saveConfig();
                confirmPassword.remove(e.getPlayer());
                notLoggedIn.remove(e.getPlayer());
                e.getPlayer().setInvulnerable(false);
                e.getPlayer().setAllowFlight(false);
                Utils.playConfirmSound(e.getPlayer());
            } else {
                e.getPlayer().sendMessage(ChatColor.GOLD + ">> " + ChatColor.RED + "The passwords don't match, please try again.");
                Utils.playFailSound(e.getPlayer());
            }
        }
    }

    public static void changePassword(Player player) {
        player.sendMessage(ChatColor.GOLD + ">> " + ChatColor.GREEN + "Please enter a new password.");
        waitingForNewPassword.add(player);
    }

    @EventHandler
    public void onTarget(EntityTargetEvent e) {
        if (e.getTarget() instanceof Player) {
            if (notLoggedIn.contains((Player) e.getTarget())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (notLoggedIn.contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (notLoggedIn.contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventory(InventoryOpenEvent e) {
        if (notLoggedIn.contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (notLoggedIn.contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof HumanEntity) {
            if (notLoggedIn.contains((HumanEntity) e.getEntity())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof HumanEntity) {
            if (notLoggedIn.contains((HumanEntity) e.getEntity())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof HumanEntity) {
            if (notLoggedIn.contains((HumanEntity) e.getDamager())) {
                e.setCancelled(true);
            }
        } else if (e.getEntity() instanceof HumanEntity) {
            if (notLoggedIn.contains((HumanEntity) e.getEntity())) {
                e.setCancelled(true);
            }
        }
    }

}

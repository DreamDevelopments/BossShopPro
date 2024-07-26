package org.black_ixx.bossshop.managers.misc;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.black_ixx.bossshop.managers.ClassManager;
import org.black_ixx.bossshop.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class PacketManager {

    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    private static final List<ItemStack> emptyInventory;
    private static final PacketContainer inventoryClearPacket;
    private static final PacketContainer spectatorPacket;
    private static final PacketContainer survivalPacket;
    private static final PacketContainer creativePacket;
    private static final PacketContainer adventurePacket;

    private static final HashMap<Player, Integer> hiddenInventoriesPlayers = new HashMap<>();

    static {
        emptyInventory = new ArrayList<>(45);
        for(int i = 0; i < 45; i++) {
            emptyInventory.add(new ItemStack(Material.AIR));
        }
        inventoryClearPacket = new PacketContainer(PacketType.Play.Server.WINDOW_ITEMS);
        inventoryClearPacket.getIntegers().write(0, 0);
        inventoryClearPacket.getIntegers().write(1, -1);
        inventoryClearPacket.getItemListModifier().write(0, emptyInventory);
        inventoryClearPacket.getItemModifier().write(0, new ItemStack(Material.AIR));

        spectatorPacket = new PacketContainer(PacketType.Play.Server.GAME_STATE_CHANGE);
        spectatorPacket.getGameStateIDs().write(0, 3);
        spectatorPacket.getFloat().write(0, 3.0F);

        survivalPacket = new PacketContainer(PacketType.Play.Server.GAME_STATE_CHANGE);
        survivalPacket.getGameStateIDs().write(0, 3);
        survivalPacket.getFloat().write(0, 0.0F);

        creativePacket = new PacketContainer(PacketType.Play.Server.GAME_STATE_CHANGE);
        creativePacket.getGameStateIDs().write(0, 3);
        creativePacket.getFloat().write(0, 1.0F);

        adventurePacket = new PacketContainer(PacketType.Play.Server.GAME_STATE_CHANGE);
        adventurePacket.getGameStateIDs().write(0, 3);
        adventurePacket.getFloat().write(0, 2.0F);
    }

    public static void clearPlayerInventory(Player player) {
        sendClearPacket(player);
        hiddenInventoriesPlayers.put(player, player.getOpenInventory().getTopInventory().getSize());
    }

    public static void sendClearPacket(Player player) {
        sendClearPacket(player, true);
    }

    public static void sendClearPacket(Player player, boolean gamemode) {
        protocolManager.sendServerPacket(player, inventoryClearPacket);
        if(gamemode && player.getVehicle() == null)
            sendFakeGameMode(player);
    }

    private static void sendFakeGameMode(Player player) {
        if(!hiddenInventoriesPlayers.containsKey(player))
            return;
        if(!player.getLocation().subtract(0, 0.3f, 0).getBlock().isPassable())
            protocolManager.sendServerPacket(player, spectatorPacket);
        else
            Bukkit.getScheduler().runTaskLater(ClassManager.manager.getPlugin(), () -> sendFakeGameMode(player), 3);
    }

    public static void restorePlayerInventory(Player player) {
        hiddenInventoriesPlayers.remove(player);
        Bukkit.getScheduler().runTaskLater(ClassManager.manager.getPlugin(), () -> {
            if(!hiddenInventoriesPlayers.containsKey(player)) {
                GameMode gameMode = player.getGameMode();
                if (gameMode.equals(GameMode.SURVIVAL))
                    protocolManager.sendServerPacket(player, survivalPacket);
                else if (gameMode.equals(GameMode.CREATIVE))
                    protocolManager.sendServerPacket(player, creativePacket);
                else if (gameMode.equals(GameMode.ADVENTURE))
                    protocolManager.sendServerPacket(player, adventurePacket);
/*                PlayerInventory inventory = player.getInventory();
                inventory.setContents(inventory.getContents())*/;
                player.updateInventory();
            }
        }, 1);
    }

    private static boolean hasNoInventory(Player player) {
        return (player.getOpenInventory().getTopInventory() instanceof PlayerInventory);
    }

    public static boolean hasInventoryCleared(Player player) {
        return hiddenInventoriesPlayers.containsKey(player);
    }

    public static boolean hasInvisibleTag(InventoryView inventory) {
        return inventory.getTitle().contains(ClassManager.manager.getSettings().getPropertyString(Settings.INVISIBLE_INVENTORY_TAG, null, "DD_INVISIBLE_INVENTORY"));
    }

    public static void cancelPacketsForHiddenInventories(JavaPlugin plugin) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
/*        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.WINDOW_CLICK) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if(!hiddenInventoriesPlayers.containsKey(event.getPlayer()))
                    return;

                Bukkit.broadcastMessage("SET SLOT: " + event.getPacket().getIntegers().getValues().get(1) + " ___ " + event.getPacket().getIntegers().getValues().get(2));
                Bukkit.broadcastMessage("WINDOWS CLICK");
                if(event.getPacket().getIntegers().getValues().get(2) >= hiddenInventoriesPlayers.get(event.getPlayer())) {
                    event.setCancelled(true);
                    Bukkit.broadcastMessage("CANCELED");
                }
            }
        });*/

        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.SET_SLOT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if(!hiddenInventoriesPlayers.containsKey(event.getPlayer()) || event.getPacket().getIntegers().getValues().get(1) == -1)
                    return;
                if(event.getPacket().getIntegers().getValues().get(2) >= hiddenInventoriesPlayers.get(event.getPlayer())) {
                    event.setCancelled(true);
                }
                //sendClearPacket(event.getPlayer(), false);
            }
        });

/*        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.OPEN_WINDOW) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if(!hiddenInventoriesPlayers.containsKey(event.getPlayer()))
                    return;
                sendClearPacket(event.getPlayer(), false);
            }
        });*/

    }

}

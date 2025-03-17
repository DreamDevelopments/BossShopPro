package org.black_ixx.bossshop.pointsystem;

import com.bencodez.votingplugin.VotingPluginMain;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import com.bencodez.votingplugin.user.VotingPluginUser;


public class BSPointsPluginVotingPlugin extends BSPointsPlugin {

    public BSPointsPluginVotingPlugin() {
        super("VotingPlugin", "VP");
    }


    @Override
    public double getPoints(OfflinePlayer player) {
        if (player instanceof Player) {
            VotingPluginUser user = VotingPluginMain.plugin.getVotingPluginUserManager().getVotingPluginUser((Player) player);
            return user.getPoints();
        } else {
            return 0;
        }
    }

    @Override
    public double setPoints(OfflinePlayer player, double points) {
        if (player instanceof Player) {
            VotingPluginUser user = VotingPluginMain.plugin.getVotingPluginUserManager().getVotingPluginUser((Player) player);
            user.setPoints((int) points);
            return points;
        } else {
            return 0;
        }
    }

    @Override
    public double takePoints(OfflinePlayer player, double points) {
        if (player instanceof Player) {
            VotingPluginUser user = VotingPluginMain.plugin.getVotingPluginUserManager().getVotingPluginUser((Player) player);
            user.removePoints((int) points);
            return getPoints(player);
        } else {
            return 0;
        }
    }

    @Override
    public double givePoints(OfflinePlayer player, double points) {
        if (player instanceof Player) {
            VotingPluginUser user = VotingPluginMain.plugin.getVotingPluginUserManager().getVotingPluginUser((Player) player);
            user.addPoints((int) points);
            return getPoints(player);
        } else {
            return 0;
        }
    }


    @Override
    public boolean usesDoubleValues() {
        return false;
    }

}

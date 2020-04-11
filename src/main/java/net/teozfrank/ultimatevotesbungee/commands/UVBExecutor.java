package net.teozfrank.ultimatevotesbungee.commands;

import net.teozfrank.ultimatevotesbungee.main.UltimateVotesBungee;
import net.teozfrank.ultimatevotesbungee.util.BroadcastManager;
import net.teozfrank.ultimatevotesbungee.util.DatabaseManager;
import net.teozfrank.ultimatevotesbungee.util.UUIDFetcher;
import net.teozfrank.ultimatevotesbungee.util.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

/**
 * Created by Frank on 05/01/2015.
 */
public class UVBExecutor extends Command {

    private UltimateVotesBungee plugin;

    public UVBExecutor(UltimateVotesBungee plugin) {
        super("uvb");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("ultimatevotesbungee.admin")) {
            Util.sendMessage(sender, ChatColor.RED + "You do not have permission for this command!");
            return;
        }
        if(args.length < 1) {
            Util.sendEmptyMessage(sender, ChatColor.AQUA + "UltimateVotesBungee Commands.");
            Util.sendEmptyMessage(sender, "");
            Util.sendEmptyMessage(sender, ChatColor.AQUA + "/uvb addtestvote <player> - "
                    + ChatColor.GOLD + " attempt add a test vote for a player");
            Util.sendEmptyMessage(sender, ChatColor.AQUA + "/uvb reward <player> - "
                    + ChatColor.GOLD + " attempt to reward an online player using bungeecord communication (they need to have unclaimed votes)");
        } else if(args.length == 2 && args[0].equals("reward")) {
            String playerName = args[1];
            UUID uuid = plugin.getMySql().getUUIDFromUsername(playerName);
            plugin.getUtil().sendServerMessage(uuid);
            Util.sendMessage(sender, ChatColor.GREEN + "Rewarding player " + playerName);
        } else if(args.length == 2 && args[0].equals("convert")) {
            String playerName = args[1];
            UUIDFetcher uuidFetcher = new UUIDFetcher(playerName);
            try {
                UUID uuid = uuidFetcher.call();
                Util.sendMessage(sender, ChatColor.GREEN + "UUID: " + uuid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(args.length == 2 && args[0].equals("addtestvote")) {
            String playerName = args[1];
            DatabaseManager mySql = plugin.getMySql();
            try {
                UUID uuid = mySql.getUUIDFromUsername(playerName);
                if(uuid == null) {
                    Util.sendMessage(sender, ChatColor.GREEN + "UUID retrieval failed!: " + playerName);
                    return;
                }
                plugin.getMySql().addPlayerAllTimeVote(uuid, playerName);
                plugin.getMySql().addPlayerMonthlyVote(uuid, playerName);
                Util.sendMessage(sender, ChatColor.GREEN + "Successfully added vote for player: " + playerName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (args.length >= 2 && args[0].equalsIgnoreCase("broadcast")) {

            StringBuilder messages = new StringBuilder();

            for (int x = 1; x < args.length; x++) {
                messages.append(args[x]).append(" ");
            }
            String message = messages.toString();
            message = ChatColor.translateAlternateColorCodes('&', message);

            Util.broadcast(message);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("broadcastvote")) {

            String playerName = args[1];
            BroadcastManager bm = plugin.getBroadcastManager();
            bm.broadcastBungee(playerName, "testService");
        } else {
            Util.sendMessage(sender, "Unknown command!");
        }
    }
}

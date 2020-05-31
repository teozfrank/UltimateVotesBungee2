package net.teozfrank.ultimatevotesbungee.commands;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.teozfrank.ultimatevotesbungee.main.UltimateVotesBungee;
import net.teozfrank.ultimatevotesbungee.util.*;
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
        }
        else if(args.length == 2 && args[0].equals("rewarduuid")) {
            UUID uuid = UUID.fromString(args[1]);
            plugin.getUtil().sendServerMessage(uuid);
            Util.sendMessage(sender, ChatColor.GREEN + "Rewarding UUID " + uuid);
        } else if(args.length == 2 && args[0].equals("reward")) {
            String playerName = args[1];
            UUID uuid = plugin.getDatabaseManager().getUUIDFromUsername(playerName);
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
            DatabaseManager mySql = plugin.getDatabaseManager();
            try {
                UUID uuid = mySql.getUUIDFromUsername(playerName);
                if(uuid == null) {
                    Util.sendMessage(sender, ChatColor.GREEN + "UUID retrieval failed!: " + playerName);
                    return;
                }

                String serverName = null;

                boolean isOnline = false;

                if (plugin.getProxy().getPluginManager().getPlugin("RedisBungee") != null) {
                    if(plugin.isDebugEnabled()) {
                        SendConsoleMessage.debug("RedisBungee is installed on this server, using to identify if player is online");
                    }
                    ServerInfo playersServerInfo = RedisBungee.getApi().getServerFor(uuid);//get server object for redis
                    if (playersServerInfo != null) {
                        isOnline = true;
                        serverName = playersServerInfo.getName();
                        if(plugin.isDebugEnabled()) {
                            SendConsoleMessage.debug("Player with uuid: " +  uuid + "is online");
                        }
                    }   else {
                        if(plugin.isDebugEnabled()) {
                            SendConsoleMessage.debug("Player with uuid: " +  uuid + "is offline");
                        }
                        isOnline = false;
                    }

                } else {
                    try {
                        if(plugin.isDebugEnabled()) {
                            SendConsoleMessage.debug("RedisBungee is not installed on this server, bungee API to identify if player is online");
                        }
                        Server playersServer = plugin.getProxy().getPlayer(uuid).getServer();
                        if(plugin.isDebugEnabled()) {
                            if(playersServer == null) {
                                SendConsoleMessage.debug("Players server is null, not online");
                            }
                        }
                        if(playersServer != null ){
                            serverName = playersServer.getInfo().getName();
                            isOnline = true;
                        }
                    } catch (NullPointerException e) {}
                }

                plugin.getDatabaseManager().addPlayerAllTimeVote(uuid, playerName);
                plugin.getDatabaseManager().addPlayerMonthlyVote(uuid, playerName);
                plugin.getDatabaseManager().addVoteLog(uuid, playerName, "UVTestVote", "127.0.0.1", serverName);
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

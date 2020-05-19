package net.teozfrank.ultimatevotesbungee.events;


import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import net.md_5.bungee.api.config.ServerInfo;
import net.teozfrank.ultimatevotesbungee.main.UltimateVotesBungee;
import net.teozfrank.ultimatevotesbungee.util.*;
import com.vexsoftware.votifier.bungee.events.VotifierEvent;
import com.vexsoftware.votifier.model.Vote;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.UUID;

/**
 * Copyright teozfrank / FJFreelance 2014 All rights reserved.
 */
public class PlayerVote implements Listener {

    public UltimateVotesBungee plugin;

    public PlayerVote(UltimateVotesBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerVoteMade(VotifierEvent e) {

        final DatabaseManager databaseManager = plugin.getDatabaseManager();
        final FileManager fm = plugin.getFileManager();
        final Util util = plugin.getUtil();
        final BroadcastManager bm = plugin.getBroadcastManager();

        final Vote v = e.getVote();
        final String voteUsername = v.getUsername();
        final String service = v.getServiceName();
        final String ipAddress = v.getAddress();

        if (!fm.isMySQLEnabled()) {
            return;
        }

        if(plugin.isDebugEnabled()) {
            SendConsoleMessage.debug("Vote received from username " + voteUsername + " from " + v.getServiceName() + " with IP " + ipAddress);
        }

        if (v.getUsername().length() == 0 || v.getUsername().equalsIgnoreCase("anonymous") || v.getUsername().contains("test")) {
            return;//ignored
        }

        if (!(v.getUsername().equals(""))) {//to prevent votes that put in no username being counted

            if (plugin.getProxy().getPluginManager().getPlugin("RedisBungee") != null) {
                if (fm.isBroadcastRedis()) {//if redis broadcasts are enabled
                    if (fm.isVoteSpamPrevention()) {//if vote spam prevention is enabled
                        if (! util.hasVoted(voteUsername)) {//if the player has not voted
                            bm.broadcastRedis(voteUsername, service);
                        }
                    } else {
                        bm.broadcastRedis(voteUsername, service);
                    }
                }
            }

            plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {

                @Override
                public void run() {
                    UUID playerUUID = databaseManager.getUUIDFromUsername(voteUsername);

                    boolean isOnline = false;

                    if (plugin.getProxy().getPluginManager().getPlugin("RedisBungee") != null) {
                        if(plugin.isDebugEnabled()) {
                            SendConsoleMessage.debug("RedisBungee is installed on this server, using to identify if player is online");
                        }
                        ServerInfo playersServerInfo = RedisBungee.getApi().getServerFor(playerUUID);//get server object for redis
                        if (playersServerInfo != null) {
                            isOnline = true;
                            if(plugin.isDebugEnabled()) {
                                SendConsoleMessage.debug("Player with uuid: " +  playerUUID + "is online");
                            }
                        }   else {
                            if(plugin.isDebugEnabled()) {
                                SendConsoleMessage.debug("Player with uuid: " +  playerUUID + "is offline");
                            }
                            isOnline = false;
                        }

                    } else {
                        try {
                            if(plugin.isDebugEnabled()) {
                                SendConsoleMessage.debug("RedisBungee is not installed on this server, bungee API to identify if player is online");
                            }
                            Server playersServer = plugin.getProxy().getPlayer(playerUUID).getServer();
                            if(plugin.isDebugEnabled()) {
                                if(playersServer == null) {
                                    SendConsoleMessage.debug("Players server is null, not online");
                                }
                            }
                            if(playersServer != null ){
                                isOnline = true;
                            }
                        } catch (NullPointerException e) {}
                    }

                    if(! isOnline && ! plugin.getFileManager().rewardOffline()) {//if the player is not online and we are not rewarding offline
                        if(plugin.isDebugEnabled()) {
                            SendConsoleMessage.debug("Player is not online and reward offline is set to false, vote ignored");
                        }
                        return;// do not reward
                    }

                    if (fm.isBroadcastBungee()) {
                        if (fm.isVoteSpamPrevention()) {//if vote spam prevention is enabled
                            if (! util.hasVoted(voteUsername)) {//if the player has not voted
                                if(fm.isBroacastOnline()) {
                                    if(playerUUID != null) {
                                        bm.broadcastBungee(voteUsername, service);
                                    }
                                } else {
                                    bm.broadcastBungee(voteUsername, service);
                                }
                            }
                        } else {
                            if(fm.isBroacastOnline()) {
                                if(playerUUID != null) {
                                    bm.broadcastBungee(voteUsername, service);
                                }
                            } else {
                                bm.broadcastBungee(voteUsername, service);
                            }
                        }
                    }

                    if (fm.isVoteSpamPrevention() && !util.hasVoted(voteUsername)) {//if vote spam prevention is enabled and the voter has not voted yet.
                        util.addVotedPlayer(voteUsername);
                    }

                    if(playerUUID != null) {
                        if(plugin.isDebugEnabled()) {
                            SendConsoleMessage.debug("Player UUID for player " + voteUsername + " :" + playerUUID.toString());
                        }
                        databaseManager.addPlayerAllTimeVote(playerUUID, voteUsername);
                        databaseManager.addPlayerMonthlyVote(playerUUID, voteUsername);
                        databaseManager.addVoteLog(playerUUID, voteUsername, service, ipAddress);

                        plugin.getUtil().sendServerMessage(playerUUID);
                    } else {
                        if(plugin.isDebugEnabled()) {
                            SendConsoleMessage.debug("Player UUID is null for player " + voteUsername + " ignoring.");
                        }

                    }

                }
            });
        }
    }


}

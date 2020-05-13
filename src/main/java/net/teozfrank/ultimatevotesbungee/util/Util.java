package net.teozfrank.ultimatevotesbungee.util;

import com.google.common.base.Charsets;
import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import net.teozfrank.ultimatevotesbungee.main.UltimateVotesBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.teozfrank.ultimatevotesbungee.main.UltimateVotesBungee;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

/**
 * Copyright teozfrank / FJFreelance 2014 All rights reserved.
 */
public class Util {

    private UltimateVotesBungee plugin;
    private List<String> votedPlayers;

    public Util(UltimateVotesBungee plugin)  {
        this.plugin = plugin;
        this.votedPlayers = new ArrayList<String>();
    }

    /**
     * add a player that has voted to the list
     * @param voterName the voters name
     */
    public void addVotedPlayer(String voterName) {
        if(!getVotedPlayers().contains(voterName)) {
            getVotedPlayers().add(voterName);
        }
    }

    public boolean hasVoted(String voterName) {
        if(getVotedPlayers().contains(voterName)) {
            return true;
        }
        return false;
    }

    public List<String> getVotedPlayers() {
        return votedPlayers;
    }

    public void clearVotedPlayers() {
        getVotedPlayers().clear();
    }

    public static void sendMessage(CommandSender sender, String messageIn) {
        sender.sendMessage(ChatColor.AQUA + "[UltimateVotesBungee] " + messageIn);
    }

    public static void sendEmptyMessage(CommandSender sender, String messageIn) {
        sender.sendMessage(messageIn);
    }

    /**
     * attempts to send a plugin message to the server that a player is online on
     * @param playerUUID the players UUID
     */
    public void sendServerMessage(UUID playerUUID) {

        if(playerUUID == null) {
            if(plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("Player uuid is null, not sending server message.");
            }
            return;
        }
        //TODO sudo code for this,
        //identify players server, if server is on another instance, send message to that instance
        //if server is on same instance send plugin message locally
        //
        if (plugin.getProxy().getPluginManager().getPlugin("RedisBungee") != null) {

            try {
                ServerInfo playersServerInfo = RedisBungee.getApi().getServerFor(playerUUID);//get server object for redis
                if(playersServerInfo == null) {
                    if(plugin.isDebugEnabled()) {
                        SendConsoleMessage.debug("player with uuid: " + playerUUID + " is offline.");
                    }
                    return;
                }
                if(plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("player with uuid: " + playerUUID + " is online.");
                }
                String serverName = playersServerInfo.getName();//get the server name
                if(plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("Player with uuid "+ playerUUID + " server name: " + serverName);
                }
                ServerInfo localProxyServer = plugin.getProxy().getServerInfo(serverName);//try resolve to local servers

                if(localProxyServer == null ){// if the local proxy server is null, that means the player server is on another instance,
                    // we need to send to redis api
                    //TODO get this working!
                    sendPluginMessageRedis(playerUUID);
                    return;
                } else {
                    if(plugin.isDebugEnabled()) {
                        SendConsoleMessage.debug("Player with uuid " + playerUUID
                                + " is not part of local servers of this proxy sending local plugin message.");
                    }
                }
            } catch (NullPointerException e) {
                SendConsoleMessage.error("Error Sending Server Message: " + e.getMessage());
            }


        }
        try {
            if(plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("Sending local proxy message for uuid: " + playerUUID);
            }
            Server playersServer = plugin.getProxy().getPlayer(playerUUID).getServer();
            sendPluginMessage(playerUUID, playersServer);
        } catch (NullPointerException e) {
            SendConsoleMessage.error("Null when trying to resolve players server are they online?" + e.getMessage());
        }
    }

    public Server getPlayersServerByUUID(UUID playerUUID, Collection<ProxiedPlayer> playerCollection) {

        for(ProxiedPlayer player: playerCollection) {
            if(player.getUniqueId().equals(playerUUID)) {
                return player.getServer();
            }
        }
        return null;
    }

    public void sendPluginMessage(UUID playerUUID, Server playersServer){
        if(plugin.isDebugEnabled()) {
            SendConsoleMessage.debug("Players server for " + playerUUID+ " is: " + playersServer);
        }
        String serverName = playersServer.getInfo().getName();
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
            out.writeUTF("uv:rewards");
            out.writeUTF(playerUUID.toString());
            playersServer.sendData("uv:rewards", b.toByteArray());
            if(plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("Sending UUID: " + playerUUID );
            }
        } catch (IOException e) {
            SendConsoleMessage.error("Error sending plugin message: " + e.getMessage());
        }
    }

    public void sendPluginMessageRedis(UUID playerUUID) {
        if(plugin.isDebugEnabled()) {
            SendConsoleMessage.debug("Send plugin message redis with uuid: " + playerUUID);
        }
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
            out.writeUTF("uv:rewards");
            out.writeUTF(playerUUID.toString());
            if(plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("Send channel message to redis for uuid: " + playerUUID);
                SendConsoleMessage.debug("Channel: uv:rewards");
                SendConsoleMessage.debug("Bytes: " + b.toString());
            }
            //TODO see how this works?
            RedisBungee.getApi().sendChannelMessage("uv:rewards", b.toString());
            if(plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("Sending UUID: " + playerUUID );
            }
        } catch (IOException e) {
            SendConsoleMessage.error("Error sending plugin message: " + e.getMessage());
        }
    }

    public static UUID getOfflineUUID(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charsets.UTF_8));
    }

    public static void broadcast(String message) {
        ProxyServer.getInstance().broadcast(message);
    }

    public static void broadcast(TextComponent message) {
        ProxyServer.getInstance().broadcast(message);
    }
}

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
        if (plugin.getProxy().getPluginManager().getPlugin("RedisBungee") != null) {

            try {
                ServerInfo playersServerInfo = RedisBungee.getApi().getServerFor(playerUUID);
                Server playersServer = getPlayersServerByUUID(playerUUID, playersServerInfo.getPlayers());
                sendPluginMessage(playerUUID, playersServer);
            } catch (NullPointerException e) {
                SendConsoleMessage.severe(e.getMessage());
            }

            return;
        }
        try {
            Server playersServer = plugin.getProxy().getPlayer(playerUUID).getServer();
            sendPluginMessage(playerUUID, playersServer);
        } catch (NullPointerException e) {
            SendConsoleMessage.severe("Null when trying to resolve players server are they online?" + e.getMessage());
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
            SendConsoleMessage.severe(e.getMessage());
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

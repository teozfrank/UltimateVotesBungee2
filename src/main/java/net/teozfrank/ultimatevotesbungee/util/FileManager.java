package net.teozfrank.ultimatevotesbungee.util;

import net.teozfrank.ultimatevotesbungee.main.UltimateVotesBungee;
import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.ChatColor;

import java.io.File;

/**
 * Copyright teozfrank / FJFreelance 2014 All rights reserved.
 */
public class FileManager extends Config {

    public FileManager(UltimateVotesBungee plugin) {
        this.CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");
        this.CONFIG_HEADER = new String[] {"Configuration file for UltimateVotesBungee", "Copyright (c) teozfrank 2014 - 2017 ,All Rights Reserved", "This plugin is a purchase from SpigotMC"
        ,"If you did not purchase this please contact the owner, distribution of this plugin is against the terms of this plugin."};
        try {
            init();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Comment("config version, dont touch this as it will break the plugin!")
    private double configVersion = 1.6;

    @Comment("is mysql enabled")
    private boolean MySQLEnabled = false;

    @Comment("mysql host")
    private String MySQLHost = "localhost";

    @Comment("mysql port")
    private String MySQLPort = "3306";

    @Comment("mysql database")
    private String MySQLDatabase = "ultimatevotes";

    @Comment("mysql user")
    private String MySQLUser = "root";

    @Comment("mysql password")
    private String MySQLPass = "password";

    @Comment("Should we prevent vote spam prevention by only broadcasting once for each player? (if enabled)")
    private boolean voteSpamPrevention = true;

    @Comment("The time in minutes that the vote spam prevention clears the players that have already voted, to allow vote broadcasts to be sent for them again.")
    private int voteSpamPreventionTimeout = 5;

    @Comment("Should we broadcast a vote message to all servers on the bungeecord instance when a player votes?")
    private boolean broadcastBungee = true;

    @Comment("Should we only broadcast when a player is online?")
    private boolean broadcastOnline = false;

    @Comment("vote broadcast message")
    private String broadcastBungeeMessage = "&b%player% &aVoted for the server! Use /claim!";

    @Comment("vote broadcast hover message")
    private String broadcastBungeeHoverMessage = "&aVote for the server just like &b%player% &adid!";

    @Comment("vote broadcast hover message")
    private String broadcastBungeeClickCommand = "vote sites";

    @Comment("Should we broadcast a vote broadcast command to all servers on all bungeecord instances when a player votes? (requires redis bungee installed and configured)")
    private boolean broadcastRedis = true;

    @Comment("vote broadcast message")
    private String broadcastRedisCmd = "uv broadcast &b%player% &aVoted for the server from &b%service%! &aUse /claim!";

    @Comment("is debug mode enabled")
    private boolean debugEnabled = false;

    @Comment("Maintain connection to the database?")
    private boolean maintainConnection = true;

    @Comment("Should we reward or count votes for offline rewards? If set to false, players that are not online will not be rewarded for voting nor will there votes be counted")
    private boolean rewardOffline = true;

    public double getConfigVersion() {
        return configVersion;
    }

    public boolean isMySQLEnabled() {
        return MySQLEnabled;
    }

    public String getMySQLHost() {
        return MySQLHost;
    }

    public String getMySQLPort() {
        return MySQLPort;
    }

    public String getMySQLDatabase() {
        return MySQLDatabase;
    }

    public String getMySQLUser() {
        return MySQLUser;
    }

    public String getMySQLPass() {
        return MySQLPass;
    }

    public boolean isBroadcastBungee() {
        return broadcastBungee;
    }

    public String getBroadcastBungeeMessage() {
        return ChatColor.translateAlternateColorCodes('&', broadcastBungeeMessage);
    }

    public String getBroadcastBungeeHoverMessage() {
        return ChatColor.translateAlternateColorCodes('&', broadcastBungeeHoverMessage);
    }

    public String getBroadcastBungeeClickCommand() {
        return ChatColor.translateAlternateColorCodes('&', broadcastBungeeClickCommand);
    }

    public boolean isBroadcastRedis() {
        return broadcastRedis;
    }

    public String getBroadcastRedisCmd() {
        return ChatColor.translateAlternateColorCodes('&', broadcastRedisCmd);
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public boolean isVoteSpamPrevention() {
        return voteSpamPrevention;
    }

    public int getVoteStamPreventionTimeout() {
        return voteSpamPreventionTimeout;
    }

    public boolean isBroacastOnline() { return broadcastOnline;}

    public boolean isMaintainConnection() { return maintainConnection; }

    public boolean rewardOffline() { return rewardOffline; }
}

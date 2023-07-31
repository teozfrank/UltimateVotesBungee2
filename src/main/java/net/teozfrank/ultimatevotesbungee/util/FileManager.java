package net.teozfrank.ultimatevotesbungee.util;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.teozfrank.ultimatevotesbungee.main.UltimateVotesBungee;

import java.io.File;
import java.io.IOException;

public class FileManager {

    private UltimateVotesBungee plugin;
    private File configFile;
    private Configuration config;

    public FileManager(UltimateVotesBungee plugin) {
        this.plugin = plugin;
        setupConfig();
        loadConfig();
    }

    private void setupConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");

        if(!configFile.exists()) {
            try {
                SendConsoleMessage.info("creating config.yml as none was found.");
                configFile.createNewFile();
            } catch (IOException e) {
                SendConsoleMessage.error("Failed to create config file: " + e.getMessage());
            }
        }
    }

    public void loadConfig(){
        try {
            SendConsoleMessage.info("loading config.yml.");
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            SendConsoleMessage.error("Failed to load config file: " + e.getMessage());
        }
    }

    public double getConfigVersion(){
        return config.getDouble("configVersion");
    }

    public boolean isMySQLEnabled() {
        return config.getBoolean("MySQLEnabled");
    }

    public String getMySQLHost() {
        return config.getString("MySQLHost");
    }

    public String getMySQLPort() {
        return config.getString("MySQLPort");
    }

    public String getMySQLDatabase() {
        return config.getString("MySQLDatabase");
    }

    public String getMySQLUser() {
        return config.getString("MySQLUser");
    }

    public String getMySQLPass() {
        return config.getString("MySQLPass");
    }

    public boolean isVoteSpamPrevention() {
        return config.getBoolean("voteSpamPrevention");
    }

    public int getVoteSpamPreventionTimeout() {
        return config.getInt("voteSpamPreventionTimeout");
    }

    public boolean isBroadcastBungee() {
        return config.getBoolean("broadcastBungee");
    }

    public boolean isBroadcastOnline() {
        return config.getBoolean("broadcastOnline");
    }

    public String getBroadcastBungeeMessage() {
        return config.getString("broadcastBungeeMessage");
    }

    public String getBroadcastBungeeHoverMessage() {
        return config.getString("broadcastBungeeHoverMessage");
    }

    public String getBroadcastBungeeClickCommand() {
        return config.getString("broadcastBungeeClickCommand");
    }

    public boolean isBroadcastRedis() {
        return config.getBoolean("broadcastRedis");
    }

    public String getBroadcastRedisCmd() {
        return config.getString("broadcastRedisCmd");
    }

    public boolean isDebugEnabled() {
        return config.getBoolean("debugEnabled");
    }

    public boolean isMaintainConnection() {
        return config.getBoolean("maintainConnection");
    }

    public boolean isRewardOffline() {
        return config.getBoolean("rewardOffline");
    }
}

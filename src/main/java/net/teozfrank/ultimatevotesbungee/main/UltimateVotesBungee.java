package net.teozfrank.ultimatevotesbungee.main;

import net.teozfrank.ultimatevotesbungee.commands.UVBExecutor;
import net.teozfrank.ultimatevotesbungee.events.PlayerLogin;
import net.teozfrank.ultimatevotesbungee.events.PlayerVote;
import net.teozfrank.ultimatevotesbungee.util.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.TimeUnit;

/**
 * Copyright teozfrank / FJFreelance 2014 All rights reserved.
 */
public class UltimateVotesBungee extends Plugin {

    private DatabaseManager mySql;
    private FileManager fileManager;
    private BroadcastManager broadcastManager;
    private Util util;

    @Override
    public void onEnable() {
        if(this.getDescription().getVersion().contains("dev")) {
            SendConsoleMessage.warning("---------------------------------------------");
            SendConsoleMessage.warning("This is a development version of UltimateVotes, "
                    + "it is recommended to backup your entire UltimateVotes plugin folder and database before running this build.");
            SendConsoleMessage.warning("This version is also not intended to be used on a live production server, use at your own risk.");
            SendConsoleMessage.warning("---------------------------------------------");
        }
        this.fileManager = new FileManager(this);
        this.mySql = new DatabaseManager(this);
        this.util = new Util(this);
        this.broadcastManager = new BroadcastManager(this);

        if(this.getProxy().getPluginManager().getPlugin("NuVotifier") != null ||
                this.getProxy().getPluginManager().getPlugin("BungeeVote") != null) {
            this.getProxy().getPluginManager().registerListener(this, new PlayerVote(this));
            this.getProxy().getPluginManager().registerListener(this, new PlayerLogin(this));
        }

        if(getFileManager().getConfigVersion() != 1.6) {
            this.onDisable();
            getProxy().getPluginManager().unregisterListener(new PlayerVote(this));
            getLogger().severe(ChatColor.RED + "Your config.yml is out of date! Please stop the proxy, backup your config then delete it and restart! Listening for votes has been disabled until this is rectified!");
            return;
        }
        if(getFileManager().isVoteSpamPrevention()) {
            SendConsoleMessage.info("Starting clear voter task.");
            getProxy().getScheduler().schedule(this, new Runnable() {

                @Override
                public void run() {
                    if(getFileManager().isDebugEnabled()) {
                        SendConsoleMessage.info("Clearing vote spam prevention cache.");
                    }
                    getUtil().clearVotedPlayers();
                }
            }, getFileManager().getVoteStamPreventionTimeout(), getFileManager().getVoteStamPreventionTimeout(), TimeUnit.MINUTES);
        }
        registerCommands();
        submitStats();
    }

    private void submitStats() {
        Metrics metrics = new Metrics(this, 7320);
    }

    private void registerCommands() {
        getProxy().getPluginManager().registerCommand(this, new UVBExecutor(this));
    }

    public DatabaseManager getDatabaseManager() {
        return mySql;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public boolean isDebugEnabled() {
        return getFileManager().isDebugEnabled();
    }

    public Util getUtil() {
        return util;
    }

    public BroadcastManager getBroadcastManager() {
        return broadcastManager;
    }

}

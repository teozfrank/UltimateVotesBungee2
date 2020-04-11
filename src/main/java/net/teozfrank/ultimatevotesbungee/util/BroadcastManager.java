package net.teozfrank.ultimatevotesbungee.util;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import net.teozfrank.ultimatevotesbungee.main.UltimateVotesBungee;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Copyright teozfrank / FJFreelance 2014 All rights reserved.
 */
public class BroadcastManager {

    private UltimateVotesBungee plugin;

    public BroadcastManager(UltimateVotesBungee plugin) {
        this.plugin = plugin;
    }

    public void broadcastBungee(String voteUsername, String service) {
        FileManager fm = plugin.getFileManager();

        String voteBroadcast = fm.getBroadcastBungeeMessage();
        String voteBroadcastHover = fm.getBroadcastBungeeHoverMessage();
        String voteBroadcastClick = fm.getBroadcastBungeeClickCommand();

        voteBroadcast = voteBroadcast.replaceAll("%player%", voteUsername);
        voteBroadcast = voteBroadcast.replaceAll("%service%", service);
        voteBroadcastHover = voteBroadcastHover.replaceAll("%player%", voteUsername);

        TextComponent message = new TextComponent(voteBroadcast);
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(voteBroadcastHover).create()));
        if(!voteBroadcastClick.equals("")) {
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + voteBroadcastClick));
        }
        Util.broadcast(message);
        if(plugin.isDebugEnabled()) {
            SendConsoleMessage.debug("Broadcasting vote message.");
        }
    }

    public void broadcastRedis(String voteUsername, String service) {
        FileManager fm = plugin.getFileManager();

        String broadcastVoteCmd = fm.getBroadcastRedisCmd();
        broadcastVoteCmd = broadcastVoteCmd.replaceAll("%player%", voteUsername);
        broadcastVoteCmd = broadcastVoteCmd.replaceAll("%service%", service);

        RedisBungee.getApi().sendProxyCommand(broadcastVoteCmd);

        if(plugin.isDebugEnabled()) {
            plugin.getLogger().info("regis proxy command: " + broadcastVoteCmd);
        }
    }
}

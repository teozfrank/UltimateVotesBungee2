package net.teozfrank.ultimatevotesbungee.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;

/**
 * Created by Frank on 05/01/2015.
 */
public class SendConsoleMessage {

    private static final String prefix = ChatColor.GREEN + "[UltimateVotesBungee] ";
    private static final String info = "[Info] ";
    private static final String severe = ChatColor.YELLOW + "[Severe] ";
    private static final String warning = ChatColor.RED + "[Warning] ";
    private static final String debug = ChatColor.AQUA + "[Debug] ";
    public SendConsoleMessage() {
    }
    public static void info(String message) {
        ProxyServer.getInstance().getLogger().info(prefix + info + message);
    }
    public static void severe(String message) {
        ProxyServer.getInstance().getLogger().info(prefix + severe + message);
    }
    public static void warning(String message) {
        ProxyServer.getInstance().getLogger().info(prefix + warning + message);
    }
    public static void debug(String message){
        ProxyServer.getInstance().getLogger().info(prefix + debug + message);
    }
}

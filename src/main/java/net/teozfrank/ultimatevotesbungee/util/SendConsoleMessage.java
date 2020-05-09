package net.teozfrank.ultimatevotesbungee.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;

/**
 * Created by Frank on 05/01/2015.
 */
public class SendConsoleMessage {

    private static final String prefix = ChatColor.GREEN + "[UltimateVotes] ";
    private static final String debug = ChatColor.AQUA + "[Debug] ";
    private static final String error = ChatColor.RED + "[Error] ";
    private static final String warning = ChatColor.YELLOW + "[Warning] ";
    private static final String info = "[Info] ";

    public SendConsoleMessage() {
    }
    public static void info(String message) {
        ProxyServer.getInstance().getLogger().info(prefix + info + message);
    }
    public static void error(String message) {
        ProxyServer.getInstance().getLogger().info(prefix + error + message);
    }
    public static void warning(String message) {
        ProxyServer.getInstance().getLogger().info(prefix + warning + message);
    }
    public static void debug(String message){
        ProxyServer.getInstance().getLogger().info(prefix + debug + message);
    }
}

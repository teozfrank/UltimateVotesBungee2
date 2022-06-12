package net.teozfrank.ultimatevotesbungee.events;

import net.teozfrank.ultimatevotesbungee.main.UltimateVotesBungee;
import net.teozfrank.ultimatevotesbungee.util.DatabaseManager;
import net.teozfrank.ultimatevotesbungee.util.SendConsoleMessage;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.UUID;

/**
 * Created by Frank on 09/02/2015.
 */
public class PlayerLogin implements Listener {

    public UltimateVotesBungee plugin;

    public PlayerLogin(UltimateVotesBungee plugin) {
        this.plugin = plugin;
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerLogin(LoginEvent e) {

        DatabaseManager mySql = plugin.getDatabaseManager();
        UUID uuid = e.getConnection().getUniqueId();
        String name = e.getConnection().getName();

        if(! mySql.getCachedUUIDs().containsValue(uuid)) {
            if(plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("Adding uuid to uuid cache");
            }
            mySql.getCachedUUIDs().put(name, uuid);
        }

    }
}

package net.teozfrank.ultimatevotesbungee.util;

import net.teozfrank.ultimatevotesbungee.main.UltimateVotesBungee;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * Copyright teozfrank / FJFreelance 2014 All rights reserved.
 */
public class UltimateVotesBungeeAPI {

    public UltimateVotesBungeeAPI() {

    }

    /**
     * get the players monthly votes count
     * accesses the database, should be called async, not on the main server thread!
     * @param playerUUID the players UUID
     * @return the amount of votes a player has for this month
     */
    public static int getPlayerMonthlyVotes(UUID playerUUID) {
        return UltimateVotesBungee.getDatabaseManager().checkUserVotes(playerUUID, "MONTHLYVOTES");
    }

    /**
     * Get the players unclaimed vote count
     * @param playerUUID the players UUID
     * @return the amount of unclaimed votes the player has for this month
     */
    public static int getPlayerUnclaimedVotes(UUID playerUUID) {
        return UltimateVotesBungee.getDatabaseManager().checkUserUnclaimedVotes(playerUUID);
    }

    /**
     * get the players vote count by username
     * accesses the database, should be called async, not on the main server thread!
     * @param playername the players username
     * @return the amount of votes a player has for this month
     */
    public static int getPlayerMonthlyVotes(String playername) {
        return UltimateVotesBungee.getDatabaseManager().checkUserVotes(playername, "MONTHLYVOTES");
    }

    /**
     * get the players total votes in total
     * accesses the database, should be called async, not on the main server thread!
     * @param playerUUID the players UUID
     * @return the amount of votes a player has in total
     */
    public static int getPlayerAllTimeVotes(UUID playerUUID) {
        return UltimateVotesBungee.getDatabaseManager().checkUserVotes(playerUUID, "ALLVOTES");
    }

    /**
     * get the players total votes in total
     * accesses the database, should be called async, not on the main server thread!
     * @param playername the players username
     * @return the amount of votes a player has in total
     */
    public static int getPlayerAllTimeVotes(String playername) {
        return UltimateVotesBungee.getDatabaseManager().checkUserVotes(playername, "ALLVOTES");
    }

    /**
     * has the player voted today
     * accesses the database, should be called async, not on the main server thread!
     * @param playerUUID the player UUID
     * @return true if they have voted, false if not
     */
    public static boolean hasVotedToday(UUID playerUUID) {
        return UltimateVotesBungee.getDatabaseManager().hasVotedToday(playerUUID);
    }

    /**
     * get the top 10 voters from the database
     * accesses the database, should be called async, not on the main server thread!
     * @return the top 10 voters from the database
     */
    public static LinkedHashMap<String, Integer> getTopVotersMonthly() {
        return UltimateVotesBungee.getDatabaseManager().voteMonthly();
    }

    /**
     * get the top 10 voters all time from the database
     * accesses the database, should be called async, not on the main server thread!
     * @return the top 10 voters all time from the database
     */
    public static LinkedHashMap<String, Integer> getTopVotersAllTime() {
        return UltimateVotesBungee.getDatabaseManager().voteAllTime();
    }

    /**
     * get the top monthly voters based on a limit passed in
     * accesses the database, should be called async, not on the main server thread!
     * @param limit the limit of the top voters
     * @return a linked hashmap of the top players
     */
    public static LinkedHashMap<String, Integer> getTopVotersMonthly(int limit) {
        if(limit <= 0) {
            SendConsoleMessage.error("API ACCESS: Top monthly player limit too low! limited value was: " + limit);
            return null;
        }
        return UltimateVotesBungee.getDatabaseManager().voteMonthly(limit);
    }

    /**
     * get the top all time voters based on a limit passed in
     * accesses the database, should be called async, not on the main server thread!
     * @param limit the limit of the top voters
     * @return a linked hashmap of the top players
     */
    public static LinkedHashMap<String, Integer> getTopVotersAllTime(int limit) {
        if(limit <= 0) {
            SendConsoleMessage.error("API ACCESS: Top all time player limit too low! limited value was: " + limit);
            return null;
        }
        return UltimateVotesBungee.getDatabaseManager().voteAllTime(limit);
    }

    /**
     * perform a mysql query on the database of ultimatevotes
     * please remember the structure of the database can change
     * breaking code relying on this, please only use this as
     * as last resort for something the API does not provide
     * accesses the database, should be called async, not on the main server thread!
     * @param query the mysql query
     * @return the resulting resultset of the database query
     */
    public static ResultSet performDatabaseQuery(String query) {
        try {
            Connection connection = UltimateVotesBungee.getDatabaseManager().getConnection();
            Statement statement = connection.createStatement();
            return statement.executeQuery(query);
        } catch (SQLException e) {
            SendConsoleMessage.error("API ACCESS QUERY ERROR: " + e.getMessage());
        }
        return null;
    }
}

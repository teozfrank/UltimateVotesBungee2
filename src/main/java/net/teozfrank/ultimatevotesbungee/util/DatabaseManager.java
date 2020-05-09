package net.teozfrank.ultimatevotesbungee.util;


import net.teozfrank.ultimatevotesbungee.main.UltimateVotesBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.*;
import java.util.*;
import java.util.List;

public class DatabaseManager {

    private UltimateVotesBungee plugin;
    private Connection connection;//connection to the database
    private HashMap<String, UUID> cachedUUIDs;
    private List<UUID> uuidList;
    public Map<UUID, String> uuidToOrigionalName;

    public DatabaseManager(final UltimateVotesBungee plugin) {
        this.plugin = plugin;
        if (plugin.getFileManager().isMySQLEnabled()) {
            plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
                @Override
                public void run() {
                    setupConnection(); //async connection
                    setupAllVotes();
                    setupMonthlyVotes();
                    setupLastReset();
                    setupTopVoters();
                    setupVoteLog();
                }
            });

        } else {
            SendConsoleMessage.info("DatabaseManager is " + ChatColor.RED + "DISABLED " +
                    ChatColor.GREEN + "Please enable once you have setup the database details.");
        }
        cachedUUIDs = new HashMap<String, UUID>();
        uuidList = new ArrayList<UUID>();
        uuidToOrigionalName = new HashMap<UUID, String>();
    }

    public void addVoteLog(UUID playerUUID, String playerName, String serviceName, String IPAddress) {
        String sql = "INSERT INTO VOTELOG VALUES (NULL, '" + playerUUID +"', '"+ playerName
                + "', '" + serviceName
                + "', '" + IPAddress + "', NULL)";
        boolean success = execute(sql);
        if(! success) {
            SendConsoleMessage.error("Error inserting VoteLog!");
        }
    }

    public void setupVoteLog() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String query = "SHOW TABLES LIKE 'VOTELOG'";
            if(plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("Preparing initial sql statement for VoteLog.");
            }
            PreparedStatement statement = getConnection().prepareStatement(query);
            int i;
            ResultSet result = statement.executeQuery();
            i = 0;
            while (result.next()) {
                i++;
            }
            result.close();
            statement.close();
            if(!connection.isClosed() && !plugin.getFileManager().isMaintainConnection()) {
                if(plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("Closing connection, connection is not being maintained.");
                }
                connection.close();
            }
            if (!(i > 0)) {
                SendConsoleMessage.info("Table VOTELOG does not exist creating it for you!");
                String sql = "CREATE TABLE VOTELOG "
                        + "(ID BIGINT NOT NULL AUTO_INCREMENT UNIQUE,"
                        + " UUID VARCHAR(40), "
                        + " PLAYER VARCHAR(50), "
                        + " SERVICENAME VARCHAR(50),"
                        + " IPADDRESS VARCHAR(30),"
                        + " VOTETIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                        + " PRIMARY KEY ( ID ))";
                statement = getConnection().prepareStatement(sql);
                statement.executeUpdate();
                statement.close();
                if(!connection.isClosed() && !plugin.getFileManager().isMaintainConnection()) {
                    if(plugin.isDebugEnabled()) {
                        SendConsoleMessage.debug("Closing connection, connection is not being maintained.");
                    }
                    connection.close();
                }
            } else {
                if(plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("Table VOTELOG exists, ignoring!");
                }

            }
        } catch (ClassNotFoundException ex) {
            SendConsoleMessage.warning("DatabaseManager driver wasn't found!");
        } catch (Exception e) {
            e.printStackTrace();
            //SendConsoleMessage.warning("DatabaseManager could not establish a connection when trying to setup all votes: " + e.printStackTrace(););
        }
    }


    public void setupTopVoters() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String query = "SHOW TABLES LIKE 'TOPVOTERS'";
            if(plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("Preparing initial sql statement for TopVoters.");
            }
            PreparedStatement statement = getConnection().prepareStatement(query);
            int results;
            ResultSet result = statement.executeQuery();
            results = 0;
            while (result.next()) {
                results++;
            }
            result.close();
            statement.close();
            if(! connection.isClosed() && ! plugin.getFileManager().isMaintainConnection()) {
                if(plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("Closing connection, connection is not being maintained.");
                }
                connection.close();
            }
            if (!(results > 0)) {
                SendConsoleMessage.info("Table TopVoters does not exist creating it for you!");
                String sql = "CREATE TABLE TOPVOTERS "
                        + "(ID MEDIUMINT NOT NULL AUTO_INCREMENT UNIQUE,"
                        + " MONTHYEAR VARCHAR(7), "
                        + " POSITION INT, "
                        + " UUID varchar(40), "
                        + " PLAYERNAME VARCHAR(50), "
                        + " VOTES MEDIUMINT, "
                        + " PRIMARY KEY ( ID ))";
                statement = getConnection().prepareStatement(sql);
                statement.executeUpdate();
                statement.close();
                if(!connection.isClosed() && !plugin.getFileManager().isMaintainConnection()) {
                    if(plugin.isDebugEnabled()) {
                        SendConsoleMessage.debug("Closing connection, connection is not being maintained.");
                    }
                    connection.close();
                }
            } else {
                if(plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("Table TopVoters exists, ignoring!");
                }

            }
        } catch (ClassNotFoundException ex) {
            SendConsoleMessage.warning("DatabaseManager driver wasn't found!");
        } catch (Exception e) {
            e.printStackTrace();
            //SendConsoleMessage.warning("DatabaseManager could not establish a connection when trying to setup all votes: " + e.printStackTrace(););
        }
    }

    /**
     * Add a top voter to the top voters table
     * @param monthYear the month year for the record
     * @param position the position
     * @param uuid the players uuid
     * @param playerName the players name
     * @param voteCount the vote count of the player
     */
    public void addTopVoters(String monthYear, int position, String uuid, String playerName, int voteCount) {
        String sql = "INSERT INTO TOPVOTERS VALUES (NULL, '"
                + monthYear + "', "
                + position + ", '"
                + uuid + "', '"
                + playerName + "', "
                + voteCount + ");";
        if(plugin.isDebugEnabled()) {
            SendConsoleMessage.debug("SQL: " + sql);
        }
        boolean success = execute(sql);
        if(plugin.isDebugEnabled()) {
            if(success) {
                SendConsoleMessage.debug("Top voter insert successful");
            }

        }
    }

    public void setupConnection() {
        try {
            String MySqlHost = plugin.getFileManager().getMySQLHost();
            String MySqlPort = plugin.getFileManager().getMySQLPort();
            String MySqlDatabase = plugin.getFileManager().getMySQLDatabase();
            String MySqlUsername = plugin.getFileManager().getMySQLUser();
            String MySqlPassword = plugin.getFileManager().getMySQLPass();

            SendConsoleMessage.info("Attempting to connect to MySQL database.");
            Connection sqlDatabaseConnection;
            if (plugin.getFileManager().isMaintainConnection()) {
                sqlDatabaseConnection = DriverManager.getConnection("jdbc:mysql://" + MySqlHost + ":" + MySqlPort + "/" + MySqlDatabase + "?autoReconnect=true&useSSL=false", MySqlUsername, MySqlPassword);
                SendConsoleMessage.info("Connection to MySQL database successful.");
                this.connection = sqlDatabaseConnection;
            } else {
                sqlDatabaseConnection = DriverManager.getConnection("jdbc:mysql://" + MySqlHost + ":" + MySqlPort + "/" + MySqlDatabase + "?useSSL=false", MySqlUsername, MySqlPassword);
                SendConsoleMessage.info("Connection to MySQL database successful.");
                this.connection = sqlDatabaseConnection;
            }

        } catch (SQLException e) {
            SendConsoleMessage.warning("DatabaseManager could not establish a connection when trying to setup the connection!" + e);
        }
    }

    public Connection getConnection() {
        try {
            if (! connection.isClosed()) {
                if (plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("SQL Connection is still active, using it!.");
                }
                return connection;
            }
        } catch (SQLException e) {
            SendConsoleMessage.error("SQL Error! " + e.getMessage());
        } catch (NullPointerException e) {

        }
        if (plugin.isDebugEnabled()) {
            SendConsoleMessage.debug("SQL Connection is null creating new connection.");
        }
        setupConnection();
        return connection;
    }

    public void setupAllVotes() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String query = "SHOW TABLES LIKE 'ALLVOTES'";
            if(plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("Preparing initial sql statement for AllVotes.");
            }
            PreparedStatement statement = getConnection().prepareStatement(query);
            int i;
            ResultSet result = statement.executeQuery();
            i = 0;
            while (result.next()) {
                i++;
            }
            result.close();
            statement.close();
            if(!connection.isClosed() && !plugin.getFileManager().isMaintainConnection()) {
                if(plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("Closing connection, connection is not being maintained.");
                }
                connection.close();
            }
            if (!(i > 0)) {
                SendConsoleMessage.info("Table ALLVOTES does not exist creating it for you!");
                String sql = "CREATE TABLE ALLVOTES "
                        + "(ID MEDIUMINT NOT NULL AUTO_INCREMENT UNIQUE,"
                        + " UUID VARCHAR(40) UNIQUE, "
                        + " PLAYER VARCHAR(50) UNIQUE, "
                        + " VOTES MEDIUMINT ,"
                        + " LASTVOTE DATE,"
                        + " PRIMARY KEY ( ID ))";
                statement = getConnection().prepareStatement(sql);
                statement.executeUpdate();
                statement.close();
                if(!connection.isClosed() && !plugin.getFileManager().isMaintainConnection()) {
                    if(plugin.isDebugEnabled()) {
                        SendConsoleMessage.debug("Closing connection, connection is not being maintained.");
                    }
                    connection.close();
                }
            } else {
                if(plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("Table ALLVOTES exists, ignoring!");
                }

            }
        } catch (ClassNotFoundException ex) {
            SendConsoleMessage.warning("DatabaseManager driver wasn't found!");
        } catch (Exception e) {
            e.printStackTrace();
            //SendConsoleMessage.warning("DatabaseManager could not establish a connection when trying to setup all votes: " + e.printStackTrace(););
        }
    }

    public void setupMonthlyVotes() {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            String query = "SHOW TABLES LIKE 'MONTHLYVOTES'";
            if(plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("Preparing initial sql statement for MonthlyVotes.");
            }
            PreparedStatement statement = getConnection().prepareStatement(query);
            int i;
            ResultSet result = statement.executeQuery();
            i = 0;
            while (result.next()) {
                i++;
            }
            result.close();
            statement.close();
            if(!connection.isClosed() && !plugin.getFileManager().isMaintainConnection()) {
                if(plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("Closing connection, connection is not being maintained.");
                }
                connection.close();
            }
            if (!(i > 0)) {
                SendConsoleMessage.info("Table MONTHLYVOTES does not exist creating it for you!");
                String sql = "CREATE TABLE MONTHLYVOTES "
                        + "(ID MEDIUMINT NOT NULL AUTO_INCREMENT UNIQUE,"
                        + " UUID VARCHAR(40) UNIQUE, "
                        + " PLAYER VARCHAR(50) UNIQUE, "
                        + " VOTES MEDIUMINT ,"
                        + "UNCLAIMEDVOTES MEDIUMINT,"
                        + " LASTVOTE DATE,"
                        + " PRIMARY KEY ( ID ))";
                statement = getConnection().prepareStatement(sql);
                statement.executeUpdate();
                statement.close();
                if(!connection.isClosed() && !plugin.getFileManager().isMaintainConnection()) {
                    if(plugin.isDebugEnabled()) {
                        SendConsoleMessage.debug("Closing connection, connection is not being maintained.");
                    }
                    connection.close();
                }
            } else {
                if(plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("Table MONTHLYVOTES exists, ignoring!");
                }
            }
        } catch (ClassNotFoundException ex) {
            SendConsoleMessage.warning("DatabaseManager driver wasn't found!");
        } catch (Exception e) {
            SendConsoleMessage.warning("DatabaseManager could not establish a connection when trying to setup monthly votes: " + e);
        }
    }

    public void setupLastReset() {
        try {

            Class.forName("com.mysql.jdbc.Driver");
            String query = "SHOW TABLES LIKE 'LASTRESET'";
            if(plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("Preparing initial sql statement for LastReset.");
            }
            PreparedStatement statement = getConnection().prepareStatement(query);
            int i;
            ResultSet result = statement.executeQuery();
            i = 0;
            while (result.next()) {
                i++;
            }
            result.close();
            statement.close();
            if(!connection.isClosed() && !plugin.getFileManager().isMaintainConnection()) {
                if(plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("Closing connection, connection is not being maintained.");
                }
                connection.close();
            }
            if (!(i > 0)) {
                SendConsoleMessage.info("Table LASTRESET does not exist creating it for you!");
                String sql = "CREATE TABLE LASTRESET "
                        + "(ID MEDIUMINT NOT NULL AUTO_INCREMENT,"
                        + " TABLENAME VARCHAR(30), "
                        + " LASTRESET DATE,"
                        + " PRIMARY KEY ( ID ))";
                statement = getConnection().prepareStatement(sql);
                statement.executeUpdate();
                statement.close();
                if(!connection.isClosed() && !plugin.getFileManager().isMaintainConnection()) {
                    if(plugin.isDebugEnabled()) {
                        SendConsoleMessage.debug("Closing connection, connection is not being maintained.");
                    }
                    connection.close();
                }
                setInitialLastReset("MONTHLYVOTES");

            }

        } catch (ClassNotFoundException ex) {
            SendConsoleMessage.warning("DatabaseManager driver wasn't found!");
        } catch (Exception e) {
            SendConsoleMessage.warning("DatabaseManager could not establish a connection when trying to setup last reset table: " + e);
        }
    }

    public void setupDailyTarget() {
        try {

            Class.forName("com.mysql.jdbc.Driver");
            SendConsoleMessage.info("Attempting to connect to DatabaseManager.");
            String query = "SHOW TABLES LIKE 'DAILYTARGET'";
            PreparedStatement statement = getConnection().prepareStatement(query);
            int i;
            ResultSet result = statement.executeQuery();
            i = 0;
            while (result.next()) {
                i++;
            }
            result.close();
            statement.close();
            if (!(i > 0)) {
                SendConsoleMessage.info("Table DAILYTARGET does not exist creating it for you!");
                String sql = "CREATE TABLE DAILYTARGET "
                        + "(ID MEDIUMINT NOT NULL AUTO_INCREMENT,"
                        + " VOTES MEDIUMINT ,"
                        + " LASTREACHED DATE,"
                        + " LASTRESET DATE,"
                        + " PRIMARY KEY ( ID ))";
                statement = getConnection().prepareStatement(sql);
                int success = statement.executeUpdate();
                if (success == 1) {
                    SendConsoleMessage.info("Daily target table creation successful.");
                }
                statement.close();

                java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
                String sql2 = "INSERT INTO DAILYTARGET VALUES (NULL, '0', NULL, '" + date + "')";
                if (plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("inserting default daily target value: " + sql2);
                }
                try {
                    Statement statement2 = getConnection().createStatement();
                    statement2.executeUpdate(sql2);
                    statement2.close();
                } catch (SQLException e) {
                    plugin.getLogger().severe("Error inserting default daily target values!" + e);
                }
                if(!plugin.getFileManager().isMaintainConnection()) {
                    connection.close();
                }
            }

        } catch (ClassNotFoundException ex) {
            SendConsoleMessage.warning("DatabaseManager driver wasn't found!");
        } catch (Exception e) {
            SendConsoleMessage.warning("DatabaseManager could not establish a connection: " + e);
        }
    }

    public int getDailyVoteTargetCount() {
        String query = "SELECT VOTES FROM DAILYTARGET WHERE ID ='1'";

        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("getting daily target vote count: " + query);
        }
        int voteCount = 0;
        int rows = 0;

        try {
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                voteCount = result.getInt("VOTES");
                rows++;
            }
            result.close();
            statement.close();
            if (!(rows > 1)) {
                return voteCount;
            }
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL Error!" + e);
        }
        return -1;//error
    }

    public java.sql.Date getDailyVoteTargetLastReset() {
        String query = "SELECT LASTRESET FROM DAILYTARGET WHERE ID ='1'";

        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("getting vote target last reset date: " + query);
        }
        java.sql.Date lastReset = null;
        int rows = 0;

        try {
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                lastReset = result.getDate("LASTRESET");
                rows++;
            }
            result.close();
            statement.close();
            if (!(rows > 1)) {
                return lastReset;
            }
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL Error while getting last reset date!" + e);
        }
        return null;//error
    }

    public java.sql.Date getDailyVoteTargetLastReached() {
        String query = "SELECT LASTREACHED FROM DAILYTARGET WHERE ID ='1'";

        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("getting vote target last reset date: " + query);
        }
        java.sql.Date lastReset = null;
        int rows = 0;

        try {
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                lastReset = result.getDate("LASTREACHED");
                rows++;
            }
            result.close();
            statement.close();
            if (!(rows > 1)) {
                if (lastReset == null) {
                    return null;
                }
                return lastReset;
            }
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL Error while retrieving last reached target!" + e);
        }
        return null;//error
    }

    public void addVoteToDailyTarget() {
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        String query = "SELECT VOTES FROM DAILYTARGET WHERE ID ='1'";
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("Adding vote to daily target: " + query);
        }
        int voteCount = 0;
        int unclaimedCount = 0;
        int rows = 0;

        try {
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                voteCount = result.getInt("VOTES");
                rows++;
            }
            result.close();
            statement.close();
            if (!(rows > 1)) {
                int newVoteValue = voteCount + 1;
                String sql = "UPDATE DAILYTARGET SET VOTES ='" + newVoteValue + "' WHERE ID='1'";
                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().info(sql);
                }
                Statement statement2 = this.getConnection().createStatement();
                statement2.executeUpdate(sql);
                statement2.close();
            }
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL Error while adding daily vote! " + e);
        }
    }

    public void resetDailyVoteTarget(boolean targetReached) {
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        String sql = "";
        if (targetReached) {//if the vote target is reached
            sql = "UPDATE DAILYTARGET SET VOTES ='0', LASTREACHED ='" + date + "', LASTRESET = '" + date + "'";
        } else {
            sql = "UPDATE DAILYTARGET SET VOTES ='0', LASTRESET = '" + date + "'";
        }
        try {
            Statement statement = this.getConnection().createStatement();
            statement.executeUpdate(sql);
            statement.close();
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            SendConsoleMessage.error("SQL ERROR while resetting daily vote target! " + e);
        }
    }

    private int insertUUIDs(Map<String, UUID> playerData, String tableName) {

        int failedUpdates = 0;
        for (Map.Entry<String, UUID> entry : playerData.entrySet()) {
            String sql = "UPDATE " + tableName + " SET `UUID`='" + entry.getValue() + "' WHERE `PLAYER`='" + entry.getKey() + "'";
            if (plugin.isDebugEnabled()) {
                SendConsoleMessage.debug(sql);
            }
            boolean success = execute(sql);
            if (!success) {
                SendConsoleMessage.error("UUID update failed for player " + entry.getKey());
                failedUpdates++;
            }
            if (!uuidList.contains(entry.getValue())) {
                uuidList.add(entry.getValue());
            }

        }
        return failedUpdates;
    }

    public boolean execute(String sql) {
        try {
            Statement statement = getConnection().createStatement();
            statement.executeUpdate(sql);
            statement.close();
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
            return true;
        } catch (SQLException e) {
            SendConsoleMessage.error("Error executing query: " + sql +" Error: "+ e.getMessage());
            return false;
        }
    }

    public HashMap<String, UUID> getCachedUUIDs() {
        return cachedUUIDs;
    }

    public UUID getUUIDFromUsername(String username) {
        if(plugin.isDebugEnabled()) {
            SendConsoleMessage.debug("Get uuid from username: " + username);
        }
        UUID uuid = null;

        if(plugin.isDebugEnabled()) {
            SendConsoleMessage.debug("Trying cache to retrieve uuid.");
        }
        if (cachedUUIDs.containsKey(username)) {
            if (plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("Player uuid is cached using: " + cachedUUIDs.get(username));
            }

            return cachedUUIDs.get(username);
        }

        ProxiedPlayer player = plugin.getProxy().getPlayer(username);
        if(plugin.isDebugEnabled()) {
            SendConsoleMessage.debug("Trying to check if player is online to retrieve uuid.");
        }
        if (player != null) {
            if (plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("Player is online retrieving UUID.");
            }
            UUID playerUUID = player.getUniqueId();
            if (!cachedUUIDs.containsKey(username)) {
                cachedUUIDs.put(username, playerUUID);
            }
            if(plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("Retrieved cached UUID is: " + playerUUID);
            }
            return playerUUID;
        }

        String sql = "SELECT UUID FROM ALLVOTES WHERE PLAYER='" + username + "'";
        int results = 0;
        if(plugin.isDebugEnabled()) {
            SendConsoleMessage.debug("Trying to check allvotes database table to see if player has voted before.");
        }
        try {
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(sql);

            while (result.next()) {
                uuid = UUID.fromString(result.getString("UUID"));
                if (!cachedUUIDs.containsKey(username)) {
                    cachedUUIDs.put(username, uuid);
                }
                results++;
            }
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            SendConsoleMessage.error("SQL Error getting uuid from username! " + e.getMessage());
        }

        if (results == 0) {

            if (plugin.getProxy().getConfig().isOnlineMode()) {
                if (plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("UUID is not in database and player is offline, contacting Mojang.");
                }
                UUIDFetcher uuidFetcher = new UUIDFetcher(username);
                try {
                    UUID returnedUUID = uuidFetcher.call();
                    uuid = returnedUUID;
                    if (!cachedUUIDs.containsKey(username)) {
                        cachedUUIDs.put(username, uuid);
                    }
                    return uuid;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("UUID resolved from Mojang: " + uuid);
                }
                return uuid;
            }
            else {

                if (!cachedUUIDs.containsKey(username)) {
                    cachedUUIDs.put(username, uuid);
                }
                if(plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("Using offline uuid.");
                }
                return Util.getOfflineUUID(username);
            }
        }
        return uuid;
    }

    public void addPlayerMonthlyVote(UUID playerUUID, String playerName) {
        if (playerUUID == null) {
            if (plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("UUID is null ignoring for vote username " + playerName);
            }
            return;
        }
        String query = "SELECT VOTES FROM MONTHLYVOTES WHERE UUID='" + playerUUID + "'";
        if (plugin.isDebugEnabled()) {
            SendConsoleMessage.debug("Add Player Monthly Vote check uuid: " + query);
        }
        int p = 0;

        try {
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                p++;
            }

            if (p == 0) {
                String uuid = "";
                if(plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("No results found by uuid!");
                }
                int result2Count = 0;
                String queryCheckPlayerName = "SELECT VOTES,UUID FROM MONTHLYVOTES WHERE PLAYER='" + playerName + "'";
                Statement statement2 = getConnection().createStatement();
                ResultSet result2 = statement2.executeQuery(queryCheckPlayerName);

                if(plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("Add Player Monthly Vote check username: " + queryCheckPlayerName);
                }

                while(result2.next()) {
                    uuid = result2.getString("UUID");
                    result2Count++;
                }

                if(result2Count == 1) {
                    if(plugin.isDebugEnabled()) {
                        SendConsoleMessage.debug("UUID of player: " + playerUUID);
                        SendConsoleMessage.debug("Found result by username with uuid: " + uuid);
                    }
                    if(! playerUUID.equals(uuid)) {
                        if(plugin.isDebugEnabled()) {
                            SendConsoleMessage.debug("UUID mismatch of MonthlyVotes, updating uuid of player to: " + playerUUID);
                        }
                        String sql = "UPDATE MONTHLYVOTES SET UUID = '" + playerUUID + "' WHERE UUID='" + uuid + "'";
                        if(plugin.isDebugEnabled()) {
                            SendConsoleMessage.debug("Update uuid query: " + sql);
                        }
                        boolean success = execute(sql);
                        if(success) {
                            if(plugin.isDebugEnabled()) {
                                SendConsoleMessage.debug("UUID successfully updated.");
                                this.updateExistingMonthlyVoteRecord(playerUUID, playerName);
                            } else {
                                SendConsoleMessage.error("UUID was not updated successfully please check logs!!");
                            }
                        }
                    }
                } else {
                    if(plugin.isDebugEnabled()) {
                        SendConsoleMessage.debug("No result by username creating new monthly vote record!");
                    }
                    this.createNewMonthlyVoteRecord(playerUUID, playerName);
                }

            } else if (p == 1) {
                this.updateExistingMonthlyVoteRecord(playerUUID, playerName);
            } else {
                plugin.getLogger().severe("more than one record was found for a player!! failed to register vote!");
            }
            result.close();
            statement.close();

            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("error adding all time player vote!!" + e);
        }
    }

    public void addPlayerAllTimeVote(UUID playerUUID, String playerName) {
        if (playerUUID == null) {
            if (plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("UUID is null ignoring for vote username " + playerName);
            }
            return;
        }
        String query = "SELECT VOTES FROM ALLVOTES WHERE UUID='" + playerUUID + "'";
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("Add Player AllTime Vote: " + query);
        }
        int p = 0;

        try {
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                p++;
            }

            if (p == 0) {
                String uuid = "";
                if(plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("No results found by uuid!");
                }
                int result2Count = 0;
                String queryCheckPlayerName = "SELECT VOTES,UUID FROM ALLVOTES WHERE PLAYER='" + playerName + "'";
                Statement statement2 = getConnection().createStatement();
                ResultSet result2 = statement2.executeQuery(queryCheckPlayerName);

                if(plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("Add Player Monthly Vote check username: " + queryCheckPlayerName);
                }

                while(result2.next()) {
                    uuid = result2.getString("UUID");
                    result2Count++;
                }

                if(result2Count == 1) {
                    if(plugin.isDebugEnabled()) {
                        SendConsoleMessage.debug("UUID of player: " + playerUUID);
                        SendConsoleMessage.debug("Found result by username with uuid: " + uuid);
                    }
                    if(! playerUUID.equals(uuid)) {
                        if(plugin.isDebugEnabled()) {
                            SendConsoleMessage.debug("UUID mismatch of AllVotes, updating uuid of player to: " + playerUUID);
                        }
                        String sql = "UPDATE ALLVOTES SET UUID = '" + playerUUID + "' WHERE UUID='" + uuid + "'";
                        if(plugin.isDebugEnabled()) {
                            SendConsoleMessage.debug("Update uuid query: " + sql);
                        }
                        boolean success = execute(sql);
                        if(success) {
                            if(plugin.isDebugEnabled()) {
                                SendConsoleMessage.debug("UUID successfully updated.");
                                this.updateExistingAllTimeVoteRecord(playerUUID, playerName);
                            } else {
                                SendConsoleMessage.error("UUID was not updated successfully please check logs!!");
                            }
                        }
                    }
                } else {
                    if(plugin.isDebugEnabled()) {
                        SendConsoleMessage.debug("No result by username creating new monthly vote record!");
                    }
                    this.createNewAllTimeVoteRecord(playerUUID, playerName);
                }
            } else if (p == 1) {
                this.updateExistingAllTimeVoteRecord(playerUUID, playerName);
            } else {
                plugin.getLogger().severe("more than one record was found for a player!! failed to register vote!");
            }
            result.close();
            statement.close();
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("error adding all time player vote!!" + e);
        }
    }

    public void createNewMonthlyVoteRecord(UUID playerUUID, String playerName) {
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        String sql = "INSERT INTO MONTHLYVOTES VALUES (null, '" + playerUUID + "', '" + playerName + "','" + 1 + "','" + 1 + "','" + date + "')";//inserts a new all time vote record with 1 vote
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("Create New Monthly Vote Record: " + sql);
        }
        execute(sql);
    }

    public void createNewAllTimeVoteRecord(UUID playerUUID, String playerName) {
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        String sql = "INSERT INTO ALLVOTES VALUES (null, '" + playerUUID + "', '" + playerName + "','" + 1 + "','" + date + "')";//inserts a new all time vote record with 1 vote
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("Create New All Time Vote Record: " + sql);
        }
        execute(sql);
    }

    public void updateExistingMonthlyVoteRecord(UUID playerUUID, String playerName) {
        String query = "SELECT VOTES,UNCLAIMEDVOTES,PLAYER FROM MONTHLYVOTES WHERE UUID ='" + playerUUID + "'";
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("Update Existing Vote Record: " + query);
        }
        int voteCount = 0;
        int unclaimedCount = 0;
        int rows = 0;
        String playerNameSQL = null;
        try {
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                voteCount = result.getInt("VOTES");
                unclaimedCount = result.getInt("UNCLAIMEDVOTES");
                playerNameSQL = result.getString("PLAYER");
                rows++;
            }
            result.close();
            statement.close();
            if (!(rows > 1)) {
                int newVoteValue = voteCount + 1;
                int newUnclaimedCount = unclaimedCount + 1;
                java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
                String sql = "UPDATE MONTHLYVOTES SET VOTES ='" + newVoteValue + "',LASTVOTE ='" + date + "', UNCLAIMEDVOTES='" + newUnclaimedCount + "' WHERE `UUID`='" + playerUUID + "'";
                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().info(sql);
                    SendConsoleMessage.debug("RowCount: " + rows);
                    SendConsoleMessage.debug("VoteCount: " + voteCount);
                    SendConsoleMessage.debug("UnclaimedCount: " + unclaimedCount);
                    SendConsoleMessage.debug("NewVoteCount: " + newVoteValue);
                    SendConsoleMessage.debug("NewUnclaimedCount: " + newUnclaimedCount);
                }
                execute(sql);
            } else {
                plugin.getLogger().severe("Duplicate player names please check database!");
            }
            if (!playerNameSQL.equals(playerName)) {
                if (plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("Player name has changed, updating.");
                }
                execute("UPDATE MONTHLYVOTES SET PLAYER='" + playerName + "' WHERE UUID='" + playerUUID + "'");
            }
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL Error!" + e);
        }

    }

    public void updateExistingAllTimeVoteRecord(UUID playerUUID, String playerName) {
        String query = "SELECT VOTES,PLAYER FROM ALLVOTES WHERE UUID ='" + playerUUID + "'";
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("Update Existing Vote Record: " + query);
        }
        int voteCount = 0;
        int unclaimedCount = 0;
        int rows = 0;
        String playerNameSQL = null;

        try {
            Statement statement = this.getConnection().createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                voteCount = result.getInt("VOTES");
                playerNameSQL = result.getString("PLAYER");
                rows++;
            }
            result.close();
            statement.close();
            if (!(rows > 1)) {
                int newVoteValue = voteCount + 1;
                java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
                String sql = "UPDATE ALLVOTES SET VOTES ='" + newVoteValue + "',LASTVOTE ='" + date + "' WHERE `UUID`='" + playerUUID + "'";
                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().info(sql);
                }
                Statement statement2 = this.getConnection().createStatement();
                statement2.executeUpdate(sql);
                statement2.close();
            } else {
                plugin.getLogger().severe("Duplicate player names please check database!");
            }
            if (!playerNameSQL.equals(playerName)) {
                if (plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug("Player name has changed, updating.");
                }
                execute("UPDATE ALLVOTES SET PLAYER='" + playerName + "' WHERE UUID='" + playerUUID + "'");
            }
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL Error!" + e);
        }

    }

    /**
     * reset the players unclaimed reward count
     *
     * @param playerUUID the player uuid
     */
    public void resetUnclaimedReward(UUID playerUUID) {
        if(plugin.isDebugEnabled()) {
            SendConsoleMessage.debug("reset unclaimed votes.");
        }
        String sql = "UPDATE MONTHLYVOTES SET UNCLAIMEDVOTES ='0' WHERE `UUID`='" + playerUUID + "'";
        execute(sql);
    }

    public LinkedHashMap<String, Integer> voteAllTime() {

        LinkedHashMap<String, Integer> votesAllTime2 = new LinkedHashMap<String, Integer>();

        String query = "SELECT PLAYER, VOTES FROM ALLVOTES ORDER BY VOTES DESC LIMIT 0, 10";
        int votes = 0;

        try {
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                votesAllTime2.put(result.getString("PLAYER"), result.getInt("VOTES"));
                votes++;
            }
            if (plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("Loaded " + ChatColor.AQUA + votes + ChatColor.GREEN + " all vote records!");
            }

            result.close();
            statement.close();
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            SendConsoleMessage.error("Could not load all time votes!" + e);
        }
        return votesAllTime2;
    }

    public LinkedHashMap<String, Integer> voteAllTime(int limit) {

        LinkedHashMap<String, Integer> votesAllTime2 = new LinkedHashMap<String, Integer>();

        String query = "SELECT PLAYER, VOTES FROM ALLVOTES ORDER BY VOTES DESC LIMIT 0, " + limit;
        int votes = 0;

        try {
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                votesAllTime2.put(result.getString("PLAYER"), result.getInt("VOTES"));
                votes++;
            }
            if (plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("Loaded " + ChatColor.AQUA + votes + ChatColor.GREEN + " all vote records!");
            }

            result.close();
            statement.close();
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            SendConsoleMessage.error("Could not load all time votes!" + e);
        }
        return votesAllTime2;
    }

    public LinkedHashMap<String, Integer> voteMonthly() {


        LinkedHashMap<String, Integer> votesAllTime = new LinkedHashMap<String, Integer>();
        String query = "SELECT PLAYER, VOTES FROM MONTHLYVOTES ORDER BY VOTES DESC LIMIT 0, 10";
        int votes = 0;

        try {
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                votesAllTime.put(result.getString("PLAYER"), result.getInt("VOTES"));
                votes++;
            }
            if (plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("Loaded " + ChatColor.AQUA + votes + ChatColor.GREEN + " monthly vote records!");
            }

            result.close();
            statement.close();
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            SendConsoleMessage.error("Could not retrieve player votes!" + e);
        }
        return votesAllTime;
    }

    /**
     * get a linked hashmap version of the top monthly voters
     * @param limit the limit of top voters to have
     * @return
     */
    public LinkedHashMap<String, Integer> voteMonthly(int limit) {


        LinkedHashMap<String, Integer> votesAllTime = new LinkedHashMap<String, Integer>();
        String query = "SELECT PLAYER, VOTES FROM MONTHLYVOTES ORDER BY VOTES DESC LIMIT 0, " + limit;
        int votes = 0;

        try {
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                votesAllTime.put(result.getString("PLAYER"), result.getInt("VOTES"));
                votes++;
            }
            if (plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("Loaded " + ChatColor.AQUA + votes + ChatColor.GREEN + " monthly vote records!");
            }

            result.close();
            statement.close();

            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            SendConsoleMessage.error("Could not retrieve player votes!" + e);
        }
        return votesAllTime;
    }

    /**
     * check to see how much offline unclaimed votes a player has
     *
     * @param playerUUID the player uuid
     * @return
     */
    public int checkUserUnclaimedVotes(UUID playerUUID) {

        int unclaimedVotes = 0;
        String query = "SELECT UNCLAIMEDVOTES FROM MONTHLYVOTES WHERE UUID='" + playerUUID + "'";
        int results = 0;
        try {
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                unclaimedVotes = result.getInt("UNCLAIMEDVOTES");
                results++;
            }
            result.close();
            statement.close();
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            SendConsoleMessage.error("Could not load unclaimed votes!" + e);
        }

        if (results == 0) {
            return 0;
        } else if (results == 1) {
            return unclaimedVotes;
        } else {
            return -1;// if more than one result appears returns -1
        }
    }

    /**
     * remove unclaimed votes from a player
     *
     * @param playerUUID the player UUID
     * @return true if player has enough, false if not
     */
    public boolean removeUnclaimedVotes(UUID playerUUID, int amount) {

        int unclaimedVotes = 0;
        String query = "SELECT UNCLAIMEDVOTES FROM MONTHLYVOTES WHERE UUID='" + playerUUID + "'";
        int results = 0;
        try {
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                unclaimedVotes = result.getInt("UNCLAIMEDVOTES");
                results++;
            }

            result.close();
            statement.close();
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            SendConsoleMessage.error("Could not load unclaimed votes!" + e);
        }

        if (amount == 0) {
            return false;
        }

        if (unclaimedVotes >= amount) {
            int finalAmount = unclaimedVotes - amount;
            String sql = "UPDATE MONTHLYVOTES SET UNCLAIMEDVOTES ='" + finalAmount + "' WHERE `UUID`='" + playerUUID + "'";

            try {
                if (plugin.isDebugEnabled()) {
                    SendConsoleMessage.debug(sql);
                }
                Statement statement = this.getConnection().createStatement();
                statement.executeUpdate(sql);
                statement.close();
                if(!plugin.getFileManager().isMaintainConnection()) {
                    connection.close();
                }
                return true;
            } catch (SQLException e) {
                SendConsoleMessage.error("SQL Error!" + e);
            }
        }
        return false;
    }

    public int checkUserVotes(UUID playerUUID, String table) {
        int playerVotes = 0;
        String query = "SELECT VOTES FROM " + table + " WHERE UUID='" + playerUUID + "'";
        if(plugin.isDebugEnabled()) {
            SendConsoleMessage.debug("SQL Query: " + query);
        }
        int results = 0;
        try {
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                playerVotes = result.getInt("VOTES");
                results++;
            }

            result.close();
            statement.close();
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            SendConsoleMessage.error("Could not check players votes by UUID!" + e);
        }

        if (results == 0) {
            return 0;
        } else if (results == 1) {
            if(plugin.isDebugEnabled()) {
                SendConsoleMessage.debug("Vote count: " + playerVotes);
            }
            return playerVotes;
        } else {
            return -1;// if more than one result appears returns -1
        }

    }

    public int checkUserVotes(String playerName, String table) {
        int playerVotes = 0;
        String query = "SELECT VOTES FROM " + table + " WHERE PLAYER='" + playerName + "'";
        if(plugin.isDebugEnabled()) {
            SendConsoleMessage.debug("SQL Query: " + query);
        }
        int results = 0;
        try {
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                playerVotes = result.getInt("VOTES");
                results++;
            }

            result.close();
            statement.close();
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            SendConsoleMessage.error("Could check players votes by username!" + e);
        }

        if (results == 0) {
            return 0;
        } else if (results == 1) {
            return playerVotes;
        } else {
            return -1;// if more than one result appears returns -1
        }

    }

    public boolean hasVotedToday(UUID playerUUID) {
        java.sql.Date todaysDate = new java.sql.Date(System.currentTimeMillis());

        String query = "SELECT ID FROM MONTHLYVOTES WHERE UUID ='" + playerUUID + "' AND LASTVOTE ='" + todaysDate + "'";
        int results = 0;

        try {
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                results++;
            }
            result.close();
            statement.close();
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            SendConsoleMessage.error("could not check if player voted today!");
        }

        if (results == 1) {
            return true;
        } else {
            return false;
        }
    }

    public void resetVotes(String tablename) {

        String sql = "UPDATE " + tablename + " SET VOTES = 0";
        try {
            Statement statement = this.getConnection().createStatement();
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("could not reset votes for table " + tablename);
            return;
        }

        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        String sql2 = "UPDATE LASTRESET SET LASTRESET ='" + date + "' WHERE TABLENAME='" + tablename + "'";
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info(sql);
            plugin.getLogger().info(sql2);
        }
        try {
            Statement statement2 = this.getConnection().createStatement();
            statement2.executeUpdate(sql2);
            statement2.close();
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("could not set last reset date." + e);
        }

    }

    public void setInitialLastReset(String tableName) {
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        String sql = "INSERT INTO LASTRESET VALUES (null,'" + tableName + "','" + date + "')";
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("Add initial last reset: " + sql);
        }
        try {
            Statement statement = getConnection().createStatement();
            statement.executeUpdate(sql);
            statement.close();
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("ERROR inserting new initial last reset!" + e);
        }
    }

    public java.sql.Date getLastResetDate(String tableName) {
        java.sql.Date lastReset = null;
        String query = "SELECT LASTRESET FROM LASTRESET WHERE TABLENAME='" + tableName + "'";
        int results = 0;
        try {
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                lastReset = result.getDate("LASTRESET");
                results++;
            }

            result.close();
            statement.close();
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not check last reset date!" + e);
        }

        if (results == 0) {
            return null;
        } else if (results == 1) {
            return lastReset;
        } else {
            return null;
        }
    }

    public LinkedHashMap<String, Integer> loadTopFiveDailyVotes() {

        java.sql.Date todaysDate = new java.sql.Date(System.currentTimeMillis());

        LinkedHashMap<String, Integer> topFiveDailyVotes = new LinkedHashMap<String, Integer>();
        String query = "SELECT PLAYER, VOTES FROM MONTHLYVOTES WHERE DATE = '" + todaysDate + "' ORDER BY VOTES DESC LIMIT 0, 5";
        if (plugin.isDebugEnabled()) {
            SendConsoleMessage.debug(query);
        }
        int votes = 0;

        try {
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                topFiveDailyVotes.put(result.getString("PLAYER"), result.getInt("VOTES"));
                votes++;
            }

            result.close();
            statement.close();
            SendConsoleMessage.info("Loaded " + ChatColor.AQUA + votes + ChatColor.GREEN + " daily vote records!");
            if(!plugin.getFileManager().isMaintainConnection()) {
                connection.close();
            }
        } catch (SQLException e) {
            SendConsoleMessage.error("Could not load top 5 daily votes votes!" + e);
        }
        return topFiveDailyVotes;
    }
}

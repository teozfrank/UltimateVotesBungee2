package net.teozfrank.ultimatevotesbungee.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Callable;

public class UUIDFetcher implements Callable<UUID> {
    private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
    private final String name;
    JsonParser jsonParser = new JsonParser();

    public UUIDFetcher(String name) {
        this.name = name;
    }

    public UUID call() throws Exception {
        HttpURLConnection connection = createConnection();
        String json = new Gson().toJson(name);

        writeBody(connection, json);
        JsonArray array = (JsonArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
        for (Object profile : array) {
            JsonObject jsonProfile = (JsonObject) profile;
            String id = jsonProfile.get("id").toString();
            id = id.replaceAll("\"", "");
            String name = jsonProfile.get("name").toString();
            SendConsoleMessage.debug(id);
            UUID uuid = UUIDFetcher.getUUID(id);
            return uuid;
        }
        return null;
    }

    private static void writeBody(HttpURLConnection connection, String body) throws Exception {

        OutputStream stream = connection.getOutputStream();
        SendConsoleMessage.debug("UUID body: " + body);
        stream.write(body.getBytes());
        stream.flush();
        stream.close();
    }

    private static HttpURLConnection createConnection() throws Exception {
        URL url = new URL(PROFILE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    private static UUID getUUID(String id) {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" +id.substring(20, 32));
    }

    public static byte[] toBytes(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

    public static UUID fromBytes(byte[] array) {
        if (array.length != 16) {
            throw new IllegalArgumentException("Illegal byte array length: " + array.length);
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(array);
        long mostSignificant = byteBuffer.getLong();
        long leastSignificant = byteBuffer.getLong();
        return new UUID(mostSignificant, leastSignificant);
    }
}
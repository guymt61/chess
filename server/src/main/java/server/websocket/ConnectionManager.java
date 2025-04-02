package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Connection>> games = new ConcurrentHashMap<>();

    public void add(int gameId, String username, Session session) {
        var connection = new Connection(username, session);
        games.putIfAbsent(gameId, new ConcurrentHashMap<>());
        games.get(gameId).put(username, connection);
    }

    public void remove(int gameId, String username) {
        games.get(gameId).remove(username);
    }

    public void broadcast(int gameId, String excludeUsername, ServerMessage serverMessage) throws IOException {
        var removeList = new ArrayList<Connection>();
        ConcurrentHashMap<String, Connection> connections = games.get(gameId);
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                if (!c.username.equals(excludeUsername)) {
                    c.send(new Gson().toJson(serverMessage));
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.remove(c.username);
        }
    }

    public void send(int gameId, String username, ServerMessage serverMessage) throws IOException {
        Connection connection = games.get(gameId).get(username);
        if (connection != null) {
            if (connection.session.isOpen()) {
                connection.send(new Gson().toJson(serverMessage));
            }
            else {
                games.get(gameId).remove(username);
            }
        }
    }
}
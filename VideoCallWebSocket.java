package com.videocall;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import org.json.JSONObject;

@ServerEndpoint("/ws/{username}")
public class VideoCallWebSocket {
    private static ConcurrentHashMap<String, Session> clients = new ConcurrentHashMap<>();
    private String username;
    
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        this.username = username;
        clients.put(username, session);
        System.out.println("hi " + username + " connected!");
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        JSONObject json = new JSONObject(message);
        String to = json.optString("to");

        if (clients.containsKey(to)) {
            clients.get(to).getBasicRemote().sendText(message);
        }
    }

    @OnClose
    public void onClose(Session session) {
        clients.remove(username);
        System.out.println(username + " disconnected.");
    }
}


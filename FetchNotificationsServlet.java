package com.java.fullstack;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/fetchNotifications")
public class FetchNotificationsServlet extends HttpServlet {
    private DataBaseUtility db;

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        System.out.println("Fetching Notifications...");
        res.setContentType("application/json");
        db = DataBaseUtility.getInstance();

        int caretakerId = db.getUserId((String) req.getSession().getAttribute("name"));
        System.out.println("Caretaker ID: " + caretakerId);

        // ✅ Fetch latest notifications first
        String query = "SELECT notification_id, message, read_status, request_id FROM NotificationInformation WHERE user_id = ? AND read_status = 'read' AND showMessage = TRUE ORDER BY notification_id DESC";  

        try (Connection conn = DataBaseUtility.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, caretakerId);
            ResultSet rs = stmt.executeQuery();

            JSONArray notifications = new JSONArray();

            while (rs.next()) {
                JSONObject notification = new JSONObject();
                notification.put("notification_id", rs.getInt("notification_id"));
                notification.put("message", rs.getString("message"));
                notification.put("readStatus", rs.getString("read_status"));
                notification.put("rId", rs.getInt("request_id"));  // Added request ID
                notifications.put(notification);
            }

            JSONObject responseJson = new JSONObject();
            responseJson.put("notifications", notifications);
            res.getWriter().println(responseJson);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

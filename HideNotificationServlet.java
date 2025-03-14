package com.java.fullstack;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

@WebServlet("/hideNotification")
public class HideNotificationServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        int notificationId = Integer.parseInt(req.getParameter("notification_id"));
        
        String query = "UPDATE NotificationInformation SET showMessage = FALSE, read_status = 'read'  WHERE notification_id = ?";
        
        try (Connection conn = DataBaseUtility.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, notificationId);
            int rowsUpdated = stmt.executeUpdate();
            
            res.setContentType("application/json");
            JSONObject responseJson = new JSONObject();
            responseJson.put("success", rowsUpdated > 0);
            res.getWriter().println(responseJson);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

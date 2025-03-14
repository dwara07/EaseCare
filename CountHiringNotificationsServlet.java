package com.java.fullstack;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

@WebServlet("/countHiringNotifications")
public class CountHiringNotificationsServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();

        try {
            int userId = DataBaseUtility.getInstance().getUserId((String) request.getSession().getAttribute("name"));
            int hiringCount = getHiringNotificationCount(userId);
System.out.println("count" + hiringCount + "  " +userId);
            jsonResponse.put("userId", userId);
            jsonResponse.put("hiringCount", hiringCount);
        } catch (Exception e) {
            jsonResponse.put("error", "Database error: " + e.getMessage());
        }

        out.print(jsonResponse.toString());
    }

    private int getHiringNotificationCount(int userId) throws Exception {
        int count = 0;
        try (Connection conn = DataBaseUtility.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM NotificationInformation WHERE user_id = ? AND notify_type = 'hiring'")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        }
        return count;
    }
}

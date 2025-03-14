package com.java.fullstack;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

@WebServlet("/MarkNotificationsAsReadServlet")
public class MarkNotificationsAsReadServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        DataBaseUtility db = DataBaseUtility.getInstance();
        int userId = db.getUserId((String) request.getSession().getAttribute("name")); // Ensure correct user retrieval

        JSONObject jsonResponse = new JSONObject();
        if (userId > 0) { // Ensure valid user ID
            try (Connection conn = DataBaseUtility.getConnection()) {
                String sql = "UPDATE NotificationInformation SET read_status = 'read' WHERE user_id = ? AND read_status = 'unread'";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                int rowsUpdated = stmt.executeUpdate();

                jsonResponse.put("success", rowsUpdated > 0);
            } catch (Exception e) {
                e.printStackTrace();
                jsonResponse.put("success", false);
            }
        } else {
            jsonResponse.put("success", false);
        }
        response.getWriter().write(jsonResponse.toString());
    }
}

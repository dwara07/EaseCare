package com.java.fullstack;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

@WebServlet("/getNameByNotificationId")
public class GetNameByNotificationIdServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String notificationIdStr = request.getParameter("notification_id");
        JSONObject jsonResponse = new JSONObject();

        if (notificationIdStr == null || notificationIdStr.isEmpty()) {
            jsonResponse.put("error", "Invalid notification ID");
            response.setContentType("application/json");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        int notificationId = Integer.parseInt(notificationIdStr);
        
        try (Connection conn = DataBaseUtility.getConnection()) {
            // Step 1: Get service_id and user_id from NotificationInformation
            String sql1 = "SELECT service_id, user_id FROM NotificationInformation WHERE notification_id = ?";
            try (PreparedStatement stmt1 = conn.prepareStatement(sql1)) {
                stmt1.setInt(1, notificationId);
                ResultSet rs1 = stmt1.executeQuery();

                if (rs1.next()) {
                    int userId = rs1.getInt("user_id");

                    // Step 2: Get user_name from User_Credentials
                    String sql2 = "SELECT user_name FROM User_Credentials WHERE user_id = ?";
                    try (PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
                        stmt2.setInt(1, userId);
                        ResultSet rs2 = stmt2.executeQuery();

                        if (rs2.next()) {
                            String userName = rs2.getString("user_name");
                            jsonResponse.put("user_name", userName);
                        } else {
                            jsonResponse.put("error", "User not found");
                        }
                    }
                } else {
                    jsonResponse.put("error", "Notification not found");
                }
            }
        } catch (Exception e) {
            jsonResponse.put("error", "Database error: " + e.getMessage());
        }

        response.setContentType("application/json");
        response.getWriter().write(jsonResponse.toString());
    }
}

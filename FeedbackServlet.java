package com.java.fullstack;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

@WebServlet("/feedback")
public class FeedbackServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Retrieve form data
        String message = "A feedback was sent by " + request.getSession().getAttribute("name") + " : " +  request.getParameter("comments");
        DataBaseUtility db = DataBaseUtility.getInstance();

        int userId = db.getUserId(request.getParameter("name"));
        int service_id = db.getUserId((String) request.getSession().getAttribute("name"));

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int notificationId = -1;
        boolean success = false;

        try {
            Connection conn = DataBaseUtility.getConnection();

            // Insert into NotificationInformation
            String sql1 = "INSERT INTO NotificationInformation (message, user_id, service_id, notify_type) VALUES (?, ?, ?, 'feedback')";
            pstmt = conn.prepareStatement(sql1);
            pstmt.setString(1, message);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, service_id);
            pstmt.executeUpdate();
            pstmt.close(); 

            // Retrieve last inserted notification_id
            String sql2 = "SELECT notification_id FROM NotificationInformation WHERE message = ? AND user_id = ? AND service_id = ? ORDER BY notification_id DESC LIMIT 1";
            pstmt = conn.prepareStatement(sql2);
            pstmt.setString(1, message);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, service_id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                notificationId = rs.getInt("notification_id");
            }

            // Insert into User_Activity
            if (notificationId != -1) {
                String sql3 = "INSERT INTO User_Activity (notification_id, user_id) VALUES (?, ?)";
                pstmt = conn.prepareStatement(sql3);
                pstmt.setInt(1, notificationId);
                pstmt.setInt(2, userId);
                pstmt.executeUpdate();
                success = true; // If this executes, mark success
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (Exception ignored) {}
        }

        // Send JSON response
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", success ? "ok" : "error");
        response.getWriter().write(jsonResponse.toString());
    }
}

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

import org.json.JSONObject;

@WebServlet("/ReportUserServlet")
public class ReportUserServlet extends HttpServlet {
    private DataBaseUtility db;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        db = DataBaseUtility.getInstance();

        JSONObject requestData = new JSONObject(request.getReader().readLine());

        String reportingPerson = requestData.getString("reportingPerson");
        String reportedPerson = requestData.getString("reportedPerson");
        String complaintText = requestData.getString("complaint");

        int reportingUserId = db.getUserId(reportingPerson);
        int reportedUserId = db.getUserId(reportedPerson);

        String insertQuery = "INSERT INTO NotificationInformation (user_id, service_id, message, read_status, notify_type,isRequestService) VALUES (?, ?, ?, ?, ?,FALSE)";
        String selectQuery = "SELECT notification_id FROM NotificationInformation WHERE user_id = ? ORDER BY notification_id DESC LIMIT 1";

        try (Connection conn = DataBaseUtility.getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
             PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {

            insertStmt.setInt(1, reportedUserId);
            insertStmt.setInt(2, reportingUserId);
            insertStmt.setString(3, "You have been reported! Reason: " + complaintText);
            insertStmt.setString(4, "unread");
            insertStmt.setString(5, "report");

            int rowsAffected = insertStmt.executeUpdate();

            int notificationId = -1; // Default value in case it's not found
            if (rowsAffected > 0) {
                selectStmt.setInt(1, reportedUserId);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        notificationId = rs.getInt("notification_id");
                    }
                }
                // ✅ Now `notificationId` is valid before using it
                db.insertIntoUserActivity(notificationId, reportedUserId);
            }

            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("success", rowsAffected > 0);
            response.getWriter().println(jsonResponse);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

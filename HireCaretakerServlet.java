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

@WebServlet("/HireCaretakerServlet")
public class HireCaretakerServlet extends HttpServlet {
    private DataBaseUtility db;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	response.setContentType("text/html; charset=UTF-8");
        response.setContentType("application/json");
        db = DataBaseUtility.getInstance();

        String whoClickedName = (String) request.getSession().getAttribute("name");
        int whoClickedId = db.getUserId(whoClickedName);

        JSONObject requestData = new JSONObject(request.getReader().readLine());
        String caretakerName = requestData.getString("caretakerName");

        int caretakerId = db.getUserId(caretakerName);

        String insertQuery = "INSERT INTO NotificationInformation (user_id, service_id, message, isRequestService, notify_type) VALUES (?, ?, ?, ?, ?)";
        String selectQuery = "SELECT notification_id FROM NotificationInformation WHERE user_id = ? ORDER BY notification_id DESC LIMIT 1";

        try (Connection conn = DataBaseUtility.getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
             PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {

            insertStmt.setInt(1, caretakerId);
            insertStmt.setInt(2, whoClickedId);
            insertStmt.setString(3, "You have been hired by " + whoClickedName + " for RS " + requestData.getString("amount") + " " + requestData.getString("duration") + " !!!");
            insertStmt.setBoolean(4, false);
            insertStmt.setString(5, "hiring");

            int rowsAffected = insertStmt.executeUpdate();

            int notificationId = -1; // Default value if not found
            if (rowsAffected > 0) {
                selectStmt.setInt(1, caretakerId);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        notificationId = rs.getInt("notification_id");
                    }
                }
                // ✅ Now `notificationId` is valid
                System.out.println("notiiiii hire " + notificationId);
                db.insertIntoUserActivity(notificationId, caretakerId);
            }

            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("success", rowsAffected > 0);
            response.getWriter().println(jsonResponse);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

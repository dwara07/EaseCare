package com.java.fullstack;
import java.io.BufferedReader;
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

@WebServlet("/updateWorkStatus")
public class UpdateWorkStatusServlet extends HttpServlet {
    private DataBaseUtility db;

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        db = DataBaseUtility.getInstance();
        String name = (String) req.getSession().getAttribute("name");
    	int userId = db.getUserId(name);

        
        BufferedReader reader = req.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        try {
            JSONObject requestData = new JSONObject(sb.toString());
            int rId = requestData.getInt("rId");

            // ✅ Update the status in the database
            String query = "UPDATE Requestservices SET status = 'hired' WHERE rId = ?";
            try (Connection conn = DataBaseUtility.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, rId);
                int rowsAffected = stmt.executeUpdate();
                int notificationId = Integer.parseInt(requestData.getString("nId").replace("noti-id-", ""));
                System.out.println("reeeee " + requestData.getString("nId") + notificationId);

                	
                insertIntoNotification(userId ,rId, name,notificationId); 
                
				/*
				 * db.insertIntoUserActivity(notificationId, userId);
				 */                
                JSONObject responseJson = new JSONObject();
                responseJson.put("success", rowsAffected > 0);
                res.getWriter().println(responseJson);
            }
        } catch (Exception e) {	
            e.printStackTrace();
            res.getWriter().println("{\"success\": false}");
        }
    }
    
    private void insertIntoNotification(int userId, int rId, String careTakerName,int notificationId) {
        String title = null;
        String query = "SELECT tittle,user_id FROM Requestservices WHERE rId = ?";
        int id = 0;

        try (Connection conn = DataBaseUtility.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, rId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
            	id = rs.getInt("user_id");
                title = rs.getString("tittle");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (title != null) {
            String message = "Your " + title + " request was accepted by " + careTakerName;
            // Insert into NotificationInformation using a separate connection
            String insertQuery = "INSERT INTO NotificationInformation (message, user_id,request_id, notify_type,service_id, isRequestService) VALUES (?, ?,?, 'hired',?, FALSE)";
            try (Connection conn = DataBaseUtility.getConnection();
                 PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setString(1, message);
                insertStmt.setInt(2, getServiceIdByNotificationId(notificationId));
                insertStmt.setInt(3, rId);
                insertStmt.setInt(4, userId);
                insertStmt.executeUpdate();
                int lastUpdatedNotificationId = notificationId + 1;
                db.insertIntoUserActivity(lastUpdatedNotificationId, id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static Integer getServiceIdByNotificationId(int notificationId) {
        String query = "SELECT service_id FROM NotificationInformation WHERE notification_id = ?";
        try (Connection conn = DataBaseUtility.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, notificationId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("service_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if no user_id is found
    }

    
    
}

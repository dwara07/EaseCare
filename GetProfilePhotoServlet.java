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

@WebServlet("/getProfilePhoto")
public class GetProfilePhotoServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Assuming user_id is stored in session as an attribute
        int userId = (int) DataBaseUtility.getInstance().getUserId((String) request.getSession(false).getAttribute("name"));

        // Fetch the profile photo URL from the database
        String profilePhotoUrl = getProfilePhotoUrlFromDb(userId);

        // Send back the URL as JSON response
        response.setContentType("application/json");
        response.getWriter().print(new JSONObject().put("profilePhotoUrl", profilePhotoUrl));
    }
    
    private String getProfilePhotoUrlFromDb(int userId) {
        String profilePhotoUrl = null;
        try (Connection conn = DataBaseUtility.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT path FROM user_profile WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                profilePhotoUrl = rs.getString("path");
            } else {
                // Return a default image URL if not found
                profilePhotoUrl = "https://cdn.vectorstock.com/i/1000v/92/16/default-profile-picture-avatar-user-icon-vector-46389216.jpg";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            profilePhotoUrl = "https://cdn.vectorstock.com/i/1000v/92/16/default-profile-picture-avatar-user-icon-vector-46389216.jpg";  // fallback on error
        }
        return profilePhotoUrl;
    }
}

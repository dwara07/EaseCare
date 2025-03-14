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

import com.java.fullstack.DataBaseUtility;

@WebServlet("/SaveProfileServlet")
public class SaveProfileServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Getting userId from session
        int userId = DataBaseUtility.getInstance().getUserId((String) request.getSession(false).getAttribute("name")); 
        
        // Get the new path from the request
        String newPath = request.getParameter("path");
        
        // Ensure path is not null or empty
        if (newPath == null || newPath.isEmpty()) {
            response.getWriter().write("Error: Path cannot be empty.");
            return;
        }
        
        // Perform the database update
        try (Connection conn = DataBaseUtility.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE user_profile SET path=? WHERE user_id=?")) {

            // Set parameters
            stmt.setString(1, newPath);
            stmt.setInt(2, userId);
            
            // Execute the update
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                response.getWriter().write("Profile updated successfully.");
            } else {
                response.getWriter().write("Error: User not found or no changes made.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("Database error: " + e.getMessage());
        }
    }
}

package com.java.fullstack;

import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.sql.*;
import org.json.JSONObject;

@WebServlet("/UpdateAllDetails")
public class UpdateAllDetails extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONObject responseObj = new JSONObject();
        Connection conn = null;
        DataBaseUtility db = DataBaseUtility.getInstance();

        try {
            // Read JSON request
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONObject jsonData = new JSONObject(sb.toString());

            // Extract user ID & role
            String sessionName = (String) request.getSession().getAttribute("name");
            if (sessionName == null) throw new Exception("User session not found!");
            
            int userId = db.getUserId(sessionName);
            String role = db.getRole(userId); // Get user role

            // Extract user data
            String fullname = jsonData.getString("fullName");
            String email = jsonData.getString("email");
            String mobile = jsonData.getString("mobile");
            String gender = jsonData.getString("gender");
            String bio = jsonData.optString("bio", ""); // Optional
            String servicesInterested = jsonData.optString("servicesInterested", ""); // Optional
            String changePassword = jsonData.optString("changePassword", ""); // Optional

            // Database Connection
            conn = DataBaseUtility.getConnection();
            int updatedRows = 0;

            // ✅ Update `Personal_Information` table (phone number)
            String updatePersonalInfo = "UPDATE Personal_Information SET phone_number=? WHERE u_id=?";
            try (PreparedStatement pstmt = conn.prepareStatement(updatePersonalInfo)) {
                pstmt.setString(1, mobile);
                pstmt.setInt(2, userId);
                updatedRows += pstmt.executeUpdate();
            }

            // ✅ Update `User_Credentials` table (email & username)
            String updateEmail = "UPDATE User_Credentials SET user_gmail=?, user_name=? WHERE user_id=?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateEmail)) {
                pstmt.setString(1, email);
                pstmt.setString(2, fullname);
                pstmt.setInt(3, userId);
                pstmt.executeUpdate();
            }

            // ✅ Update `Personal_Information` table (gender)
            if (!gender.isEmpty()) {
                String updateGender = "UPDATE Personal_Information SET gender=? WHERE u_id=?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateGender)) {
                    pstmt.setString(1, gender);
                    pstmt.setInt(2, userId);
                    pstmt.executeUpdate();
                }
            }

            // ✅ Update `CareTakerInfo` only for caretakers
            if ("caretaker".equalsIgnoreCase(role) && !servicesInterested.isEmpty()) {
                String updateServices = "UPDATE CareTakerInfo SET service_interested=?, description=? WHERE caretaker_id=?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateServices)) {
                    pstmt.setString(1, servicesInterested);
                    pstmt.setString(2, bio);
                    pstmt.setInt(3, userId);
                    pstmt.executeUpdate();
                }
            }

            // ✅ Update `User_Credentials` table (Password)
            if (!changePassword.isEmpty()) {
                String updatePassword = "UPDATE User_Credentials SET user_password=? WHERE user_id=?";
                try (PreparedStatement pstmt = conn.prepareStatement(updatePassword)) {
                    pstmt.setString(1, changePassword);
                    pstmt.setInt(2, userId);
                    pstmt.executeUpdate();
                }
            }

            // ✅ SUCCESS RESPONSE
            responseObj.put("status", "success");
            responseObj.put("message", updatedRows > 0 ? "Your details updated successfully!" : "No changes made.");

            // ✅ Update session name (only if update is successful)
            if (!responseObj.getString("message").contains("Please")) {
                request.getSession().setAttribute("name", fullname);
            }

        } catch (Exception e) {
            e.printStackTrace();
            responseObj.put("status", "error");

            String errorMessage = e.getMessage();
            if (errorMessage.contains("Duplicate entry") && errorMessage.contains("user_name")) {
                responseObj.put("message", "Username already exists! Please choose a different one.");
            } else if (errorMessage.contains("Duplicate entry") && errorMessage.contains("user_gmail")) {
                responseObj.put("message", "Email already exists! Please use a different email.");
            } else {
                responseObj.put("message", "Database update failed: " + errorMessage);
            }
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            out.print(responseObj);
            out.flush();
        }
    }
}

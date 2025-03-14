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

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/GetParticularCareTakerDetails")
public class GetParticularCareTakerDetails extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String caretakerName = request.getParameter("caretakerName");

        JSONArray jsonResponse = getCaretakerDetails(caretakerName);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }
    
    private JSONArray getCaretakerDetails(String caretakerName) {
        JSONArray caretakerArray = new JSONArray();
        String query = "SELECT u.user_name AS caretakerName, p.age, p.phone_number, " +
                       "p.street, p.pin_code, p.city, p.state, p.country, " +
                       "u.user_gmail, c.description, c.qualification, c.experience, " +
                       "c.job_type, c.service_interested, up.path AS profileImage " + // Fetch profile image
                       "FROM User_Credentials u " +
                       "JOIN Personal_Information p ON u.user_id = p.u_id " +
                       "JOIN CareTakerInfo c ON u.user_id = c.caretaker_id " +
                       "LEFT JOIN user_profile up ON u.user_id = up.user_id " + // Join profile image table
                       "WHERE u.user_name = ?";

        try (Connection conn = DataBaseUtility.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, caretakerName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    JSONObject caretakerData = new JSONObject();
                    caretakerData.put("caretakerName", rs.getString("caretakerName"));
                    caretakerData.put("age", rs.getInt("age"));
                    caretakerData.put("contactNumber", rs.getString("phone_number"));

                    // Format Address: "No 25, 68th Street, New York, NY 10001"
                    String address = "No " + rs.getString("pin_code") + ", " + rs.getString("street") + ", " +
                                     rs.getString("city") + ", " + rs.getString("state") + ", " + rs.getString("country");
                    String customizedAddress = rs.getString("city") + ", " + rs.getString("state") + ", " + rs.getString("country");

                    caretakerData.put("address", address);
                    caretakerData.put("customizedAddress", customizedAddress);
                    caretakerData.put("email", rs.getString("user_gmail"));
                    caretakerData.put("bio", rs.getString("description"));

                    // Additional Fields
                    caretakerData.put("qualification", rs.getString("qualification"));
                    caretakerData.put("exp", rs.getString("experience"));
                    caretakerData.put("jobType", rs.getString("job_type"));
                    caretakerData.put("servicesInterested", formatServicesInterested(rs.getString("service_interested")));

                    // Profile Image
                    caretakerData.put("profile", rs.getString("profileImage"));

                    // Add the caretaker data to the array
                    caretakerArray.put(caretakerData);
                } else {
                    JSONObject errorResponse = new JSONObject();
                    errorResponse.put("error", "Caretaker not found");
                    caretakerArray.put(errorResponse);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", "Database error");
            caretakerArray.put(errorResponse);
        }
        return caretakerArray;
    }
    
    private String formatServicesInterested(String services) {
        if (services == null || services.isEmpty()) {
            return "";
        }

        String[] serviceArray = services.split(","); // Split by comma
        StringBuilder formattedServices = new StringBuilder();

        for (String service : serviceArray) {
            String[] words = service.split("-"); // Split by hyphen
            StringBuilder formattedService = new StringBuilder();

            for (String word : words) {
                word = word.trim(); // Remove extra spaces
                if (!word.isEmpty()) {
                    formattedService.append(Character.toUpperCase(word.charAt(0))) // First letter uppercase
                                    .append(word.substring(1).toLowerCase()) // Rest lowercase
                                    .append(" "); // Add space
                }
            }

            formattedServices.append(formattedService.toString().trim()).append(", "); // Trim & Add comma
        }

        return formattedServices.toString().replaceAll(", $", ""); // Remove last comma
    }


    
}

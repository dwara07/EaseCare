package com.java.fullstack;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/caretakerInfo")
public class CaretakerInfoServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get form parameters
        String qualification = request.getParameter("qualification");
        String experience = request.getParameter("experience");
        String jobType = request.getParameter("jobType");
        String description = request.getParameter("description");

        // Extracting checked services
        String[] services = request.getParameterValues("services[]"); // Getting selected checkboxes
        
        System.out.println("services " + services);
        
        String serviceInterested = (services != null) ? String.join(", ", services) : ""; // Joining with commas

      
        // Database insertion
        String sql = "INSERT INTO CareTakerInfo (caretaker_id, qualification, experience, job_type, description, service_interested) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataBaseUtility.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int caretakerId = DataBaseUtility.getInstance().getUserId((String) request.getSession().getAttribute("name"));

            stmt.setInt(1, caretakerId);
            stmt.setString(2, qualification);
            stmt.setString(3, experience);
            stmt.setString(4, jobType);
            stmt.setString(5, description);
            stmt.setString(6, serviceInterested); // Insert services as a single string

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Insertion successful!");
                response.sendRedirect("dashboard.html");
            } else {
                System.out.println("Failed to insert data.");
                response.sendRedirect("CareTakerInformation.html");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("CareTakerInformation.html");
        }
    }
}

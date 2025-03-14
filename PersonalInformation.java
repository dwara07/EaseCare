package com.java.fullstack;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/PersonalInformation")
public class PersonalInformation extends HttpServlet {
    private DataBaseUtility db; // Database utility instance

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        db = DataBaseUtility.getInstance(); // Initialize database connection
        extractAddressInput(req, res); // Extract user address input
    }

    private void extractAddressInput(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            // Retrieve input parameters from the request
            int age = Integer.parseInt(req.getParameter("age"));
            String phoneNumber = req.getParameter("phone");
            String country = req.getParameter("country");
            String state = req.getParameter("state");
            String city = req.getParameter("city");
            String street = req.getParameter("street");
            int zipCode = Integer.parseInt(req.getParameter("code"));
            String gender = req.getParameter("gender");

            // Retrieve user ID from session
            String name = (String) req.getSession().getAttribute("name");
            int userId = db.getUserId(name);
            System.out.println("uid: " + userId);

            // Prepare SQL query including the gender field
            String query = "INSERT INTO Personal_Information (age, phone_number, country, state, city, street, pin_code, gender, u_id) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            // Execute query with parameters
            db.executePrepared(query, age, phoneNumber, country, state, city, street, zipCode, gender, userId);
            
            // Redirect based on user role
            if (db.getRole(name).equals("need-care")) {
                res.sendRedirect("dashboard.html");
            } else {
                res.sendRedirect("CareTakerInformation.html");
            }
        } catch (Exception e) {
            res.sendRedirect("personal_information.html");
            e.printStackTrace();
        }
    }
}

package com.java.fullstack;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

@WebServlet("/getCareTakerBioDetails")
public class GetCareTakerBioDetails extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // Set response type
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        DataBaseUtility db = DataBaseUtility.getInstance();
        JSONObject json = new JSONObject();

        // Retrieve user name from session
        String name = (String) req.getSession().getAttribute("name");
        String role = db.getRole(name); // Get role of the logged-in user

        if ("caretaker".equals(role)) {
            String[] caretakerDetails = db.getCareTakerBioDetails(name);

            json.put("email", db.getEmail(name));
            json.put("experience", caretakerDetails[0]);
            json.put("bio", caretakerDetails[1]);
            json.put("phone", caretakerDetails[2]);
            json.put("gender", caretakerDetails[3]);
            json.put("services", caretakerDetails[4]); // List of services
        } else if ("need-care".equals(role)) {
            // Fetch details for users who need care
            String[] needCareDetails = db.needServiceDetails(name);
            json.put("name", needCareDetails[0]);  // User Name
            json.put("email", needCareDetails[1]); // User Email
            json.put("phone", needCareDetails[2]); // Phone Number
            json.put("gender", needCareDetails[3]); // Gender
        }

        res.getWriter().print(json);
    }
}

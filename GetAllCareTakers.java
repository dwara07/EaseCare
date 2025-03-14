package com.java.fullstack;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;

@WebServlet("/getAllCareTakers")
public class GetAllCareTakers extends HttpServlet {
	
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        DataBaseUtility db = DataBaseUtility.getInstance();
        JSONArray caretakers = db.getAllCaretakerDetails();

        res.getWriter().print(caretakers);
        System.out.println("Caretakers Data Sent: " + caretakers);
    }
}

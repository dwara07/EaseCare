package com.java.fullstack;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

@WebServlet("/getAddressAndSettingsInfo")
public class GetAddressAndSettingsInfoServlet extends HttpServlet {
    private DataBaseUtility db;

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        db = DataBaseUtility.getInstance();
        String[] details = db.getAddressAndNotificationInfoByName((String) req.getSession().getAttribute("name"));

        JSONObject json = new JSONObject();
        json.put("country", details[0]);
        json.put("state", details[1]);
        json.put("city", details[2]);
        json.put("street", details[3]);
        json.put("pincode", details[4]);
        json.put("notify_all", details[5]);
        json.put("receive_mail_from_carelink", details[6]);
        json.put("theme", details[7]); // ✅ Include theme

        System.out.println("get settings: " + json.toString());

        res.setContentType("application/json");
        res.getWriter().write(json.toString());
    }
}

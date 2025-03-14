package com.java.fullstack;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserActivity {
    
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        DataBaseUtility db = DataBaseUtility.getInstance();
        String notificationId = req.getParameter("notiId");
        int userId = db.getUserId((String) req.getSession().getAttribute("name"));

        db.insertIntoUserActivity(Integer.parseInt(notificationId) , userId);
    }
}

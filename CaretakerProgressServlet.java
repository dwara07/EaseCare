package com.java.fullstack;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

@WebServlet("/caretakerProgress")
public class CaretakerProgressServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();

        try {
            int userId = DataBaseUtility.getInstance().getUserId((String) request.getSession().getAttribute("name"));
            int successCount = getCount(userId, "feedback");
            int failureCount = getCount(userId, "report");

            int total = successCount + failureCount;
            int happinessPercentage = (total == 0) ? 0 : (successCount * 100) / total;

            jsonResponse.put("userId", userId);
            jsonResponse.put("successCount", successCount);
            jsonResponse.put("failureCount", failureCount);
            jsonResponse.put("happinessPercentage", happinessPercentage);
            System.out.println("ytrsasdf " + jsonResponse.toString());
        } catch (Exception e) {
            jsonResponse.put("error", "Database error: " + e.getMessage());
        }

        out.print(jsonResponse.toString());
    }

    private int getCount(int userId, String notifyType) throws Exception {
        int count = 0;
        try (Connection conn = DataBaseUtility.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM NotificationInformation WHERE user_id = ? AND notify_type = ?")) {
            stmt.setInt(1, userId);
            stmt.setString(2, notifyType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        }
        return count;
    }
}

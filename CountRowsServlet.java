package com.java.fullstack;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

@WebServlet("/countRequests")
public class CountRowsServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();

        try {
            int userId = DataBaseUtility.getInstance().getUserId((String) request.getSession().getAttribute("name"));
            int count = getCountOfRequests(userId);

            jsonResponse.put("userId", userId);
            jsonResponse.put("requestCount", count);
        } catch (NumberFormatException e) {
            jsonResponse.put("error", "Invalid userId format");
        } catch (Exception e) {
            jsonResponse.put("error", "Database error: " + e.getMessage());
        }

        out.print(jsonResponse.toString());
    }

    private int getCountOfRequests(int userId) throws Exception {
        int count = 0;
        try (Connection conn = DataBaseUtility.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM Requestservices WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        }
        return count;
    }
}

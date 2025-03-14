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
import javax.servlet.http.HttpSession;
import org.json.JSONObject;

@WebServlet("/fetchProgressStats")
public class FetchProgressStats extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        HttpSession session = request.getSession();
        int userId = DataBaseUtility.getInstance().getUserId((String) session.getAttribute("name")); 
        System.out.println("User ID ppp: " + userId);

        
        try {
            Connection con = DataBaseUtility.getConnection();

            // Count feedback (success)
            PreparedStatement psFeedback = con.prepareStatement("SELECT COUNT(*) FROM NotificationInformation WHERE service_id = ? AND TRIM(notify_type) = 'feedback'");
            psFeedback.setInt(1, userId);
            ResultSet rsFeedback = psFeedback.executeQuery();
            int feedbackCount = rsFeedback.next() ? rsFeedback.getInt(1) : 0;

            // Count report (failure)
            PreparedStatement psReport = con.prepareStatement("SELECT COUNT(*) FROM NotificationInformation WHERE service_id = ? AND TRIM(notify_type) = 'report'");
            psReport.setInt(1, userId);
            ResultSet rsReport = psReport.executeQuery();
            int reportCount = rsReport.next() ? rsReport.getInt(1) : 0;

            int total = feedbackCount + reportCount;
            System.out.println("feed" + feedbackCount + "  " + reportCount);
            int successRate = (total == 0) ? 0 : (feedbackCount * 100) / total;
            int failureRate = (total == 0) ? 0 : (reportCount * 100) / total;

            JSONObject json = new JSONObject();
            json.put("successRate", successRate);
            json.put("failureRate", failureRate);

            out.print(json);
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

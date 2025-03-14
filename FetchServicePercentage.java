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

@WebServlet("/fetchServicePercentage")
public class FetchServicePercentage extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        int userId = DataBaseUtility.getInstance().getUserId((String) session.getAttribute("name")); 
        
        try {
            Connection con = DataBaseUtility.getConnection();
            // Query to count occurrences of each category
            String[] categories = {"Pet Sitting", "Baby Sitting", "Elderly Care", "House Sitting"};
            int[] counts = new int[categories.length];
            int total = 0;
            
            for (int i = 0; i < categories.length; i++) {
                PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM Requestservices WHERE required = ? AND user_id = ?");
                ps.setString(1, categories[i]);
                ps.setInt(2, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    counts[i] = rs.getInt(1);
                    total += counts[i];
                }
            }
            
            // Calculate percentages
            JSONObject json = new JSONObject();
            for (int i = 0; i < categories.length; i++) {
                int percentage = (total == 0) ? 0 : (counts[i] * 100) / total;
                json.put(categories[i], percentage);
            }

            out.print(json);
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.java.fullstack;
import java.io.IOException;
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

@WebServlet("/checkStatus")
public class CheckWorkStatusServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int rId = Integer.parseInt(request.getParameter("rId"));
        response.setContentType("application/json");

        try {
            Connection conn = DataBaseUtility.getConnection();
            
            String sql = "SELECT status FROM Requestservices WHERE rId = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, rId);
            ResultSet rs = pstmt.executeQuery();

            JSONObject json = new JSONObject();
            if (rs.next()) {
                json.put("status", rs.getString("status"));
            } else {
                json.put("status", "not_found");
            }

            response.getWriter().write(json.toString());
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"status\":\"error\"}");
        }
    }
}

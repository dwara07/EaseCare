package com.java.fullstack;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/Validation")
public class Validation extends HttpServlet {
    private static final long serialVersionUID = 1L;
	private static DataBaseUtility db;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Validation Servlet Invoked");
        db = DataBaseUtility.getInstance();
//        db.connectToDB();
        response.setContentType("text/html");

        // Retrieve username and password from request
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Check if credentials are empty
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            response.sendRedirect("loginForm.html?login=empty");
            return;  // Stop further execution
        }

        // Validate credentials in database
        String query = "SELECT * FROM User_Credentials WHERE user_name = ? AND user_password = ?";
        try (Connection conn = DataBaseUtility.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Store username in session
                    HttpSession session = request.getSession();
                    session.setAttribute("name", username);
                    session.setAttribute("uid", DataBaseUtility.getInstance().getUserId(username));
            		System.out.println("uid" + DataBaseUtility.getInstance().getUserId(username));
            		session.setMaxInactiveInterval(12 * 60 * 60); // 12 hours

                    response.sendRedirect("dashboard.html");
                    return;  // Stop further execution
                } else {
                    response.sendRedirect("loginForm.html?login=error");
                    return;  // Stop further execution
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("loginForm.html?login=error");
            return;  // Stop further execution
        }
    }

}

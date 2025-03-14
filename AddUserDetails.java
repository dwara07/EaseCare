package com.java.fullstack;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/AddUserDetails")

public class AddUserDetails extends HttpServlet {
	private static DataBaseUtility db; // Database utility instance

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		db = DataBaseUtility.getInstance();
//		db.connectToDB();
		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();

		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String confirmPassword = request.getParameter("conPwd");
		String emailID = request.getParameter("emailId");
		String role = request.getParameter("role");
		System.out.println(role);
//		RequestDispatcher rd;

		// Password confirmation check
		if (!password.equals(confirmPassword)) {
			pw.println("<script>alert('Passwords do not match. Please try again.'); window.history.back();</script>");
			return;
		}
		
		// ✅ Correct parameterized query
		String query = "INSERT INTO User_Credentials (user_name, user_password, user_gmail, user_role) VALUES (?, ?, ?, ?)";
		try{
			Connection conn = DataBaseUtility.getConnection();
			PreparedStatement stmt = conn.prepareStatement(query);
			
			stmt.setString(1, username);
			stmt.setString(2, password);
			stmt.setString(3, emailID);
			stmt.setString(4, role);
			
			int result = stmt.executeUpdate();

			if (result == 1) {
				db.insertIntoNotificationSetting(db.getUserId(username));
				storeUserNameInSession(username, request);
				System.out.println("ses " + request.getSession().getAttribute("name").toString());
				pw.println("<script>window.location='personal_information.html';</script>");
			} else {
	            response.sendRedirect("register.html?register=error");
			}
		} catch (SQLException e) {
            response.sendRedirect("register.html?register=error");
			e.printStackTrace();
		}
		
		String profileQuery = "INSERT INTO user_profile (user_id) VALUES (?)";
		try {
			db.executePrepared(profileQuery, db.getUserId(username));
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void storeUserNameInSession(String name, HttpServletRequest req) {
		HttpSession session = req.getSession(true);
		session.setAttribute("name", name); // Store user name in session
		session.setAttribute("uid", DataBaseUtility.getInstance().getUserId(name));
		System.out.println("uid" + DataBaseUtility.getInstance().getUserId(name));
		session.setMaxInactiveInterval(12 * 60 * 60); // 12 hours
	}
}

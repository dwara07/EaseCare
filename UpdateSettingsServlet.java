package com.java.fullstack;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/UpdateSettings")
public class UpdateSettingsServlet extends HttpServlet {

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		DataBaseUtility db = DataBaseUtility.getInstance();
		HttpSession session = request.getSession();
		int userId = db.getUserId((String) session.getAttribute("name"));

		try (Connection conn = DataBaseUtility.getConnection()) {
			// Extract form data
			String country = request.getParameter("country");
			String state = request.getParameter("state");
			String city = request.getParameter("city");
			String street = request.getParameter("street");
			String pinCode = request.getParameter("pin_code");

			// ✅ Update Personal_Information Table
			String updateAddressSQL = "UPDATE Personal_Information SET country=?, state=?, city=?, street=?, pin_code=? WHERE u_id=?";
			try (PreparedStatement ps = conn.prepareStatement(updateAddressSQL)) {
				ps.setString(1, country);
				ps.setString(2, state);
				ps.setString(3, city);
				ps.setString(4, street);
				ps.setInt(5, Integer.parseInt(pinCode));
				ps.setInt(6, userId);
				ps.executeUpdate();
			}

			if (request.getParameter("isCaretaker").equals("true")) {
				boolean notifyAll = Boolean.parseBoolean(request.getParameter("notify_all"));
				boolean receiveMail = Boolean.parseBoolean(request.getParameter("receive_mail_from_carelink"));
				String theme = request.getParameter("theme"); // ✅ Get theme from form

				// ✅ Update notificationSetting Table
				String updateNotificationsSQL = "UPDATE notificationSetting SET notify_all=?, receive_mail_from_carelink=?, theme=? WHERE user_id=?";
				try (PreparedStatement ps = conn.prepareStatement(updateNotificationsSQL)) {
					ps.setBoolean(1, notifyAll);
					ps.setBoolean(2, receiveMail);
					ps.setString(3, theme); // ✅ Update theme
					ps.setInt(4, userId);
					ps.executeUpdate();
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

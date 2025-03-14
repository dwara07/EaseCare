package com.java.fullstack;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/requestService")
public class RequestService extends HttpServlet {
	private DataBaseUtility db;

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		System.out.println("innssssss");
		System.out.println("Processing service request...");
		db = DataBaseUtility.getInstance();
//        db.connectToDB();

		extractServiceFormInputs(req, res);
		extractCareTakerPreference(req, res);
		joinRequestWithPreference(req, res);
	}

	private void insertInNotificationTable(String personRequired, int reqId, int serviceSeekerId , HttpServletRequest req) {
	    System.out.println("Person Required: " + personRequired);
	    System.out.println("Request ID: " + reqId + "  Service seeker : " + serviceSeekerId);

	    String caretakersQuery = "SELECT user_id FROM User_Credentials WHERE user_role = 'caretaker'";

	    try (Connection conn = DataBaseUtility.getConnection();
	         PreparedStatement caretakerStmt = conn.prepareStatement(caretakersQuery)) {

	        conn.setAutoCommit(false); // Start transaction

	        ResultSet rs = caretakerStmt.executeQuery();

	        String insertQuery = "INSERT INTO NotificationInformation (message, read_status, user_id, request_id, service_id, notify_type) VALUES (?, 'unread', ?, ?, ?, ?)";
	        String selectQuery = "SELECT notification_id FROM NotificationInformation WHERE user_id = ? ORDER BY notification_id DESC LIMIT 1";

	        try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
	             PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {

	            while (rs.next()) {
	                int caretakerId = rs.getInt("user_id");
	                String message = "**New Service Request!** Someone nearby needs " + personRequired + ". Check details & apply if interested!";

	                insertStmt.setString(1, message);
	                insertStmt.setInt(2, caretakerId);
	                insertStmt.setInt(3, reqId);
	                insertStmt.setInt(4, serviceSeekerId);
	                insertStmt.setString(5, "request");
	                
	                insertStmt.executeUpdate();  // Insert into table

	                // Retrieve the last inserted notification_id for this caretaker
	                selectStmt.setInt(1, caretakerId);
	                ResultSet notificationRs = selectStmt.executeQuery();
	                if (notificationRs.next()) {
	                    int notificationId = notificationRs.getInt("notification_id");
	                    System.out.println("nooooo  " + notificationId);
	                    db.insertIntoUserActivity(notificationId, db.getUserId((String) req.getSession().getAttribute("name")));
	                    System.out.println("✅ Notification ID: " + notificationId);
	                }
	            }
	            conn.commit();
	            System.out.println("✅ Notifications added successfully.");
	        } catch (SQLException e) {
	            conn.rollback();
	            System.out.println("❌ Transaction failed, changes rolled back.");
	            e.printStackTrace();
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.out.println("❌ Error: " + e.getMessage());
	    }
	}



	private void extractServiceFormInputs(HttpServletRequest req, HttpServletResponse res)
	        throws ServletException, IOException {
	    PrintWriter out = res.getWriter();
	    String title = req.getParameter("tittle");
	    String[] required = req.getParameterValues("required");
	    String servicesList = (required != null) ? String.join(",", required) : "None";
	    String startTime = req.getParameter("start");
	    String endTime = req.getParameter("end");
	    String desc = req.getParameter("description");
	    String amount = "RS "+req.getParameter("amount");
	    int userId = db.getUserId((String) req.getSession().getAttribute("name"));

	    System.out.println("Service Request Info - UID: " + userId);
	    
	    String query = "INSERT INTO Requestservices (tittle, required, start_time, end_time, description, amountWithUnit, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
	    try (Connection conn = DataBaseUtility.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(query)) {

	        stmt.setString(1, title);
	        stmt.setString(2, servicesList);
	        stmt.setString(3, startTime);
	        stmt.setString(4, endTime);
	        stmt.setString(5, desc);
	        stmt.setString(6, amount);
	        stmt.setInt(7, userId);
	        stmt.executeUpdate();

	        System.out.println("✅ Service request inserted.");

	        int reqId = getRequestId(req, res);
	        insertInNotificationTable(servicesList, reqId, userId , req);

	    } catch (SQLException e) {
	        System.out.println("❌ Error: " + e.getMessage());
	        e.printStackTrace();
	        out.println("Error: " + e.getMessage());
	    }
	}
	
	private void extractCareTakerPreference(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String gender = req.getParameter("gender");
		String education = req.getParameter("education");
		int minAge = Integer.parseInt(req.getParameter("min"));
		int maxAge = Integer.parseInt(req.getParameter("max"));

		System.out.println("Caretaker Preferences:");
		System.out.println("Gender: " + gender);
		System.out.println("Education: " + education);
		System.out.println("Age Range: " + minAge + " - " + maxAge);

		String query = "INSERT INTO CareTakerPreferences (gender, education, min_age, max_age) VALUES (?, ?, ?, ?)";
		try {
			Connection conn = DataBaseUtility.getConnection();
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setString(1, gender);
			stmt.setString(2, education);
			stmt.setInt(3, minAge);
			stmt.setInt(4, maxAge);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e);
			res.getWriter().println("Error: " + e.getMessage());
		}
	}

	private void joinRequestWithPreference(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		int reqId = getRequestId(req, res);

		String query = "INSERT INTO RequestPreference (req_id, cp_id) VALUES (?, ?)";
		try {
			Connection conn = DataBaseUtility.getConnection();
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setInt(1, reqId);
			stmt.setInt(2, reqId);
			stmt.executeUpdate();
			RequestDispatcher rd = req.getRequestDispatcher("dashboard.html");
			res.getWriter().println("Form was submitted successfully...");
			rd.forward(req, res);

		} catch (SQLException e) {
			System.out.println(e);

			res.getWriter().println("Error: " + e.getMessage());
		}
	}

	private int getRequestId(HttpServletRequest req, HttpServletResponse res) {
	    int id = 0;
	    String query = "SELECT rId FROM Requestservices WHERE user_id = ? ORDER BY current_timing DESC LIMIT 1";
	    try (Connection conn = DataBaseUtility.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(query)) {

	        stmt.setInt(1, db.getUserId((String) req.getSession().getAttribute("name")));
	        ResultSet rs = stmt.executeQuery();

	        if (rs.next()) {
	            id = rs.getInt("rId");
	        }
	    } catch (SQLException e) {
	        System.out.println("❌ Error getting request ID.");
	        e.printStackTrace();
	    }
	    return id;
	}

}

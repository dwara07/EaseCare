package com.java.fullstack;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

@WebServlet("/fetchNotificationCount")
public class FetchNotificationCountServlet extends HttpServlet {
	private DataBaseUtility db;

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		res.setContentType("application/json");
		db = DataBaseUtility.getInstance();
		Integer hiredNotificationId = null;
		Integer reportNotificationId = null;

		int userId = db.getUserId((String) req.getSession().getAttribute("name"));

		String role = db.getRole(userId);

		if (role.equals("caretaker")) {

			try (Connection conn = DataBaseUtility.getConnection()) {
				// ✅ Update notifications where user & requester are from different cities and
				// notify_all is false
				String checkCityQuery = "SELECT LOWER(p1.city) AS userCity, LOWER(p2.city) AS requesterCity, "
						+ "ni.notification_id, ns.notify_all, ni.notify_type " + "FROM Personal_Information p1 "
						+ "JOIN NotificationInformation ni ON p1.u_id = ni.user_id "
						+ "JOIN Personal_Information p2 ON ni.service_id = p2.u_id "
						+ "JOIN notificationSetting ns ON ni.user_id = ns.user_id "
						+ "WHERE ni.read_status = 'unread' AND ns.notify_all = FALSE";

				try (Connection con = DataBaseUtility.getConnection();
						PreparedStatement checkStmt = con.prepareStatement(checkCityQuery);
						ResultSet rs = checkStmt.executeQuery()) {

					while (rs.next()) {
						String userCity = rs.getString("userCity");
						String requesterCity = rs.getString("requesterCity");
						int notificationId = rs.getInt("notification_id");
						String notifyType = rs.getString("notify_type");

						if (!userCity.equals(requesterCity) && "request".equalsIgnoreCase(notifyType)) { // ✅ Check
																											// notify_type
							String updateQuery = "UPDATE NotificationInformation "
									+ "SET read_status = 'read', showMessage = FALSE " + "WHERE notification_id = ?";

							try (PreparedStatement updateStmt = con.prepareStatement(updateQuery)) {
								updateStmt.setInt(1, notificationId);
								updateStmt.executeUpdate();
							}
						}
					}
				}

				// ✅ Check if the user has request service notifications
				String checkRequestServiceQuery = "SELECT COUNT(*) FROM NotificationInformation WHERE user_id = ? AND isRequestService = TRUE";
				boolean isRequestService = false;

				try (PreparedStatement stmtCheck = conn.prepareStatement(checkRequestServiceQuery)) {
					stmtCheck.setInt(1, userId);
					ResultSet rsCheck = stmtCheck.executeQuery();
					if (rsCheck.next() && rsCheck.getInt(1) > 0) {
						isRequestService = true;
					}
				}

				// ✅ Fetch unread notification count
				String query;
				if (isRequestService) {
					query = "SELECT COUNT(ni.notification_id) AS unread_count FROM NotificationInformation ni "
							+ "JOIN notificationSetting ns ON ns.user_id = ni.user_id "
							+ "LEFT JOIN Requestservices rs ON rs.rId = ni.request_id "
							+ "LEFT JOIN Personal_Information pi_requester ON pi_requester.u_id = rs.user_id "
							+ "JOIN Personal_Information pi_current ON pi_current.u_id = ni.user_id "
							+ "WHERE ni.user_id = ? " + "AND ni.read_status = 'unread' " + "AND ni.showMessage = TRUE "
							+ "AND (ns.notify_all = TRUE OR pi_requester.u_id IS NULL OR LOWER(pi_current.city) = LOWER(pi_requester.city));";

				} else {
					query = "SELECT COUNT(notification_id) AS unread_count FROM NotificationInformation "
							+ "WHERE user_id = ? " + "AND read_status = 'unread' " + "AND showMessage = TRUE";
				}

				JSONObject responseJson = new JSONObject();

				try (PreparedStatement stmt = conn.prepareStatement(query)) {
					stmt.setInt(1, userId);
					ResultSet rs = stmt.executeQuery();
					if (rs.next()) {
						responseJson.put("isRequest", isRequestService);
						responseJson.put("notificationCount", rs.getInt("unread_count"));
					} else {
						responseJson.put("notificationCount", 0);
					}
				}

				// ✅ Fetch latest "hired" notification
				String getLatestHiredNotification = "SELECT ni.notification_id, ni.message, uc.user_name "
						+ "FROM NotificationInformation ni " + "JOIN User_Credentials uc ON ni.service_id = uc.user_id "
						+ "WHERE ni.user_id = ? AND ni.notify_type = 'hiring' " + "ORDER BY ni.created_at DESC LIMIT 1";

				try (PreparedStatement latestHiredStmt = conn.prepareStatement(getLatestHiredNotification)) {
					latestHiredStmt.setInt(1, userId);
					ResultSet rsLatestHired = latestHiredStmt.executeQuery();
					if (rsLatestHired.next()) {
					    hiredNotificationId = rsLatestHired.getInt("notification_id");
					    
					    // Check if this notification is already shown in UI
					    String checkIsAddedQuery = "SELECT isAddedInUI FROM User_Activity WHERE notification_id = ?";
					    boolean isAdded = false;

					    try (PreparedStatement checkStmt = conn.prepareStatement(checkIsAddedQuery)) {
					        checkStmt.setInt(1, hiredNotificationId);
					        ResultSet rsCheck = checkStmt.executeQuery();
					        if (rsCheck.next()) {
					            isAdded = rsCheck.getBoolean("isAddedInUI");
					        }
					    }
					    if (!isAdded) {
					        responseJson.put("message", rsLatestHired.getString("message"));
					        responseJson.put("name", rsLatestHired.getString("user_name"));

					        // Update isAddedInUI to TRUE after sending message
					        String updateUIQuery = "UPDATE User_Activity SET isAddedInUI = TRUE WHERE notification_id = ?";
					        try (PreparedStatement updateStmt = conn.prepareStatement(updateUIQuery)) {
					            updateStmt.setInt(1, hiredNotificationId);
					            updateStmt.executeUpdate();
					        }
					    } else {
					        responseJson.put("message", "empty");
					    }
					}

				}
				responseJson.put("hasHiredNotification", hiredNotificationId != null);
				responseJson.put("id", hiredNotificationId != null ? hiredNotificationId : JSONObject.NULL);
				
				
				// ✅ Fetch latest "feedback" notification
				String getLatestFeedbackNotification = "SELECT ni.notification_id, ni.message, uc.user_name "
				        + "FROM NotificationInformation ni "
				        + "JOIN User_Credentials uc ON ni.service_id = uc.user_id "
				        + "WHERE ni.user_id = ? AND ni.notify_type = 'feedback' "
				        + "ORDER BY ni.created_at DESC LIMIT 1";

				try (PreparedStatement latestFeedbackStmt = conn.prepareStatement(getLatestFeedbackNotification)) {
				    latestFeedbackStmt.setInt(1, userId);
				    ResultSet rsLatestFeedback = latestFeedbackStmt.executeQuery();
				    if (rsLatestFeedback.next()) {
				        int feedbackNotificationId = rsLatestFeedback.getInt("notification_id");

				        // Check if this feedback notification is already shown in UI
				        String checkIsAddedQuery = "SELECT isAddedInUI FROM User_Activity WHERE notification_id = ?";
				        boolean isAdded = false;

				        try (PreparedStatement checkStmt = conn.prepareStatement(checkIsAddedQuery)) {
				            checkStmt.setInt(1, feedbackNotificationId);
				            ResultSet rsCheck = checkStmt.executeQuery();
				            if (rsCheck.next()) {
				                isAdded = rsCheck.getBoolean("isAddedInUI");
				            }
				        }

				        if (!isAdded) {
				            responseJson.put("message", rsLatestFeedback.getString("message"));
				            responseJson.put("name", rsLatestFeedback.getString("user_name"));

				            // Update isAddedInUI to TRUE after sending message
				            String updateUIQuery = "UPDATE User_Activity SET isAddedInUI = TRUE WHERE notification_id = ?";
				            try (PreparedStatement updateStmt = conn.prepareStatement(updateUIQuery)) {
				                updateStmt.setInt(1, feedbackNotificationId);
				                updateStmt.executeUpdate();
				            }
				            responseJson.put("hasFeedbackNotification", true);
				        } else {
				            responseJson.put("message", "empty");
				            responseJson.put("hasFeedbackNotification", false);
				        }

				        responseJson.put("hasFeedbackNotification", true);
				        responseJson.put("feedbackId", feedbackNotificationId);
				    } else {
				        responseJson.put("hasFeedbackNotification", false);
				        responseJson.put("feedbackId", JSONObject.NULL);
				    }
				}

				

				// ✅ Fetch latest "report" notification
				String getLatestReportNotification = "SELECT ni.notification_id, ni.message, uc.user_name "
						+ "FROM NotificationInformation ni " + "JOIN User_Credentials uc ON ni.service_id = uc.user_id "
						+ "WHERE ni.user_id = ? AND ni.notify_type = 'report' " + "ORDER BY ni.created_at DESC LIMIT 1";

				try (PreparedStatement latestReportStmt = conn.prepareStatement(getLatestReportNotification)) {
					latestReportStmt.setInt(1, userId);
					ResultSet rsLatestReport = latestReportStmt.executeQuery();
					if (rsLatestReport.next()) {
					    reportNotificationId = rsLatestReport.getInt("notification_id");

					    // Check if this notification is already shown in UI
					    String checkIsAddedQuery = "SELECT isAddedInUI FROM User_Activity WHERE notification_id = ?";
					    boolean isAdded = false;

					    try (PreparedStatement checkStmt = conn.prepareStatement(checkIsAddedQuery)) {
					        checkStmt.setInt(1, reportNotificationId);
					        ResultSet rsCheck = checkStmt.executeQuery();
					        if (rsCheck.next()) {
					            isAdded = rsCheck.getBoolean("isAddedInUI");
					        }
					    }

					    if (!isAdded) {
					        responseJson.put("message", rsLatestReport.getString("message"));
					        responseJson.put("name", rsLatestReport.getString("user_name"));

					        // Update isAddedInUI to TRUE after sending message
					        String updateUIQuery = "UPDATE User_Activity SET isAddedInUI = TRUE WHERE notification_id = ?";
					        try (PreparedStatement updateStmt = conn.prepareStatement(updateUIQuery)) {
					            updateStmt.setInt(1, reportNotificationId);
					            updateStmt.executeUpdate();
					        }
					    } else {
					        responseJson.put("message", "empty");
					    }
					}

				}

				if (reportNotificationId == null && hiredNotificationId != null) {
					db.insertIntoUserActivity(hiredNotificationId, userId);
				}
				if (reportNotificationId != null && hiredNotificationId == null) {
					db.insertIntoUserActivity(reportNotificationId, userId);
				}

		        responseJson.put("isRequest", false);
				responseJson.put("hasReportNotification", reportNotificationId != null);
				responseJson.put("id",
						reportNotificationId != null ? reportNotificationId : JSONObject.NULL);

				// ✅ Send response
				res.getWriter().println(responseJson);

			} catch (SQLIntegrityConstraintViolationException e) {
				System.out.println(e.getMessage());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		else { // get notification count for need-service people
			JSONObject responseJson = new JSONObject();

			try (Connection conn = DataBaseUtility.getConnection()) {
				// ✅ Count notifications where notify_type = 'hired' and service_id = userId
				String hiredNotificationQuery = "SELECT COUNT(notification_id) AS hired_count FROM NotificationInformation "
						+ "WHERE notify_type = 'hired' AND user_id = ? AND read_status = 'unread' AND showMessage = TRUE";

				try (PreparedStatement stmt = conn.prepareStatement(hiredNotificationQuery)) {
					stmt.setInt(1, userId);
					ResultSet rs = stmt.executeQuery();
					if (rs.next()) {
						responseJson.put("count", rs.getInt("hired_count"));
					} else {
						responseJson.put("count", 0);
					}
				}
				// ✅ Send response
				res.getWriter().println(responseJson);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}
}
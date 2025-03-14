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

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/getJobDetails")
public class GetJobDetails extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		String action = request.getParameter("action");
		String jobId = request.getParameter("jobId");

		PrintWriter out = response.getWriter();
		JSONObject jsonResponse = new JSONObject();

		if (action == null || jobId == null) {
			jsonResponse.put("status", "error");
			jsonResponse.put("message", "Missing action or jobId parameter");
			out.print(jsonResponse.toString());
			return;
		}

		try (Connection conn = DataBaseUtility.getConnection()) {
			String sql;
			if ("fetchJobDetails".equals(action)) {
				sql = "SELECT r.rId, n.notification_id, r.tittle, r.user_id, r.required, "
						+ "r.start_time, r.end_time, r.description, r.amountWithUnit, r.status "
						+ "FROM Requestservices r " + "LEFT JOIN NotificationInformation n ON r.rId = n.request_id "
						+ "WHERE r.rId = ?";
			} else if ("showServiceDetails".equals(action)) {
				sql = "SELECT n.notification_id, n.message, n.created_at, u.user_name, u.user_gmail, r.required "
						+ "FROM NotificationInformation n " + "JOIN User_Credentials u ON n.user_id = u.user_id "
						+ "JOIN Requestservices r ON n.request_id = r.rId " + // Added JOIN to get 'required' field
						"WHERE n.request_id = ?";
			} else {
				jsonResponse.put("status", "error");
				jsonResponse.put("message", "Invalid action");
				out.print(jsonResponse.toString());
				return;
			}

			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, Integer.parseInt(jobId));
			ResultSet rs = stmt.executeQuery();

			JSONArray dataArray = new JSONArray();
			while (rs.next()) {
				JSONObject job = new JSONObject();
				if ("fetchJobDetails".equals(action)) {
					job.put("id", rs.getInt("notification_id"));
					job.put("rId", rs.getInt("rId"));
					job.put("requesterName", DataBaseUtility.getUserName(rs.getInt("user_id")));
					job.put("title", rs.getString("tittle"));
					job.put("category", rs.getString("required")); // Map 'required' to 'category'
					job.put("startTime", rs.getString("start_time"));
					job.put("endTime", rs.getString("end_time"));
					job.put("description", rs.getString("description"));
					job.put("amountWithUnit", rs.getString("amountWithUnit"));
					job.put("status", rs.getString("status"));
				} else {
					job.put("id", rs.getInt("notification_id"));
					job.put("requesterName", rs.getString("user_name"));
					job.put("notificationId", rs.getInt("notification_id"));
					job.put("message", rs.getString("message"));
					job.put("createdAt", rs.getTimestamp("created_at").toString());
					job.put("userName", rs.getString("user_name"));
					job.put("userEmail", rs.getString("user_gmail"));
					job.put("category", rs.getString("required")); // Added category in else part
				}
				dataArray.put(job);
			}

			jsonResponse.put("status", "success");
			jsonResponse.put("data", dataArray);
		} catch (Exception e) {
			jsonResponse.put("status", "error");
			jsonResponse.put("message", e.getMessage());
			e.printStackTrace();
		}
		out.print(jsonResponse.toString());
	}
}

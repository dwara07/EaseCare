package com.java.fullstack;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/service")
public class Service extends HttpServlet{
	// Database utility instance for database operations
	private static DataBaseUtility db;


	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
//		db = DataBaseUtility.getInstance();
//		db.connectToDB();
		
		jsonResponse(req, res);
	}

	private void jsonResponse(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		// Set response content type to JSON
		res.setContentType("application/json");
		// Prepared SQL statement
		String query = "SELECT r.tittle, r.required, r.user_id, r.rId, a.state, a.city " + "FROM Requestservices r "
				+ "JOIN Personal_Information a ON r.user_id = a.u_id " + "WHERE r.status = ?";

		// Initialize JSON array for response
		JSONArray responseArray = new JSONArray();

		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			db = DataBaseUtility.getInstance();
//			DataBaseUtility.connectToDB();
			
			// Prepare statement
			pstmt = DataBaseUtility.getConnection().prepareStatement(query);

			// Set the status parameter
			pstmt.setString(1, "open");

			// Execute query and get result set
			rs = pstmt.executeQuery();

			// Iterate through results and build JSON response
			while (rs.next()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("tittle", rs.getString("tittle"));
				jsonObject.put("required", rs.getString("required"));
				jsonObject.put("id", rs.getString("rId"));
				jsonObject.put("place", rs.getString("state") + "," + rs.getString("city"));

				responseArray.put(jsonObject);
			}
			System.out.println(responseArray.toString());
			// Write JSON array to response
			res.getWriter().write(responseArray.toString());
			
		} catch (SQLException e) {
			// Handle any errors and return error message
			e.printStackTrace();
			res.getWriter().write("{\"error\": \"An error occurred while processing your request.\"}");
		} 
	}
}
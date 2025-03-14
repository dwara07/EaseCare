package com.java.fullstack;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/fetchSavedJobs")
public class FetchSavedJobsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        int userId = DataBaseUtility.getInstance().getUserId((String) req.getSession().getAttribute("name"));
        JSONArray jobsArray = new JSONArray();

        try (Connection conn = DataBaseUtility.getConnection()) {
            String role = DataBaseUtility.getInstance().getRole((String) req.getSession().getAttribute("name"));

            if (role.equals("caretaker")) {
                // ✅ Step 1: Retrieve notification_id, message, and user_name (requesterName)
                String selectQuery = "SELECT ua.notification_id, ni.message, uc.user_name AS requesterName "
                        + "FROM User_Activity ua "
                        + "JOIN NotificationInformation ni ON ua.notification_id = ni.notification_id "
                        + "LEFT JOIN User_Credentials uc ON ni.service_id = uc.user_id " // Fetch requester name
                        + "WHERE ua.user_id = ?";

                try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                    selectStmt.setInt(1, userId);
                    ResultSet rs = selectStmt.executeQuery();

                    // ✅ Step 2: Collect messages in JSON format
                    while (rs.next()) {
                        JSONObject job = new JSONObject();
                        job.put("id", rs.getInt("notification_id"));
                        job.put("title", rs.getString("message"));
                        job.put("requesterName", rs.getString("requesterName")); // ✅ Added requester name

                        jobsArray.put(job);
                    }
                }
                res.getWriter().write(jobsArray.toString());
                return;
            } else {
                String selectQuery = "SELECT ua.notification_id, ni.request_id, ni.notify_type, ni.message "
                        + "FROM User_Activity ua "
                        + "JOIN NotificationInformation ni ON ua.notification_id = ni.notification_id "
                        + "WHERE ua.user_id = ? AND ua.isAddedInUI = FALSE";

                try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                    selectStmt.setInt(1, userId);
                    ResultSet rs = selectStmt.executeQuery();

                    while (rs.next()) {
                        JSONObject job = new JSONObject();
                        int notificationId = rs.getInt("notification_id");
                        job.put("id", notificationId);

                        String notifyType = rs.getString("notify_type");

                        if ("request".equals(notifyType)) {
                            int requestId = rs.getInt("request_id");

                            // Validate request_id exists
                            if (requestId > 0) {
                                String requestQuery = "SELECT tittle, required FROM Requestservices WHERE rId = ?";

                                try (PreparedStatement requestStmt = conn.prepareStatement(requestQuery)) {
                                    requestStmt.setInt(1, requestId);
                                    ResultSet reqRs = requestStmt.executeQuery();

                                    if (reqRs.next()) {
                                        job.put("title", reqRs.getString("tittle")); // Fixed 'tittle' typo
                                        job.put("required", reqRs.getString("required"));
                                    } else {
                                        job.put("title", "Unknown Request");
                                        job.put("required", "N/A");
                                    }
                                }
                            } else {
                                job.put("title", "Invalid Request ID");
                                job.put("required", "N/A");
                            }
                        } else {
                            job.put("title", rs.getString("message"));
                        }

                        jobsArray.put(job);
                    }
                }
                res.getWriter().write(jobsArray.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception
        }

    }
}

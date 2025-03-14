package com.java.fullstack;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/serviceComposition")
public class CareTakerComposition extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();

        try {
            int userId = DataBaseUtility.getInstance().getUserId((String) request.getSession().getAttribute("name"));
            Map<String, Integer> serviceCount = new HashMap<>();

            try (Connection conn = DataBaseUtility.getConnection()) {
                // Step 1: Get all request_id where user was hired
                String hiredQuery = "SELECT request_id FROM NotificationInformation WHERE service_id = ? AND notify_type = 'hired'";
                try (PreparedStatement stmt = conn.prepareStatement(hiredQuery)) {
                	System.out.println("ins comp " + userId);
                    stmt.setInt(1, userId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            int requestId = rs.getInt("request_id");

                            // Step 2: Get 'required' field from Requestservices
                            String requiredQuery = "SELECT required FROM Requestservices WHERE rId = ?";
                            try (PreparedStatement reqStmt = conn.prepareStatement(requiredQuery)) {
                                reqStmt.setInt(1, requestId);
                                try (ResultSet reqRs = reqStmt.executeQuery()) {
                                    if (reqRs.next()) {
                                        String required = reqRs.getString("required");

                                        // Step 3: Split and count services
                                        String[] services = required.split(",");
                                        for (String service : services) {
                                            service = service.trim();
                                            serviceCount.put(service, serviceCount.getOrDefault(service, 0) + 1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Step 4: Calculate percentages
            int totalServices = serviceCount.values().stream().mapToInt(Integer::intValue).sum();
            JSONArray serviceArray = new JSONArray();
            for (Map.Entry<String, Integer> entry : serviceCount.entrySet()) {
                JSONObject serviceObj = new JSONObject();
                serviceObj.put("name", entry.getKey());
                serviceObj.put("percentage", (totalServices == 0) ? 0 : (entry.getValue() * 100) / totalServices);
                serviceArray.put(serviceObj);
            }

            jsonResponse.put("services", serviceArray);
        } catch (Exception e) {
            jsonResponse.put("error", "Database error: " + e.getMessage());
        }

        out.print(jsonResponse.toString());
    }
}

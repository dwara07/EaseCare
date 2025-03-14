package com.java.fullstack;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

@WebServlet("/serviceDetails")
public class ServiceDetailsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        JSONObject jsonResponse;

        try {
            int serviceId = Integer.parseInt(request.getParameter("serviceId"));
            jsonResponse = DataBaseUtility.getServiceDetails(serviceId);
        } catch (Exception e) {
            jsonResponse = new JSONObject().put("error", "Invalid request");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        out.print(jsonResponse.toString());
        out.flush();
    }
}

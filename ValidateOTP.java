package com.java.fullstack;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/ValidateOTP")
public class ValidateOTP extends HttpServlet {
	
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int enteredOtp = Integer.parseInt(request.getParameter("otp"));
        HttpSession session = request.getSession();
        String name = request.getParameter("username");
        int generatedOtp = (int) session.getAttribute("otp");

        if (enteredOtp == generatedOtp) {
            // OTP is correct
//        	AddUserDetails.storeUserNameInSession(name , request);
        	request.getSession().setAttribute("name", name);
        	System.out.println("otp   "+request.getSession().getAttribute("name"));
    		session.setMaxInactiveInterval(12 * 60 * 60); // 12 hours

        	System.out.println("sss kjhg");
        	
        	RequestDispatcher rd = request.getRequestDispatcher("dashboard.html");
        	rd.forward(request, response);
        	
        	
//            response.sendRedirect("dashboard.html");
        } else {
            // OTP is incorrect
            response.sendRedirect("otp_verification.html?otp=error");
        }
    }
}


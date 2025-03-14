package com.videocall;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/storeRequestedPersonName")

public class StoreRequestedPersonNameInSession extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		System.out.println("ins redirect....");
		String requestedPersonName = req.getParameter("name");
		req.getSession().setAttribute("requestedPersonName", requestedPersonName);
		
	}
	

}

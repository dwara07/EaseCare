package com.java.fullstack;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
	
@WebFilter("/dashboard.html")

public class EntryValidationFilter extends HttpFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	    System.out.println("EntryValidationFilter initialized and mapped to: /dashboard.html");
	}


	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		System.out.println("Requested URL: " + req.getRequestURI());

		System.out.println("Inside EntryValidationFilter...");

		HttpSession session = req.getSession(false); // Don't create a new session if it doesn't exist
		if (session == null || session.getAttribute("name") == null) {
			System.out.println("User not logged in. Redirecting to register.html...");
			res.sendRedirect("register.html");
			return;
		}

		// Set session timeout (only if session exists)
		session.setMaxInactiveInterval(12 * 60 * 60); // 12 hours

		System.out.println("User validated: " + session.getAttribute("name"));

		// Proceed to the next filter or resource
		chain.doFilter(request, response);
	}

	public void destroy() {
		com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.uncheckedShutdown();
		System.out.println("MySQL cleanup thread stopped.");
		System.out.println("JDBC driver unregistered successfully.");
		System.out.println("EntryValidationFilter destroyed...");
	}

}

package com.java.fullstack;


import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/getRole")

public class GetRole extends HttpServlet {
	
	public void doPost(HttpServletRequest req , HttpServletResponse res) throws IOException  , ServletException{
		String name = (String) req.getSession(false).getAttribute("name");
		System.out.println("ses get role " + name);
		if(name == null) {
			res.sendRedirect("register.html");
			return;
		}
		DataBaseUtility db = DataBaseUtility.getInstance();
		ResultSet rs = db.executeQuery("SELECT user_role FROM User_Credentials WHERE user_name = '" + name + "'");
		try {
			if(rs.next()) {
				System.out.println(rs.getString(1));
				res.getWriter().print(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

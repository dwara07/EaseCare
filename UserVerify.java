package com.java.fullstack;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/UserVerify")
public class UserVerify extends HttpServlet {
	private static DataBaseUtility db; // Database utility instance

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		db = DataBaseUtility.getInstance();
//		db.connectToDB();

		String username = request.getParameter("username");
		String useremail = request.getParameter("useremail");

		try {
//			db.connectToDB();

			Connection conn = DataBaseUtility.getConnection();

			String query = "SELECT * FROM User_Credentials WHERE user_name = ? AND user_gmail = ?";
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setString(1, username);
			stmt.setString(2, useremail);

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				int otp = new Random().nextInt(900000) + 100000; // 6-digit OTP
				HttpSession session = request.getSession();
				session.setAttribute("otp", otp);
				session.setAttribute("useremail", useremail);

				// Send OTP via email
				boolean emailSent = sendEmail(useremail, otp);
				if (emailSent) {
					response.sendRedirect("otp_verification.html");
				} else {
		            response.sendRedirect("index.html?error=1");
				}
			} else {
	            response.sendRedirect("index.html?error=1");
			}

		} catch (Exception e) {
			e.printStackTrace();
            response.sendRedirect("index.html?error=1");
		}
	}

	private boolean sendEmail(String recipientEmail, int otp) {
		final String senderEmail = "dwarakesh070706@gmail.com"; // Replace with your email
		final String senderPassword = "dxbg aktm ibye ggru"; // Replace with your email password

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(senderEmail, senderPassword);
			}
		});

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(senderEmail));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
			message.setSubject("Your OTP for Verification");
			message.setText("Your OTP for verification is: " + otp);
			Transport.send(message);
			return true;
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
		}
	}
}

package com.java.fullstack;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.java.websocket.Message;
import com.java.websocket.User;

public class DataBaseUtility {
	private static final String URL = "jdbc:mysql://localhost:3306/CareLink";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "root";

	private static DataBaseUtility instance;

	private DataBaseUtility() {
		// Private constructor
	}

	public static DataBaseUtility getInstance() {
		if (instance == null) {
			synchronized (DataBaseUtility.class) {
				if (instance == null) {
					instance = new DataBaseUtility();
				}
			}
		}
		return instance;
	}
	
	public String[] needServiceDetails(String userName) {
	    String[] details = new String[4]; // {name, email, phone, gender}
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    try {
	        conn = getConnection(); // Assuming you have a method to get a DB connection

	        // Query to get user_id, name, and email from User_Credentials
	        String userQuery = "SELECT user_id, user_name, user_gmail FROM User_Credentials WHERE user_name = ?";
	        pstmt = conn.prepareStatement(userQuery);
	        pstmt.setString(1, userName);
	        rs = pstmt.executeQuery();

	        int userId = -1;
	        if (rs.next()) {
	            userId = rs.getInt("user_id");
	            details[0] = rs.getString("user_name"); // Name
	            details[1] = rs.getString("user_gmail"); // Email
	        }
	        rs.close();
	        pstmt.close();

	        if (userId != -1) {
	            // Query to get phone and gender from Personal_Information
	            String personalQuery = "SELECT phone_number, gender FROM Personal_Information WHERE u_id = ?";
	            pstmt = conn.prepareStatement(personalQuery);
	            pstmt.setInt(1, userId);
	            rs = pstmt.executeQuery();

	            if (rs.next()) {
	                details[2] = rs.getString("phone_number"); // Phone
	                details[3] = rs.getString("gender"); // Gender
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } 
	    return details;
	}

	
	public String getRole(int userId) {
	    String role = "";
	    String query = "SELECT user_role FROM User_Credentials WHERE user_id = ?";

	    try (Connection conn = getConnection(); 
	         PreparedStatement pstmt = conn.prepareStatement(query)) {

	        pstmt.setInt(1, userId);
	        ResultSet rs = pstmt.executeQuery();

	        if (rs.next()) {
	            role = rs.getString("user_role");
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return role;
	}
	public void insertIntoUserActivity(int notificationId, int userId) {
	    String checkQuery = "SELECT COUNT(*) FROM User_Activity WHERE notification_id = ?";
	    String insertQuery = "INSERT INTO User_Activity (notification_id, user_id) VALUES (?, ?)";
System.out.println("bbbb " + notificationId + "isd " + userId);
	    try (Connection conn = getConnection();
	         PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
	         PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

	        // Check if the notificationId already exists
	        checkStmt.setInt(1, notificationId);
	        ResultSet rs = checkStmt.executeQuery();
	        if (rs.next() && rs.getInt(1) > 0) {
	            return;
	        }

	        // Insert if notificationId is not found
	        insertStmt.setInt(1, notificationId);
	        insertStmt.setInt(2, userId);
	        insertStmt.executeUpdate();

	    } catch (SQLException e) {
	    	System.out.println("eerrrr");
	        e.printStackTrace();
	    }
	}


	public void insertIntoNotificationSetting(int userId) {
		String sql = "INSERT INTO notificationSetting (user_id) VALUES (?)";

		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, userId);
			pstmt.executeUpdate();
			System.out.println("Notification setting inserted successfully.");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updatePath(String path, int id) {
		String query = "UPDATE user_profile SET path = ? WHERE user_id = ?"; // Replace `YourTableName` and column names
																				// with actual ones

		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
			// Set the values for the query parameters
			pstmt.setString(1, path); // Set the new path value
			pstmt.setInt(2, id); // Set the record ID to identify the row

			// Execute the update statement
			int rowsAffected = pstmt.executeUpdate();

			// Check if the update was successful
			if (rowsAffected > 0) {
				System.out.println("Path updated successfully for ID: " + id);
			} else {
				System.out.println("No record found with the given ID: " + id);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Error updating path: " + e.getMessage());
		}
	}

	public static JSONObject getServiceDetails(int serviceId) {
	    JSONObject jsonResponse = new JSONObject();
	    try (Connection con = getConnection()) {
	        // Updated query to include profile picture from user_profile table
	        String query = "SELECT r.tittle, r.description, r.required, r.amountWithUnit, " +
	                       "p.u_id, r.start_time, r.end_time, " +
	                       "p.street, p.city, p.state, p.country, p.pin_code, " +
	                       "up.path AS profile_picture " +
	                       "FROM Requestservices r " +
	                       "JOIN Personal_Information p ON r.user_id = p.u_id " +
	                       "LEFT JOIN user_profile up ON p.u_id = up.user_id " + // Join to get profile picture
	                       "WHERE r.rId = ?";

	        PreparedStatement stmt = con.prepareStatement(query);
	        stmt.setInt(1, serviceId);
	        ResultSet rs = stmt.executeQuery();

	        if (rs.next()) {
	            String requestedPersonName = getUserName(rs.getInt("u_id"));
	            String amountWithUnit = rs.getString("amountWithUnit");
	            String tittle = rs.getString("tittle");
	            String description = rs.getString("description");
	            String required = rs.getString("required");
	            String startTime = formatTime(rs.getString("start_time"));
	            String endTime = formatTime(rs.getString("end_time"));
	            String formattedTime = startTime + " - " + endTime;

	            String street = rs.getString("street");
	            String city = rs.getString("city");
	            String state = rs.getString("state");
	            String country = rs.getString("country");
	            String pinCode = rs.getString("pin_code");
	            String fullAddress = street + ", " + city + ", " + state + ", " + country + " - " + pinCode;

	            // Fetch profile picture (default if null)
	            String profilePicture = rs.getString("profile_picture");
	            if (profilePicture == null || profilePicture.isEmpty()) {
	                profilePicture = "https://cdn.vectorstock.com/i/1000v/92/16/default-profile-picture-avatar-user-icon-vector-46389216.jpg";
	            }

	            jsonResponse.put("requestedPersonName", requestedPersonName);
	            jsonResponse.put("tittle", tittle);
	            jsonResponse.put("description", description);
	            jsonResponse.put("required", required);
	            jsonResponse.put("time", formattedTime);
	            jsonResponse.put("fullAddress", fullAddress);
	            jsonResponse.put("amount", amountWithUnit);
	            jsonResponse.put("profile", profilePicture);
	        } else {
	            jsonResponse.put("error", "No service found for the given ID.");
	        }
	    } catch (Exception e) {
	        jsonResponse.put("error", "Server error: " + e.getMessage());
	        e.printStackTrace();
	    }
	    return jsonResponse;
	}


	private static String formatTime(String time) {
		try {
			return time.substring(0, 5) + (Integer.parseInt(time.substring(0, 2)) < 12 ? "AM" : "PM");
		} catch (Exception e) {
			return time;
		}
	}

	public static String getLastSeen(int id) {
		String time = "";
		String query = "SELECT last_seen FROM Last_Seen WHERE user_id = ?";
		try (Connection connection = getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(query)) {

			// Set the id parameter
			preparedStatement.setInt(1, id);

			// Execute the query and retrieve the result
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					time = resultSet.getString("last_seen");
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return time;
	}

	public String getEmail(String name) {
		String email = "";
		String query = "SELECT user_gmail FROM User_Credentials WHERE user_name = ?";

		try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query)) {
			pst.setString(1, name); // Set the user name parameter

			try (ResultSet rs = pst.executeQuery()) {
				if (rs.next()) {
					email = rs.getString("user_gmail"); // Retrieve the user's email
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return email; // Return the user's email
	}
	public JSONArray getAllCaretakerDetails() {
	    JSONArray caretakerList = new JSONArray();
	    String query = "SELECT u.user_id, u.user_name, c.experience, c.description, " +
	                   "a.country, a.state, n.notify_all, n.receive_mail_from_carelink, " +
	                   "up.path AS profileImage " +  // Fetch profile image
	                   "FROM User_Credentials u " +
	                   "JOIN CareTakerInfo c ON u.user_id = c.caretaker_id " +
	                   "JOIN Personal_Information a ON u.user_id = a.u_id " +
	                   "JOIN notificationSetting n ON u.user_id = n.user_id " +
	                   "LEFT JOIN user_profile up ON u.user_id = up.user_id " + // Left join for profile image
	                   "WHERE u.user_role = 'caretaker'";

	    try (Connection conn = getConnection();
	         PreparedStatement stmt = conn.prepareStatement(query);
	         ResultSet rs = stmt.executeQuery()) {

	        while (rs.next()) {
	            JSONObject caretaker = new JSONObject();
	            caretaker.put("caretakerId", rs.getInt("user_id"));
	            caretaker.put("caretakerName", rs.getString("user_name"));
	            caretaker.put("experience", rs.getString("experience"));
	            caretaker.put("description", rs.getString("description"));
	            caretaker.put("country", rs.getString("country"));
	            caretaker.put("state", rs.getString("state"));
	            caretaker.put("profile", rs.getString("profileImage")); // Add profile image
	            
	            caretakerList.put(caretaker);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return caretakerList;
	}

	public String[] getAddressAndNotificationInfoByName(String name) {
		int userId = getUserId(name);
		String[] details = new String[8]; // Updated to include theme
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = DataBaseUtility.getConnection();
			String query = "SELECT a.country, a.state, a.city, a.street, a.pin_code, "
					+ "n.notify_all, n.receive_mail_from_carelink, n.theme " + "FROM Personal_Information a "
					+ "JOIN notificationSetting n ON a.u_id = n.user_id " + "WHERE a.u_id = ?";

			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, userId);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				details[0] = rs.getString("country");
				details[1] = rs.getString("state");
				details[2] = rs.getString("city");
				details[3] = rs.getString("street");
				details[4] = String.valueOf(rs.getInt("pin_code"));
				details[5] = String.valueOf(rs.getBoolean("notify_all"));
				details[6] = String.valueOf(rs.getBoolean("receive_mail_from_carelink"));
				details[7] = rs.getString("theme"); // ✅ Fetch theme
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if (pstmt != null)
					pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return details;
	}

	public String getGmail(String name) {
		String gmail = "";
		String query = "SELECT user_gmail FROM User_Credentials WHERE user_id = ?";
		int id = getUserId(name);

		try (Connection conn = DataBaseUtility.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				gmail = rs.getString("experience");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return gmail;
	}

	public String[] getCareTakerBioDetails(String name) {
		String[] result = new String[5]; // Now storing experience, description, phone_number, gender, services
		int id = getUserId(name);

		String query = "SELECT c.experience, c.description, p.phone_number, p.gender, (SELECT GROUP_CONCAT(s.service_interested SEPARATOR ', ') FROM CareTakerInfo s WHERE s.caretaker_id = c.caretaker_id) AS services FROM CareTakerInfo c JOIN Personal_Information p ON c.caretaker_id = p.u_id WHERE c.caretaker_id = ?";

		try (Connection conn = DataBaseUtility.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				result[0] = rs.getString("experience"); // Store experience
				result[1] = rs.getString("description"); // Store description
				result[2] = rs.getString("phone_number"); // Store phone_number
				result[3] = rs.getString("gender"); // Store gender
				result[4] = rs.getString("services"); // Store services (comma-separated)
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result; // Return array [experience, description, phone_number, gender, services]
	}

	public static void updateLastSeen(int senderId, int receiverId) {
		String query = "UPDATE Last_Seen SET last_seen = CURRENT_TIMESTAMP WHERE user_id = ?";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setInt(1, senderId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Get a new database connection for every request
	public static Connection getConnection() throws SQLException {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return DriverManager.getConnection(URL, USERNAME, PASSWORD);
	}

	// Save chat message to the database
	public static void saveChatMessage(int senderId, int receiverId, String content) {
		String query = "INSERT INTO ChatMessages (sender, receiver, message) VALUES (?, ?, ?)";
		System.out.println("sojnfgaj;werngajr:");

		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setInt(1, senderId);
			pstmt.setInt(2, receiverId);
			pstmt.setString(3, content);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Load chat messages between two users
	public static ResultSet loadUserChats(int senderId, int receiverId) {
		String query = "SELECT * FROM ChatMessages WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) ORDER BY timestamp ASC";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setInt(1, senderId);
			pstmt.setInt(2, receiverId);
			pstmt.setInt(3, receiverId);
			pstmt.setInt(4, senderId);
			return pstmt.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Save the pending message when the receiver is offline
	public static void savePendingMessage(int receiverId, String message) {
		// SQL query to insert the pending message into the database
		String sql = "INSERT INTO pending_messages (receiver_id, message, status) VALUES (?, ?, ?)";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			// Set the parameters for the query
			stmt.setInt(1, receiverId);
			stmt.setString(2, message);
			stmt.setString(3, "pending"); // Mark the message as pending for now

			// Execute the query
			stmt.executeUpdate();

			System.out.println("Pending message saved for user " + receiverId);
		} catch (SQLException e) {
			System.err.println("Error saving pending message: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static List<Message> getChatHistory(int senderId, int receiverId) {
		List<Message> chatHistory = new ArrayList<>();

		// Query to fetch messages between senderId and receiverId
		String query = "SELECT sender, receiver, message, timestamp FROM ChatMessages "
				+ "WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) " + "ORDER BY timestamp ASC";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setInt(1, senderId);
			stmt.setInt(2, receiverId);
			stmt.setInt(3, receiverId);
			stmt.setInt(4, senderId);

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					int sender = rs.getInt("sender");
					int receiver = rs.getInt("receiver");
					String content = rs.getString("message");
					Timestamp timestamp = rs.getTimestamp("timestamp");
					String senderName = getUserName(sender);
					// Create a message object and add it to the list
					Message message = new Message(sender, receiver, content, timestamp, senderName);
					chatHistory.add(message);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// Handle database errors
		}

		return chatHistory;
	}

	public static String getUserName(int id) {
		String name = "";
		String query = "SELECT user_name FROM User_Credentials WHERE user_id = ?";
		try (Connection connection = getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(query)) {

			// Set the id parameter
			preparedStatement.setInt(1, id);

			// Execute the query and retrieve the result
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					name = resultSet.getString("user_name"); // Retrieve the user name
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return name;
	}

	public static List<User> getAllUsersExcept(int loggedInUserId) {
	    List<User> users = new ArrayList<>();
	    String query = "SELECT u.user_id, u.user_name, " +
	                   "COALESCE(up.path, 'https://cdn.vectorstock.com/i/1000v/92/16/default-profile-picture-avatar-user-icon-vector-46389216.jpg') AS profilePicture " +
	                   "FROM User_Credentials u " +
	                   "LEFT JOIN user_profile up ON u.user_id = up.user_id " +
	                   "WHERE u.user_id <> ?";

	    try (Connection con = getConnection();
	         PreparedStatement stmt = con.prepareStatement(query)) {
	        stmt.setInt(1, loggedInUserId);
	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {
	            int userId = rs.getInt("user_id");
	            String username = rs.getString("user_name");
	            String profilePicture = rs.getString("profilePicture");

	            users.add(new User(userId, username, profilePicture));
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return users;
	}

	// Get user ID based on username
	public int getUserId(String name) {
		int id = 0;
		String query = "SELECT user_id FROM User_Credentials WHERE user_name = ?";

		try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query)) {
			pst.setString(1, name);

			try (ResultSet rs = pst.executeQuery()) {
				if (rs.next()) {
					id = rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}

	// Get the count of unread notifications for a caretaker
	public int getUnreadCountForCaretaker(int caretakerId) {
		int unreadCount = 0;
		String query = "SELECT COUNT(*) FROM NotificationInformation WHERE user_id = ? AND read_status = 'unread'";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, caretakerId);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					unreadCount = rs.getInt(1);
					System.out.println("Unread notifications count: " + unreadCount);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return unreadCount;
	}

	// Execute queries like INSERT, UPDATE, DELETE
	public void execute(String query) {
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
			int result = stmt.executeUpdate(query);
			System.out.println("Query executed successfully. Rows affected: " + result);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void executePrepared(String query, Object... params) throws SQLException {
		try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i + 1, params[i]);
			}
			pstmt.executeUpdate();
		}
	}

	// Get the role of a user based on username
	public String getRole(String name) {
		String role = "";
		String query = "SELECT user_role FROM User_Credentials WHERE user_name = ?";
		try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query)) {
			pst.setString(1, name);
			try (ResultSet rs = pst.executeQuery()) {
				if (rs.next()) {
					role = rs.getString(1);
					System.out.println("User role: " + role);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return role;
	}

	// Execute SELECT queries
	public ResultSet executeQuery(String query) {
		try {
			Connection conn = getConnection();
			Statement stmt = conn.createStatement();
			return stmt.executeQuery(query);
		} catch (SQLException se) {
			se.printStackTrace();
		}
		return null;
	}
}

package com.java.fullstack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.json.JSONObject;

@WebServlet("/profile")
@MultipartConfig
public class ProfilePhotoServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 🌟 Get Database instance
        DataBaseUtility db = DataBaseUtility.getInstance();

        // 🌟 Get User ID
        int userId = db.getUserId((String) request.getSession(false).getAttribute("name"));

        // 🌟 Get the uploaded file
        Part filePart = request.getPart("profilePhoto"); 
        String fileName = filePart.getSubmittedFileName();
        System.out.println("Uploaded File Name: " + fileName);

        // 🌟 Get the base path dynamically (Eclipse/Tomcat Safe)
        String realPath = getServletContext().getRealPath("/profile_pictures/");
        System.out.println("Servlet Real Path: " + realPath);

        File realDir = new File(realPath);
        if (!realDir.exists()) {
            boolean created = realDir.mkdirs();
            System.out.println("Created real upload directory: " + created);
        }

        // 🌟 Set final upload path
        String uploadPath = realDir.getAbsolutePath() + File.separator + fileName;
        System.out.println("Final Upload Path: " + uploadPath);

        // 🌟 Save file to the target directory
        File file = new File(uploadPath);
        try (InputStream fileContent = filePart.getInputStream();
             FileOutputStream fos = new FileOutputStream(file)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileContent.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            System.out.println("✅ File successfully written to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ Error writing file: " + e.getMessage());
            e.printStackTrace();
        }

        // 🌟 Update DB Path
        String filePathInDB = "/projectWithFullStack/profile_pictures/" + fileName;
        db.updatePath(filePathInDB, userId);
        updateProfilePath(filePathInDB, userId);

        // 🌟 Send JSON response
        response.getWriter().print(new JSONObject().put("path", filePathInDB));
    }

    private void updateProfilePath(String path, int id) {
        DataBaseUtility db = DataBaseUtility.getInstance();
        String updateQuery = "UPDATE user_profile SET path = ? WHERE user_id = ?";
        try {
            db.executePrepared(updateQuery, path, id); 
            System.out.println("✅ Profile path updated successfully in DB.");
        } catch (SQLException e) {
            System.err.println("❌ Error updating profile path for user ID " + id + ": " + e.getMessage());
            e.printStackTrace(); 
        }
    }
}

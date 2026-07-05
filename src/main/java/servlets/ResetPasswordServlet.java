package servlets;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

import com.bittercode.util.DBUtil;
import com.bittercode.model.StoreException;

public class ResetPasswordServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String newPassword = req.getParameter("password");
        String email = req.getParameter("email"); // Get email from form parameter
        
        // If email not in parameter, try session
        HttpSession session = req.getSession();
        if (email == null || email.isEmpty()) {
            email = (String) session.getAttribute("email");
        }
        
        System.out.println("Resetting password for email: " + email);

        try {
            // Use existing DBUtil connection
            Connection con = DBUtil.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE users SET password=? WHERE mailid=?");

            ps.setString(1, newPassword);
            ps.setString(2, email);

            int i = ps.executeUpdate();

            if (i > 0) {
                // Password updated successfully
                System.out.println("Password updated successfully for: " + email);
                
                // Clear session email after password reset
                session.removeAttribute("email");
                
                // Set success message in session
                session.setAttribute("message", "Password updated successfully! Please login with your new password.");
                
                // Redirect to login page
                res.sendRedirect("CustomerLogin.html");
                
            } else {
                // No user found with this email
                System.out.println("No user found with email: " + email);
                session.setAttribute("error", "Email not found! Please try again.");
                res.sendRedirect("forgotPassword.html");
            }

        } catch (StoreException | SQLException e) {
            e.printStackTrace();
            session.setAttribute("error", "Database error occurred. Please try again.");
            res.sendRedirect("ForgotPassword.html");
        }
    }
}
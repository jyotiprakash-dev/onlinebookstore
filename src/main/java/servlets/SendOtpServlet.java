package servlets;

import java.io.IOException;
import java.util.Random;

import javax.servlet.*;
import javax.servlet.http.*;

import com.bittercode.util.EmailUtil;

public class SendOtpServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String email = req.getParameter("email");

        // Generate OTP
        Random rand = new Random();
        int otp = 100000 + rand.nextInt(900000);

        HttpSession session = req.getSession();
        session.setAttribute("otp", otp);
        session.setAttribute("email", email);

        // Send Email
        String subject = "Password Reset OTP";
        String message = "Your OTP is: " + otp;

        EmailUtil.sendEmail(email, subject, message);

        // Redirect to OTP page
        res.sendRedirect("VerifyOtp.html");
    }
}
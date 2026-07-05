package servlets;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;

public class VerifyOtpServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        int enteredOtp = Integer.parseInt(req.getParameter("otp"));

        HttpSession session = req.getSession();
        int sessionOtp = (int) session.getAttribute("otp");

        if (enteredOtp == sessionOtp) {
            res.sendRedirect("ResetPassword.html");
        } else {
            res.getWriter().println("Invalid OTP");
        }
    }
}
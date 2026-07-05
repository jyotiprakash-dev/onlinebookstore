package com.bittercode.util;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailUtil {

    public static void sendEmail(String toEmail, String subject, String messageText) {

        final String fromEmail = "velinorreading@gmail.com"; //"your_email@gmail.com"; // sender email
        final String password = "sfyp ipsf kzyz ygyh";//"your_app_password";     // Gmail App Password

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail)
            );

            message.setSubject(subject);
            message.setText(messageText);

            Transport.send(message);

            System.out.println("Email Sent Successfully!");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
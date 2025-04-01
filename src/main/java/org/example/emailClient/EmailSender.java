package org.example.emailClient;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;

public class EmailSender {
    public static boolean sendEmail(String to, String subject, String messageText) {
        Properties config = loadConfig();
        if (config == null) return false; // Return false if config loading fails

        final String username = config.getProperty("email");
        final String password = config.getProperty("password");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", config.getProperty("smtp.host"));
        props.put("mail.smtp.port", config.getProperty("smtp.port"));

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(messageText);
            Transport.send(message);
            System.out.println("Email sent successfully!");
            return true; // Return true if email is sent successfully
        } catch (MessagingException e) {
            e.printStackTrace();
            return false; // Return false if an exception occurs
        }
    }

    private static Properties loadConfig() {
        Properties config = new Properties();
        try (InputStream input = EmailSender.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return null;
            }
            config.load(input);
            return config;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
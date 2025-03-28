package org.example.emailClient;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import java.io.InputStream;
import java.util.*;

public class EmailReceiver {
    public static List<EmailData> fetchEmails(List<String[]> emailSummaries, String folderName) {
        Properties config = loadConfig();
        if (config == null) {
            emailSummaries.add(new String[]{"Error", "loading config", ""});
            return new ArrayList<>();
        }

        final String username = config.getProperty("email");
        final String password = config.getProperty("password");

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", config.getProperty("imap.host"));
        props.put("mail.imaps.port", config.getProperty("imap.port"));
        props.put("mail.imaps.ssl.enable", "true");

        try {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect(username, password);

            Folder folder = store.getFolder(folderName);
            folder.open(Folder.READ_ONLY);

            Message[] messages = folder.getMessages();
            System.out.println("Total emails in " + folderName + ": " + messages.length);
            for (Message msg : messages) {
                System.out.println("Email from: " + msg.getFrom()[0] + ", Subject: " + msg.getSubject());
            }

            List<EmailData> emailList = new ArrayList<>();
            Arrays.sort(messages, (m1, m2) -> {
                try {
                    return m2.getSentDate().compareTo(m1.getSentDate());
                } catch (MessagingException e) {
                    return 0;
                }
            });

            int maxEmails = Math.min(messages.length, 50);
            for (int i = 0; i < maxEmails; i++) {
                Message msg = messages[i];
                String from = msg.getFrom()[0].toString();
                String subject = msg.getSubject();
                String date = msg.getSentDate() != null ? msg.getSentDate().toString() : "Unknown";
                String content = getTextFromMessage(msg);
                emailList.add(new EmailData(from, subject, date, content));
                emailSummaries.add(new String[]{from, subject, date});
            }

            folder.close(false);
            store.close();
            return emailList;

        } catch (Exception e) {
            e.printStackTrace();
            emailSummaries.add(new String[]{"Error", "fetching emails: " + e.getMessage(), ""});
            return new ArrayList<>();
        }
    }

    public static String getTextFromMessage(Message message) throws Exception {
        Object content = message.getContent();
        if (content instanceof String) {
            return (String) content;
        } else if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    result.append((String) bodyPart.getContent());
                } else if (bodyPart.isMimeType("text/html")) {
                    String html = (String) bodyPart.getContent();
                    result.append(stripHtmlTags(html));
                } else if (bodyPart.getContent() instanceof MimeMultipart) {
                    result.append(getTextFromMultipart((MimeMultipart) bodyPart.getContent()));
                }
            }
            return result.length() > 0 ? result.toString() : "[Complex content]";
        }
        return "[Unsupported content type]";
    }

    private static String getTextFromMultipart(Multipart multipart) throws Exception {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append((String) bodyPart.getContent());
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result.append(stripHtmlTags(html));
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    private static String stripHtmlTags(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]+>", "").replaceAll("\\s+", " ").trim();
    }

    private static Properties loadConfig() {
        Properties config = new Properties();
        try (InputStream input = EmailReceiver.class.getClassLoader().getResourceAsStream("config.properties")) {
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
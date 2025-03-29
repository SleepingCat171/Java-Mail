package org.example.emailClient;

import javax.mail.*;
import java.io.InputStream;
import java.util.Properties;

public class EmailManager {
    private static Properties loadConfig() {
        Properties config = new Properties();
        try (InputStream input = EmailManager.class.getClassLoader().getResourceAsStream("config.properties")) {
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

    public static String getFolderName(String folderType) {
        Properties config = loadConfig();
        if (config == null) return null;

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

            Folder[] folders = store.getDefaultFolder().list("*");
            for (Folder folder : folders) {
                String folderName = folder.getFullName();
                if (folderType.equals("Trash") && (folderName.contains("Trash") || folderName.contains("Thùng rác"))) {
                    store.close();
                    return folderName;
                }
                if (folderType.equals("Sent") && (folderName.contains("Sent") || folderName.contains("Thư đã gửi"))) {
                    store.close();
                    return folderName;
                }
            }

            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean deleteEmail(EmailData email, String currentFolder) {
        Properties config = loadConfig();
        if (config == null) return false;

        final String username = config.getProperty("email");
        final String password = config.getProperty("password");

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", config.getProperty("imap.host"));
        props.put("mail.imaps.port", config.getProperty("imap.port"));
        props.put("mail.imaps.ssl.enable", "true");

        Store store = null;
        Folder sourceFolder = null;
        Folder trashFolder = null;

        try {
            Session session = Session.getInstance(props, null);
            store = session.getStore("imaps");
            store.connect(username, password);

            sourceFolder = store.getFolder(currentFolder);
            sourceFolder.open(Folder.READ_WRITE);

            String trashFolderName = getFolderName("Trash");
            if (trashFolderName == null) {
                System.out.println("Trash folder not found");
                return false;
            }
            trashFolder = store.getFolder(trashFolderName);
            trashFolder.open(Folder.READ_WRITE);

            Message[] messages = sourceFolder.getMessages();
            Message targetMessage = null;
            for (Message msg : messages) {
                String msgFrom = msg.getFrom()[0].toString();
                String msgSubject = msg.getSubject() != null ? msg.getSubject() : "";
                String emailSubject = email.getSubject() != null ? email.getSubject() : "";

                System.out.println("Comparing email: ");
                System.out.println("From: " + msgFrom + " | Expected: " + email.getFrom());
                System.out.println("Subject: " + msgSubject + " | Expected: " + emailSubject);

                if (msgFrom.equals(email.getFrom()) && msgSubject.equals(emailSubject)) {
                    targetMessage = msg;
                    break;
                }
            }

            if (targetMessage != null) {
                System.out.println("Found email to delete: " + targetMessage.getSubject());
                sourceFolder.copyMessages(new Message[]{targetMessage}, trashFolder);

                Message[] trashMessages = trashFolder.getMessages();
                boolean foundInTrash = false;
                for (Message trashMsg : trashMessages) {
                    String trashMsgFrom = trashMsg.getFrom()[0].toString();
                    String trashMsgSubject = trashMsg.getSubject() != null ? trashMsg.getSubject() : "";
                    if (trashMsgFrom.equals(email.getFrom()) && trashMsgSubject.equals(email.getSubject())) {
                        foundInTrash = true;
                        break;
                    }
                }

                if (foundInTrash) {
                    System.out.println("Email successfully moved to Trash.");
                    return true;
                } else {
                    System.out.println("Email not found in Trash after moving.");
                    return false;
                }
            } else {
                System.out.println("Could not find email to delete.");
                return false;
            }

        } catch (Exception e) {
            System.out.println("Error deleting email: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (sourceFolder != null && sourceFolder.isOpen()) sourceFolder.close(false);
                if (trashFolder != null && trashFolder.isOpen()) trashFolder.close(false);
                if (store != null && store.isConnected()) store.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean restoreEmail(EmailData email) {
        Properties config = loadConfig();
        if (config == null) return false;

        final String username = config.getProperty("email");
        final String password = config.getProperty("password");

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", config.getProperty("imap.host"));
        props.put("mail.imaps.port", config.getProperty("imap.port"));
        props.put("mail.imaps.ssl.enable", "true");

        Store store = null;
        Folder trashFolder = null;
        Folder inboxFolder = null;

        try {
            Session session = Session.getInstance(props, null);
            store = session.getStore("imaps");
            store.connect(username, password);

            String trashFolderName = getFolderName("Trash");
            if (trashFolderName == null) {
                System.out.println("Trash folder not found");
                return false;
            }
            trashFolder = store.getFolder(trashFolderName);
            trashFolder.open(Folder.READ_WRITE);

            inboxFolder = store.getFolder("INBOX");
            inboxFolder.open(Folder.READ_WRITE);

            Message[] messages = trashFolder.getMessages();
            Message targetMessage = null;
            for (Message msg : messages) {
                String msgFrom = msg.getFrom()[0].toString();
                String msgSubject = msg.getSubject() != null ? msg.getSubject() : "";
                String emailSubject = email.getSubject() != null ? email.getSubject() : "";
                if (msgFrom.equals(email.getFrom()) && msgSubject.equals(emailSubject)) {
                    targetMessage = msg;
                    break;
                }
            }

            if (targetMessage != null) {
                trashFolder.copyMessages(new Message[]{targetMessage}, inboxFolder);
                return true;
            }

            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (trashFolder != null && trashFolder.isOpen()) trashFolder.close(false);
                if (inboxFolder != null && inboxFolder.isOpen()) inboxFolder.close(false);
                if (store != null && store.isConnected()) store.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
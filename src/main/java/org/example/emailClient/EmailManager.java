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

        try {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect(username, password);

            Folder sourceFolder = store.getFolder(currentFolder);
            sourceFolder.open(Folder.READ_WRITE);

            String trashFolderName = getFolderName("Trash");
            if (trashFolderName == null) {
                System.out.println("Trash folder not found");
                return false;
            }
            Folder trashFolder = store.getFolder(trashFolderName);
            trashFolder.open(Folder.READ_WRITE);

            Message[] messages = sourceFolder.getMessages();
            Message targetMessage = null;
            for (Message msg : messages) {
                if (msg.getFrom()[0].toString().equals(email.getFrom()) &&
                        msg.getSubject().equals(email.getSubject()) &&
                        (msg.getSentDate() != null && msg.getSentDate().toString().equals(email.getDate()))) {
                    targetMessage = msg;
                    break;
                }
            }

            if (targetMessage != null) {
                sourceFolder.copyMessages(new Message[]{targetMessage}, trashFolder);
                targetMessage.setFlag(Flags.Flag.DELETED, true);
                sourceFolder.expunge();
                sourceFolder.close(false);
                trashFolder.close(false);
                store.close();
                return true;
            }

            sourceFolder.close(false);
            trashFolder.close(false);
            store.close();
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
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

        try {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect(username, password);

            String trashFolderName = getFolderName("Trash");
            if (trashFolderName == null) {
                System.out.println("Trash folder not found");
                return false;
            }
            Folder trashFolder = store.getFolder(trashFolderName);
            trashFolder.open(Folder.READ_WRITE);

            Folder inboxFolder = store.getFolder("INBOX");
            inboxFolder.open(Folder.READ_WRITE);

            Message[] messages = trashFolder.getMessages();
            Message targetMessage = null;
            for (Message msg : messages) {
                if (msg.getFrom()[0].toString().equals(email.getFrom()) &&
                        msg.getSubject().equals(email.getSubject()) &&
                        (msg.getSentDate() != null && msg.getSentDate().toString().equals(email.getDate()))) {
                    targetMessage = msg;
                    break;
                }
            }

            if (targetMessage != null) {
                trashFolder.copyMessages(new Message[]{targetMessage}, inboxFolder);
                targetMessage.setFlag(Flags.Flag.DELETED, true);
                trashFolder.expunge();
                trashFolder.close(false);
                inboxFolder.close(false);
                store.close();
                return true;
            }

            trashFolder.close(false);
            inboxFolder.close(false);
            store.close();
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    // Add this method to EmailManager.java
    public static boolean moveToSpamFolder(EmailData email, String currentFolder) {
        Properties config = loadConfig();
        if (config == null) return false;

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

            Folder sourceFolder = store.getFolder(currentFolder);
            sourceFolder.open(Folder.READ_WRITE);

            // Access spam folder
            Folder spamFolder = store.getFolder("Spam");
            spamFolder.open(Folder.READ_WRITE);

            Message[] messages = sourceFolder.getMessages();
            Message targetMessage = null;
            for (Message msg : messages) {
                if (msg.getFrom()[0].toString().equals(email.getFrom()) &&
                        msg.getSubject().equals(email.getSubject()) &&
                        (msg.getSentDate() != null && msg.getSentDate().toString().equals(email.getDate()))) {
                    targetMessage = msg;
                    break;
                }
            }

            if (targetMessage != null) {
                sourceFolder.copyMessages(new Message[]{targetMessage}, spamFolder);
                targetMessage.setFlag(Flags.Flag.DELETED, true);
                sourceFolder.expunge();
                sourceFolder.close(false);
                spamFolder.close(false);
                store.close();
                return true;
            }

            sourceFolder.close(false);
            spamFolder.close(false);
            store.close();
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Add this method to EmailManager.java
    public static boolean moveFromSpamToInbox(EmailData email) {
        Properties config = loadConfig();
        if (config == null) return false;

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

            Folder spamFolder = store.getFolder("Spam");
            spamFolder.open(Folder.READ_WRITE);

            Folder inboxFolder = store.getFolder("INBOX");
            inboxFolder.open(Folder.READ_WRITE);

            Message[] messages = spamFolder.getMessages();
            Message targetMessage = null;
            for (Message msg : messages) {
                if (msg.getFrom()[0].toString().equals(email.getFrom()) &&
                        msg.getSubject().equals(email.getSubject()) &&
                        (msg.getSentDate() != null && msg.getSentDate().toString().equals(email.getDate()))) {
                    targetMessage = msg;
                    break;
                }
            }

            if (targetMessage != null) {
                spamFolder.copyMessages(new Message[]{targetMessage}, inboxFolder);
                targetMessage.setFlag(Flags.Flag.DELETED, true);
                spamFolder.expunge();
                spamFolder.close(false);
                inboxFolder.close(false);
                store.close();
                return true;
            }

            spamFolder.close(false);
            inboxFolder.close(false);
            store.close();
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
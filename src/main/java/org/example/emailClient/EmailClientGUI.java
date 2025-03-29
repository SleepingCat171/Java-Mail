package org.example.emailClient;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EmailClientGUI extends JFrame {
    private JTable emailTable;
    private JButton composeButton, inboxButton, sentButton, trashButton, refreshButton, deleteButton, restoreButton;
    private List<EmailData> emailMessages;
    private DefaultTableModel tableModel;
    private String currentFolder = "INBOX";
    private String sentFolderName;
    private String trashFolderName;

    public EmailClientGUI() {
        setTitle("GenGuMail");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        sentFolderName = EmailManager.getFolderName("Sent");
        trashFolderName = EmailManager.getFolderName("Trash");
        if (sentFolderName == null || trashFolderName == null) {
            JOptionPane.showMessageDialog(this, "Cannot find Sent or Trash folder. Please check your Gmail settings.");
            System.exit(1);
        }

        Border roundedBorder = BorderFactory.createLineBorder(new Color(200, 200, 200), 2, true);

        // top bar
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(255, 255, 255));
        topPanel.setBorder(BorderFactory.createCompoundBorder(roundedBorder, BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JPanel logoAndNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoAndNamePanel.setBackground(new Color(255, 255, 255));

        // logo
        JLabel logoLabel = new JLabel();
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getClassLoader().getResource("logo.png"));
            Image logoImage = logoIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(logoImage));
        } catch (Exception e) {
            logoLabel.setText("Logo not found");
            e.printStackTrace();
        }
        logoAndNamePanel.add(logoLabel);

        // App name
        JLabel appNameLabel = new JLabel("GenGuMail");
        appNameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        appNameLabel.setForeground(new Color(0, 0, 0));
        logoAndNamePanel.add(appNameLabel);

        topPanel.add(logoAndNamePanel, BorderLayout.WEST);

        // left bar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(200, 230, 201)); 
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(roundedBorder);

        Font buttonFont = new Font("Arial", Font.PLAIN, 16);

        composeButton = new JButton("Soạn thư");
        composeButton.setBackground(new Color(129, 212, 250)); 
        composeButton.setForeground(Color.WHITE);
        composeButton.setFont(buttonFont);
        composeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); 
        composeButton.setBorder(BorderFactory.createCompoundBorder(roundedBorder, BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        sidebar.add(composeButton);
        sidebar.add(Box.createVerticalStrut(10));

        inboxButton = new JButton("Hộp thư đến");
        inboxButton.setBackground(new Color(220, 237, 200)); 
        inboxButton.setFont(buttonFont);
        inboxButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        inboxButton.setBorder(BorderFactory.createCompoundBorder(roundedBorder, BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        sidebar.add(inboxButton);

        sentButton = new JButton("Đã gửi");
        sentButton.setBackground(new Color(220, 237, 200));
        sentButton.setFont(buttonFont);
        sentButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        sentButton.setBorder(BorderFactory.createCompoundBorder(roundedBorder, BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        sidebar.add(sentButton);

        trashButton = new JButton("Thùng rác");
        trashButton.setBackground(new Color(220, 237, 200));
        trashButton.setFont(buttonFont);
        trashButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        trashButton.setBorder(BorderFactory.createCompoundBorder(roundedBorder, BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        sidebar.add(trashButton);

        String[] columnNames = {"From", "Subject", "Date"};
        tableModel = new DefaultTableModel(columnNames, 0);
        emailTable = new JTable(tableModel);
        emailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailTable.setBackground(new Color(245, 245, 220)); 
        emailTable.setRowHeight(30);
        emailTable.setBorder(roundedBorder);
        emailMessages = new ArrayList<>();

        JScrollPane tableScrollPane = new JScrollPane(emailTable);
        tableScrollPane.setBackground(new Color(245, 245, 220));
        tableScrollPane.setBorder(roundedBorder);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(new Color(245, 245, 220));
        toolbar.setBorder(BorderFactory.createCompoundBorder(roundedBorder, BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        restoreButton = new JButton("Restore");
        restoreButton.setBackground(new Color(165, 214, 167)); 
        restoreButton.setForeground(Color.WHITE);
        restoreButton.setFont(buttonFont);
        restoreButton.setPreferredSize(new Dimension(120, 40)); 
        restoreButton.setBorder(roundedBorder);
        restoreButton.setVisible(false);
        toolbar.add(restoreButton);

        // bottom bar
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(new Color(255, 255, 255));
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(roundedBorder, BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        deleteButton = new JButton("Delete");
        deleteButton.setBackground(new Color(255, 152, 152)); 
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(buttonFont);
        deleteButton.setPreferredSize(new Dimension(120, 40)); 
        deleteButton.setBorder(roundedBorder);
        bottomPanel.add(deleteButton);

        refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(129, 212, 250));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(buttonFont);
        refreshButton.setPreferredSize(new Dimension(120, 40)); 
        refreshButton.setBorder(roundedBorder);
        bottomPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(toolbar, BorderLayout.CENTER);
        add(tableScrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        composeButton.addActionListener(e -> showComposeWindow());

        inboxButton.addActionListener(e -> {
            currentFolder = "INBOX";
            restoreButton.setVisible(false);
            refreshInbox();
        });

        sentButton.addActionListener(e -> {
            currentFolder = sentFolderName;
            restoreButton.setVisible(false);
            refreshInbox();
        });

        trashButton.addActionListener(e -> {
            currentFolder = trashFolderName;
            restoreButton.setVisible(true);
            refreshInbox();
        });

        refreshButton.addActionListener(e -> {
            System.out.println("Refresh button clicked");
            refreshInbox();
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = emailTable.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < emailMessages.size()) {
                new Thread(() -> {
                    boolean success = EmailManager.deleteEmail(emailMessages.get(selectedRow), currentFolder);
                    SwingUtilities.invokeLater(() -> {
                        if (success) {
                            JOptionPane.showMessageDialog(this, "Email moved to Trash!");
                            // 3 sec to sync
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            refreshInbox();
                        } else {
                            JOptionPane.showMessageDialog(this, "Error deleting email.");
                        }
                    });
                }).start();
            } else {
                JOptionPane.showMessageDialog(this, "Please select an email to delete.");
            }
        });

        restoreButton.addActionListener(e -> {
            int selectedRow = emailTable.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < emailMessages.size()) {
                new Thread(() -> {
                    boolean success = EmailManager.restoreEmail(emailMessages.get(selectedRow));
                    SwingUtilities.invokeLater(() -> {
                        if (success) {
                            JOptionPane.showMessageDialog(this, "Email restored to Inbox!");
                            restoreButton.setVisible(false);
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            refreshInbox();
                        } else {
                            JOptionPane.showMessageDialog(this, "Error restoring email.");
                        }
                    });
                }).start();
            }
        });


        emailTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = emailTable.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < emailMessages.size()) {
                    showEmailDetails(emailMessages.get(selectedRow));
                }
            }
        });

        setVisible(true);
        refreshInbox();
    }

    private void refreshInbox() {
        System.out.println("Starting inbox refresh...");
        new Thread(() -> {
            try {
                List<String[]> emailSummaries = new ArrayList<>();
                emailMessages.clear(); 
                emailMessages = EmailReceiver.fetchEmails(emailSummaries, currentFolder);
                System.out.println("Fetched " + emailSummaries.size() + " emails");
                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0); 
                    for (String[] summary : emailSummaries) {
                        if (summary[0].startsWith("Error")) {
                            continue;
                        }
                        tableModel.addRow(summary);
                    }
                    if (tableModel.getRowCount() == 0) {
                        tableModel.addRow(new String[]{"No emails", "", ""});
                    }
                    System.out.println("Table updated with " + tableModel.getRowCount() + " rows");
                    emailTable.revalidate();
                    emailTable.repaint(); 
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Error refreshing inbox: " + ex.getMessage());
                });
            }
        }).start();
    }

    private void showComposeWindow() {
        JFrame composeFrame = new JFrame("Soạn thư");
        composeFrame.setSize(500, 400);
        composeFrame.setLayout(new BorderLayout());

        JPanel composePanel = new JPanel();
        composePanel.setLayout(new BoxLayout(composePanel, BoxLayout.Y_AXIS));
        composePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2, true),
                BorderFactory.createEmptyBorder(10, 20, 10, 10) 
        ));
        composePanel.setBackground(new Color(245, 245, 220));

        JPanel toPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toPanel.setBackground(new Color(245, 245, 220));
        toPanel.add(new JLabel("To:"));
        JTextField toField = new JTextField(30);
        toPanel.add(toField);
        composePanel.add(toPanel);

        composePanel.add(Box.createVerticalStrut(5));

        JPanel subjectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        subjectPanel.setBackground(new Color(245, 245, 220));
        subjectPanel.add(new JLabel("Subject:"));
        JTextField subjectField = new JTextField(30);
        subjectPanel.add(subjectField);
        composePanel.add(subjectPanel);

        composePanel.add(Box.createVerticalStrut(5));

        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        messagePanel.setBackground(new Color(245, 245, 220));
        messagePanel.add(new JLabel("Message:"));
        composePanel.add(messagePanel);

        JTextArea messageArea = new JTextArea(10, 30);
        messageArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2, true));
        composePanel.add(new JScrollPane(messageArea));

        JPanel sendButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sendButtonPanel.setBackground(new Color(245, 245, 220));
        JButton sendButton = new JButton("Send");
        sendButton.setBackground(new Color(129, 212, 250));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("Arial", Font.PLAIN, 16));
        sendButton.setPreferredSize(new Dimension(120, 40)); 
        sendButton.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2, true));
        sendButtonPanel.add(sendButton);
        composePanel.add(Box.createVerticalStrut(5));
        composePanel.add(sendButtonPanel);

        composeFrame.add(composePanel, BorderLayout.CENTER);

        sendButton.addActionListener(e -> {
            String to = toField.getText();
            String subject = subjectField.getText();
            String message = messageArea.getText();
            EmailSender.sendEmail(to, subject, message);
            JOptionPane.showMessageDialog(composeFrame, "Email sent!");
            composeFrame.dispose();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            refreshInbox();
        });

        composeFrame.setVisible(true);
    }

    private void showEmailDetails(EmailData email) {
        JFrame detailFrame = new JFrame("Email Details");
        detailFrame.setSize(500, 400);
        detailFrame.setLayout(new BorderLayout());

        JTextArea detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setText("From: " + email.getFrom() + "\n" +
                "Subject: " + email.getSubject() + "\n" +
                "Date: " + email.getDate() + "\n" +
                "Content: " + email.getContent());
        detailArea.setBackground(new Color(245, 245, 220));
        detailArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2, true));

        detailFrame.add(new JScrollPane(detailArea), BorderLayout.CENTER);
        detailFrame.setVisible(true);
    }
}
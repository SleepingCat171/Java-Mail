package org.example.emailClient;

import javax.swing.*;
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
        setTitle("Email Client");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        sentFolderName = EmailManager.getFolderName("Sent");
        trashFolderName = EmailManager.getFolderName("Trash");
        if (sentFolderName == null || trashFolderName == null) {
            JOptionPane.showMessageDialog(this, "Cannot find Sent or Trash folder. Please check your Gmail settings.");
            System.exit(1);
        }

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(240, 240, 240));
        sidebar.setPreferredSize(new Dimension(200, 0));

        composeButton = new JButton("Soạn thư");
        composeButton.setBackground(new Color(26, 115, 232));
        composeButton.setForeground(Color.WHITE);
        composeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, composeButton.getMinimumSize().height));
        sidebar.add(composeButton);
        sidebar.add(Box.createVerticalStrut(10));

        inboxButton = new JButton("Hộp thư đến");
        inboxButton.setBackground(new Color(200, 200, 200));
        inboxButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, inboxButton.getMinimumSize().height));
        sidebar.add(inboxButton);

        sentButton = new JButton("Đã gửi");
        sentButton.setBackground(new Color(200, 200, 200));
        sentButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, sentButton.getMinimumSize().height));
        sidebar.add(sentButton);

        trashButton = new JButton("Thùng rác");
        trashButton.setBackground(new Color(200, 200, 200));
        trashButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, trashButton.getMinimumSize().height));
        sidebar.add(trashButton);

        String[] columnNames = {"From", "Subject", "Date"};
        tableModel = new DefaultTableModel(columnNames, 0);
        emailTable = new JTable(tableModel);
        emailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailTable.setBackground(new Color(245, 245, 245));
        emailTable.setRowHeight(30);
        emailMessages = new ArrayList<>();

        JScrollPane tableScrollPane = new JScrollPane(emailTable);
        tableScrollPane.setBackground(Color.WHITE);

        // Thanh công cụ phía trên bảng email
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(Color.WHITE);
        refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(26, 115, 232));
        refreshButton.setForeground(Color.WHITE);
        toolbar.add(refreshButton);

        deleteButton = new JButton("Delete");
        deleteButton.setBackground(new Color(200, 0, 0));
        deleteButton.setForeground(Color.WHITE);
        toolbar.add(deleteButton);

        restoreButton = new JButton("Restore");
        restoreButton.setBackground(new Color(0, 150, 0));
        restoreButton.setForeground(Color.WHITE);
        restoreButton.setVisible(false);
        toolbar.add(restoreButton);

        add(sidebar, BorderLayout.WEST);
        add(toolbar, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);

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
                    if (success) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "Email moved to Trash!");
                            refreshInbox();
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Error deleting email."));
                    }
                }).start();
            }
        });

        restoreButton.addActionListener(e -> {
            int selectedRow = emailTable.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < emailMessages.size()) {
                new Thread(() -> {
                    boolean success = EmailManager.restoreEmail(emailMessages.get(selectedRow));
                    if (success) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "Email restored to Inbox!");
                            refreshInbox();
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Error restoring email."));
                    }
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
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error refreshing inbox: " + ex.getMessage());
            }
        }).start();
    }

    private void showComposeWindow() {
        JFrame composeFrame = new JFrame("Soạn thư");
        composeFrame.setSize(500, 400);
        composeFrame.setLayout(new BorderLayout());

        JPanel composePanel = new JPanel();
        composePanel.setLayout(new BoxLayout(composePanel, BoxLayout.Y_AXIS));
        composePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        composePanel.add(new JLabel("To:"));
        JTextField toField = new JTextField(30);
        composePanel.add(toField);

        composePanel.add(Box.createVerticalStrut(5));
        composePanel.add(new JLabel("Subject:"));
        JTextField subjectField = new JTextField(30);
        composePanel.add(subjectField);

        composePanel.add(Box.createVerticalStrut(5));
        composePanel.add(new JLabel("Message:"));
        JTextArea messageArea = new JTextArea(10, 30);
        composePanel.add(new JScrollPane(messageArea));

        JButton sendButton = new JButton("Send");
        sendButton.setBackground(new Color(26, 115, 232));
        sendButton.setForeground(Color.WHITE);
        composePanel.add(Box.createVerticalStrut(5));
        composePanel.add(sendButton);

        composeFrame.add(composePanel, BorderLayout.CENTER);

        sendButton.addActionListener(e -> {
            String to = toField.getText();
            String subject = subjectField.getText();
            String message = messageArea.getText();
            EmailSender.sendEmail(to, subject, message);
            JOptionPane.showMessageDialog(composeFrame, "Email sent!");
            composeFrame.dispose();
            // 2sec to sync
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

        detailFrame.add(new JScrollPane(detailArea), BorderLayout.CENTER);
        detailFrame.setVisible(true);
    }
}
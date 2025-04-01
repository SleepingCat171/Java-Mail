package org.example.emailClient;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EmailClientGUI extends JFrame {
    private JTable emailTable;
    private JButton composeButton, inboxButton, sentButton, trashButton, spamButton, refreshButton, deleteButton, restoreButton, notSpamButton;
    private List<EmailData> emailMessages;
    private DefaultTableModel tableModel;
    private String currentFolder = "INBOX";
    private String sentFolderName;
    private String trashFolderName;
    private String spamFolderName = "Spam"; // Assuming Gmail's standard spam folder name

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

        // Add new Spam folder button
        spamButton = new JButton("Thư rác");
        spamButton.setBackground(new Color(255, 200, 200)); // Light red for spam
        spamButton.setFont(buttonFont);
        spamButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        spamButton.setBorder(BorderFactory.createCompoundBorder(roundedBorder, BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        sidebar.add(spamButton);

        String[] columnNames = {"From", "Subject", "Date"};
        tableModel = new DefaultTableModel(columnNames, 0);
        emailTable = new JTable(tableModel);
        emailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailTable.setBackground(new Color(245, 245, 220));
        emailTable.setRowHeight(30);
        emailTable.setBorder(roundedBorder);

        // Custom renderer to highlight spam emails
        emailTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Check if this is a spam email (marked with [SPAM] prefix in the "From" column)
                if (column == 0 && value != null && value.toString().startsWith("[SPAM]")) {
                    c.setForeground(Color.RED);
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(Color.BLACK);
                    setFont(getFont().deriveFont(Font.PLAIN));
                }

                return c;
            }
        });

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

        // Add "Not Spam" button for the spam folder
        notSpamButton = new JButton("Not Spam");
        notSpamButton.setBackground(new Color(165, 214, 167));
        notSpamButton.setForeground(Color.WHITE);
        notSpamButton.setFont(buttonFont);
        notSpamButton.setPreferredSize(new Dimension(120, 40));
        notSpamButton.setBorder(roundedBorder);
        notSpamButton.setVisible(false);
        toolbar.add(notSpamButton);

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

        // Add Mark as Spam button to the bottom panel
        JButton markAsSpamButton = new JButton("Mark as Spam");
        markAsSpamButton.setBackground(new Color(255, 132, 132)); // Darker red for spam action
        markAsSpamButton.setForeground(Color.WHITE);
        markAsSpamButton.setFont(buttonFont);
        markAsSpamButton.setPreferredSize(new Dimension(150, 40));
        markAsSpamButton.setBorder(roundedBorder);
        bottomPanel.add(markAsSpamButton);

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
            notSpamButton.setVisible(false);
            refreshInbox();
        });

        sentButton.addActionListener(e -> {
            currentFolder = sentFolderName;
            restoreButton.setVisible(false);
            notSpamButton.setVisible(false);
            refreshInbox();
        });

        trashButton.addActionListener(e -> {
            currentFolder = trashFolderName;
            restoreButton.setVisible(true);
            notSpamButton.setVisible(false);
            refreshInbox();
        });

        // Add action listener for spam folder button
        spamButton.addActionListener(e -> {
            currentFolder = spamFolderName;
            restoreButton.setVisible(false);
            notSpamButton.setVisible(true);
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

        // Add action listener for Mark as Spam button
        markAsSpamButton.addActionListener(e -> {
            int selectedRow = emailTable.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < emailMessages.size()) {
                EmailData email = emailMessages.get(selectedRow);

                // Start a new thread to handle server communication
                new Thread(() -> {
                    boolean success = EmailManager.moveToSpamFolder(email, currentFolder);
                    SwingUtilities.invokeLater(() -> {
                        if (success) {
                            JOptionPane.showMessageDialog(this, "Email moved to Spam folder!");
                            // 3 sec to sync
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            refreshInbox();
                        } else {
                            JOptionPane.showMessageDialog(this, "Error moving email to Spam folder.");
                        }
                    });
                }).start();
            } else {
                JOptionPane.showMessageDialog(this, "Please select an email to mark as spam.");
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

        // Add action listener for Not Spam button
        notSpamButton.addActionListener(e -> {
            int selectedRow = emailTable.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < emailMessages.size()) {
                EmailData email = emailMessages.get(selectedRow);

                // Start a new thread to handle server communication
                new Thread(() -> {
                    boolean success = EmailManager.moveFromSpamToInbox(email);
                    SwingUtilities.invokeLater(() -> {
                        if (success) {
                            JOptionPane.showMessageDialog(this, "Email moved to Inbox!");
                            // 3 sec to sync
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            refreshInbox();
                        } else {
                            JOptionPane.showMessageDialog(this, "Error moving email to Inbox.");
                        }
                    });
                }).start();
            } else {
                JOptionPane.showMessageDialog(this, "Please select an email to mark as not spam.");
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

    private void showEmailDetails(EmailData email) {
        JFrame detailFrame = new JFrame("Email Details");
        detailFrame.setSize(500, 400);
        detailFrame.setLayout(new BorderLayout());

        JTextPane detailPane = new JTextPane();
        detailPane.setEditable(false);
        detailPane.setContentType("text/html");
        detailPane.setText("<html><body style='background-color:#F5F5DC;'>" +
                "<p style='margin-bottom: 10px;'><b style='font-size:14px;'>From:</b> <span style='font-size:13px;'>" + escapeHtml(email.getFrom()) + "</span></p>" +
                "<p style='margin-bottom: 10px;'><b style='font-size:14px;'>Subject:</b> <span style='font-size:13px;'>" + escapeHtml(email.getSubject()) + "</span></p>" +
                "<p style='margin-bottom: 10px;'><b style='font-size:14px;'>Date:</b> <span style='font-size:13px;'>" + escapeHtml(email.getDate()) + "</span></p>" +
                "<p style='margin-bottom: 10px;'><b style='font-size:14px;'>Content:</b> <span style='font-size:13px;'>" + escapeHtml(email.getContent()) + "</span></p>" +
                "</body></html>");

        detailPane.setBackground(new Color(245, 245, 220));
        detailPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2, true));

        detailFrame.add(new JScrollPane(detailPane), BorderLayout.CENTER);
        detailFrame.setVisible(true);
    }

    // special HTML
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("\n", "<br>");
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

        sendButton.addActionListener(e -> {
            String recipient = toField.getText().trim();
            String subject = subjectField.getText().trim();
            String message = messageArea.getText().trim();

            if (recipient.isEmpty() || message.isEmpty()) {
                JOptionPane.showMessageDialog(composeFrame, "Recipient and message cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = EmailSender.sendEmail(recipient, subject, message);
            if (success) {
                JOptionPane.showMessageDialog(composeFrame, "Email sent successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                composeFrame.dispose();
            } else {
                JOptionPane.showMessageDialog(composeFrame, "Failed to send email.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        composePanel.add(sendButtonPanel);
        composeFrame.add(composePanel, BorderLayout.CENTER);
        composeFrame.setVisible(true);
    }
}
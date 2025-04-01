package org.example.emailClient;

public class EmailData {
    private String from;
    private String subject;
    private String date;
    private String content;
    private boolean isSpam;

    public EmailData(String from, String subject, String date, String content) {
        this.from = from;
        this.subject = subject;
        this.date = date;
        this.content = content;
        this.isSpam = false; // Default is not spam
    }

    public String getFrom() {
        return from;
    }

    public String getSubject() {
        return subject;
    }

    public String getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }

    public boolean isSpam() {
        return isSpam;
    }

    public void setSpam(boolean isSpam) {
        this.isSpam = isSpam;
    }

    public String[] toSummaryArray() {
        return new String[]{from, subject, date};
    }
}
package org.example.emailClient;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpamDetector {
    private static final Set<String> SPAM_KEYWORDS = new HashSet<>(Arrays.asList(
            "nigga", "blablablablublublu", "enlargement", "lottery", "prize", "chu quang anh", "million dollar",
            "free money", "nigerian prince", "investment opportunity", "get rich quick",
            "bank account", "con dog", "bitcoin investment","cakcakcakcak",
            "money transfer", "pharmacy", "ketamin", "replica", "heroin",
            "earn money fast", "what are you talking about", "work from home", "no experience needed",
            "make money online", "limited time offer", "CaoTranBaoThuong","exclusive deal"
    ));

    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z0-9][a-z0-9-]{0,61}[a-z0-9](?:/[^\\s]*)?",
            Pattern.CASE_INSENSITIVE);

    // Check if the email is likely spam based on content analysis
    public static boolean isSpam(EmailData email) {
        // Check subject for spam indicators
        if (hasSpamSubject(email.getSubject())) {
            return true;
        }

        // Check content for spam indicators
        String content = email.getContent().toLowerCase();

        // Check for spam keywords
        for (String keyword : SPAM_KEYWORDS) {
            if (content.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        // Check for excessive URLs
        int urlCount = countUrls(content);
        if (urlCount > 5) {
            return true;
        }

        // Check for excessive use of exclamation marks
        if (countSymbol(content, '!') > 10) {
            return true;
        }

        // Check for all caps text blocks (shouting)
        if (hasAllCapsText(content)) {
            return true;
        }

        return false;
    }

    private static boolean hasSpamSubject(String subject) {
        if (subject == null) return false;

        String lowerSubject = subject.toLowerCase();

        // Check for spam-like subject patterns
        if (lowerSubject.contains("urgent") &&
                (lowerSubject.contains("action") || lowerSubject.contains("reply"))) {
            return true;
        }

        // Check for excessive punctuation in subject
        if (countSymbol(lowerSubject, '!') > 3 || countSymbol(lowerSubject, '$') > 2) {
            return true;
        }

        // Check for typical spam subject keywords
        for (String keyword : SPAM_KEYWORDS) {
            if (lowerSubject.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    private static int countUrls(String text) {
        if (text == null) return 0;

        Matcher matcher = URL_PATTERN.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static int countSymbol(String text, char symbol) {
        if (text == null) return 0;

        int count = 0;
        for (char c : text.toCharArray()) {
            if (c == symbol) {
                count++;
            }
        }
        return count;
    }

    private static boolean hasAllCapsText(String text) {
        if (text == null) return false;

        // Look for blocks of text (5+ chars) that are all uppercase
        Pattern allCapsPattern = Pattern.compile("\\b[A-Z]{5,}\\b");
        Matcher matcher = allCapsPattern.matcher(text);
        return matcher.find();
    }
}
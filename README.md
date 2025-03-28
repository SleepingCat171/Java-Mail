# Java Mail ☕

## Requirements
- IDE : This project using **IntelliJ IDEA Community Edition 2024.3.4.1**
- Make sure you have Java Development Kit (JDK) version 8 or higher installed

## Technologies used
- JavaMail API
- Java Swing
- Maven

## Mail configs

**config.properties**</br>

This file contains important information that the program needs to send and receive email.

```properties
email=...@gmail.com
password=
smtp.host=smtp.gmail.com
smtp.port=587
imap.host=imap.gmail.com
imap.port=993
```

- In the "email" section, we will use a real email address.
- In the "password" section, we will not use the Gmail master password because it does not work with JavaMail when 2-step verification is enabled. Instead, we will use App Password to generate a password from : https://myaccount.google.com/apppasswords


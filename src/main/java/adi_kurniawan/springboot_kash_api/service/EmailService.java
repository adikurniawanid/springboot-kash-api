package adi_kurniawan.springboot_kash_api.service;

import adi_kurniawan.springboot_kash_api.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendHtmlEmail(User user, String subject, String pathEmailTemplate, String link) throws MessagingException, IOException {
        MimeMessage message = javaMailSender.createMimeMessage();

        message.setFrom(new InternetAddress("noreply@kash.com"));
        message.setRecipients(MimeMessage.RecipientType.TO, user.getEmail());
        message.setSubject(subject);

        String htmlTemplate = new String(Files.readAllBytes(Paths.get(pathEmailTemplate)));


        htmlTemplate = htmlTemplate.replace("${name}", Objects.isNull(user.getUserDetail()) ? user.getUsername() : user.getUserDetail().getName());
        htmlTemplate = htmlTemplate.replace("${email}", user.getEmail());

        if (!Objects.isNull(link)) {
            htmlTemplate = htmlTemplate.replace("${link}", link);
        }
        message.setContent(htmlTemplate, "text/html; charset=utf-8");

        javaMailSender.send(message);
    }
}

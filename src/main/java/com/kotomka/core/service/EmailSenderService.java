package com.kotomka.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailSenderService {

    @Value("${mail.from}")
    private String fromAddress;

    private final JavaMailSenderImpl mailSender;

    @Autowired
    public EmailSenderService(JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmailMsg(String[] toEmailAddresses, String subject, String message) {
        final SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromAddress);
        mailMessage.setTo(toEmailAddresses);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        mailSender.send(mailMessage);
    }


    public void sendEmailHtmlMsg(String[] toEmailAddresses, String subject, String htmlMsg) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mail = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        mail.setFrom(fromAddress);
        mail.setTo(toEmailAddresses);
        mail.setSubject(subject);
        mail.setText(htmlMsg, true);
        mailSender.send(mimeMessage);
    }
}
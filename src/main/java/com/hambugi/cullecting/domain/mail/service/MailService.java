package com.hambugi.cullecting.domain.mail.service;

import com.hambugi.cullecting.domain.mail.repository.MailRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Service
public class MailService {
    private final JavaMailSender mailSender;
    private final MailRepository mailRepository;

    public MailService(JavaMailSender mailSender, MailRepository mailRepository) {
        this.mailSender = mailSender;
        this.mailRepository = mailRepository;
    }

    public void sendVerificationCode(String email) {
        String code = generateCode();
        mailRepository.setCode(email, code);
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("이메일 인증");
            String mailContext = getMailTemplate().replace("${code}", code);
            helper.setText(mailContext, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mailSender.send(message);
    }

    public boolean verifyCode(String email, String inputCode) {
        return mailRepository.verifyCode(email, inputCode);
    }

    private String generateCode() {
        return String.valueOf(new Random().nextInt(900000) + 100000); // 6자리 숫자
    }

    private String getMailTemplate() {
        StringBuilder contentBuilder = new StringBuilder();
        try (InputStream inputStream = new ClassPathResource("templates/MailTemplate.html").getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            reader.lines().forEach(contentBuilder::append);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }
}

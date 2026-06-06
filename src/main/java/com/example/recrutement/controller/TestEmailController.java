package com.example.recrutement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestEmailController {

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/email-test")
    public String testEmail() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("yasmineben779@gmail.com");
            message.setTo("yasmineben779@gmail.com");
            message.setSubject("Test Spring Boot Email");
            message.setText("Test réussi ! Votre configuration email fonctionne.");
            
            mailSender.send(message);
            return "✅ Email envoyé avec succès !";
        } catch (Exception e) {
            return "❌ Erreur : " + e.getMessage();
        }
    }
}
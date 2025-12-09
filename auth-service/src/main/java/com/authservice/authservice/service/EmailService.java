package com.authservice.authservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationToken(String toEmail, String username, String token){
        try{
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true,"UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Activate your BookStore account");
            helper.setFrom(fromEmail);

            String activationLink = baseUrl + "/api/v1/auth/verify?token=" + token;
            String htmlContent = buildActivationEmailHtml(username, activationLink);

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Activation email sent to {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send activation email to {}", toEmail, e);
            throw new RuntimeException("Failed to send activation email to ", e);
        }
    }
    private String buildActivationEmailHtml(String username, String activationLink) {
        String htmlContent = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #ffffff;">
                <table role="presentation" style="width: 100%; max-width: 600px; margin: 0 auto; padding: 40px 20px;">
                    <tr>
                        <td>
                            <!-- Header -->
                            <h1 style="margin: 0 0 30px 0; color: #333333; font-size: 24px; font-weight: normal;">
                                Welcome to BookStore
                            </h1>
                            <!-- Greeting -->
                            <p style="margin: 0 0 20px 0; color: #333333; font-size: 16px; line-height: 1.6;">
                                Hi %USERNAME%,
                            </p>
                            <!-- Main Message -->
                            <p style="margin: 0 0 20px 0; color: #333333; font-size: 16px; line-height: 1.6;">
                                Thank you for choosing to open an <span style="background-color: #FFFF00;">account</span> with us. 
                                To keep everything secure, we need to verify your email address.
                            </p>
                            
                            <p style="margin: 0 0 30px 0; color: #333333; font-size: 16px; line-height: 1.6;">
                                Please click the button below to activate your account. This link will expire in <strong>1 hour</strong>.
                            </p>
                            
                            <!-- Button -->
                            <table role="presentation" style="margin: 0 0 30px 0;">
                                <tr>
                                    <td style="border-radius: 4px; background-color: #007bff;">
                                        <a href="%ACTIVATION_LINK_BUTTON%"
                                           style="display: inline-block; padding: 14px 32px; color: #ffffff; text-decoration: none; font-size: 16px; font-weight: normal; border-radius: 4px;">
                                            Verify Email Address
                                        </a>
                                    </td>
                                </tr>
                            </table>
                            
                            <!-- Alternative Link -->
                            <p style="margin: 0 0 5px 0; color: #666666; font-size: 14px;">
                                Or copy and paste this link into your browser:
                            </p>
                            <p style="margin: 0 0 30px 0; color: #007bff; font-size: 14px; word-break: break-all;">
                                <a href="%ACTIVATION_LINK_TEXT%" style="color: #007bff; text-decoration: none;">%ACTIVATION_LINK_COPY%</a>
                            </p>
                            
                            <!-- Closing -->
                            <p style="margin: 0 0 30px 0; color: #333333; font-size: 16px; line-height: 1.6;">
                                Kind regards,<br>
                                Your <span style="background-color: #FFFF00;">BookStore</span> Team
                            </p>
                            
                            <!-- Security Notice -->
                            <div style="margin-top: 40px; padding: 20px; background-color: #f5f5f5; border-radius: 4px;">
                                <p style="margin: 0 0 10px 0; color: #333333; font-size: 14px; font-weight: bold;">
                                    Security
                                </p>
                                <p style="margin: 0; color: #666666; font-size: 13px; line-height: 1.6;">
                                    Unfortunately, some people send emails pretending to be from BookStore. 
                                    Please remember that we will never ask you to provide personal information, 
                                    payment details, or passwords through email or links in an email. 
                                    If you receive any suspicious emails claiming to be from us, please ignore them.
                                </p>
                            </div>
                            
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """;
        return htmlContent
                .replace("%USERNAME%", username)
                .replace("%ACTIVATION_LINK_BUTTON%", activationLink)
                .replace("%ACTIVATION_LINK_TEXT%", activationLink)
                .replace("%ACTIVATION_LINK_COPY%", activationLink);
    }
}

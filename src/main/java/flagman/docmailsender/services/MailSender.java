package flagman.docmailsender.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import flagman.docmailsender.models.ApproveRequest;
import flagman.docmailsender.models.EmailEntity;
import flagman.docmailsender.repositories.EmailEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.time.LocalDateTime;
import java.util.Properties;

@Service
public class MailSender {
    private final EmailEntityRepository emailEntityRepository;
    @Value("${flagman.mail.sender}")
    private String sender;
    @Value("${flagman.mail.password}")
    private String password;
    private final KafkaTemplate<String, ApproveRequest> kafkaTemplateApproveReq;
    @Autowired
    public MailSender(EmailEntityRepository emailEntityRepository, KafkaTemplate<String, ApproveRequest> kafkaTemplateOrder) {
        this.emailEntityRepository = emailEntityRepository;
        this.kafkaTemplateApproveReq = kafkaTemplateOrder;
    }

    @KafkaListener(topics = "approve-topic", groupId = "mail-group")
    public Boolean sendNotification(String approveJson) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ApproveRequest approve = mapper.readValue(approveJson, ApproveRequest.class);
        EmailEntity email = new EmailEntity();
        email.setEmail(approve.getEmail());
        email.setCode(approve.getCode());
        email.setCreatedAt(LocalDateTime.now());
            // Email details
            String to = approve.getEmail();
            String host = "smtp.mail.ru";

            // System properties and configuration
            Properties properties = System.getProperties();
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", "465");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");

            // Session setup with authentication
            Session session = Session.getInstance(properties, new javax.mail.Authenticator(){
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(sender, password);
                }
            });

            try {
                // Creating and configuring the MimeMessage
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(sender));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                message.setSubject("Код подтверждения подписания документа");

// Создаем мультипарт сообщение
                Multipart multipart = new MimeMultipart();

// Текстовая часть сообщения (HTML)
                MimeBodyPart htmlPart = new MimeBodyPart();
                String htmlContent = """
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <style>
        body {
            font-family: 'Arial', sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
        }
        .header {
            background-color: #4a6fa5;
            color: white;
            padding: 20px;
            text-align: center;
            border-radius: 5px 5px 0 0;
        }
        .content {
            padding: 20px;
            background-color: #f9f9f9;
            border-left: 1px solid #ddd;
            border-right: 1px solid #ddd;
        }
        .code {
            font-size: 24px;
            font-weight: bold;
            color: #4a6fa5;
            text-align: center;
            margin: 20px 0;
            padding: 10px;
            background-color: #e7eff9;
            border-radius: 5px;
            letter-spacing: 3px;
        }
        .footer {
            padding: 15px;
            text-align: center;
            font-size: 12px;
            color: #777;
            background-color: #eee;
            border-radius: 0 0 5px 5px;
            border-left: 1px solid #ddd;
            border-right: 1px solid #ddd;
            border-bottom: 1px solid #ddd;
        }
        .button {
            display: inline-block;
            padding: 10px 20px;
            background-color: #4a6fa5;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            margin: 15px 0;
        }
    </style>
</head>
<body>
    <div class="header">
        <h2>Подтверждение подписания документа</h2>
    </div>
    
    <div class="content">
        <p>Здравствуйте,</p>
        <p>Для завершения процедуры подписания документа, пожалуйста, используйте следующий код подтверждения:</p>
        
        <div class="code">%s</div>
        
        <p>Этот код действителен в течение 15 минут. Если вы не запрашивали подписание документа, проигнорируйте это сообщение.</p>
        
        <p>С уважением,<br>Команда поддержки</p>
    </div>
    
    <div class="footer">
        <p>© 2023 Ваша компания. Все права защищены.</p>
        <p>Это письмо было отправлено автоматически. Пожалуйста, не отвечайте на него.</p>
    </div>
</body>
</html>
""".formatted(approve.getCode());

                htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
                multipart.addBodyPart(htmlPart);

// Альтернативная текстовая версия для почтовых клиентов, которые не поддерживают HTML
                MimeBodyPart textPart = new MimeBodyPart();
                String textContent = """
Подтверждение подписания документа

Здравствуйте,

Для завершения процедуры подписания документа, пожалуйста, используйте следующий код подтверждения:

%s

Этот код действителен в течение 15 минут. Если вы не запрашивали подписание документа, проигнорируйте это сообщение.

С уважением,
Команда поддержки

© 2025 Флагман. Все права защищены.
""".formatted(approve.getCode());

                textPart.setText(textContent);
                multipart.addBodyPart(textPart);

                message.setContent(multipart);


                Transport.send(message);
                email.setSentAt(LocalDateTime.now());
                emailEntityRepository.save(email);
                return true;
            } catch (Exception mex) {
                mex.printStackTrace();
            }
            return false;

    }
    public Boolean sendNotification(ApproveRequest approve) throws JsonProcessingException {
        kafkaTemplateApproveReq.send("approve-topic", approve);
        return true;
    }

}

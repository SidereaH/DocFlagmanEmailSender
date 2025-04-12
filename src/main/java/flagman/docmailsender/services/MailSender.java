package flagman.docmailsender.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import flagman.docmailsender.models.ApproveRequest;
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
import java.util.Properties;

@Service
public class MailSender {
    @Value("${flagman.mail.sender}")
    private String sender;
    @Value("${flagman.mail.password}")
    private String password;
    public MailSender() {
    }

    @KafkaListener(topics = "approve-topic", groupId = "mail-group")
    public Boolean sendNotification(String approveJson) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ApproveRequest approve = mapper.readValue(approveJson, ApproveRequest.class);
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
                message.setSubject("Код подтверждения подписания документа.");

                // Adding Attachments
                Multipart multipart = new MimeMultipart();
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText("Your code: " + approve.getCode());
                multipart.addBodyPart(messageBodyPart);

//                messageBodyPart = new MimeBodyPart();
//                DataSource source = new FileDataSource("path/to/attachment.txt"); // Replace with the actual file path
//                messageBodyPart.setDataHandler(new DataHandler(source));
//                messageBodyPart.setFileName("attachment.txt");
//                multipart.addBodyPart(messageBodyPart);

                message.setContent(multipart);


                Transport.send(message);
                return true;
            } catch (Exception mex) {
                mex.printStackTrace();
            }
            return false;

    }
    public Boolean sendNotification(ApproveRequest approve) throws JsonProcessingException {

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
            message.setSubject("Your email subject goes here");

            // Adding Attachments
            Multipart multipart = new MimeMultipart();
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Your code: " + approve.getCode());
            multipart.addBodyPart(messageBodyPart);

//                messageBodyPart = new MimeBodyPart();
//                DataSource source = new FileDataSource("path/to/attachment.txt"); // Replace with the actual file path
//                messageBodyPart.setDataHandler(new DataHandler(source));
//                messageBodyPart.setFileName("attachment.txt");
//                multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);


            Transport.send(message);
            System.out.println("SUCESS");
            return true;
        } catch (Exception mex) {
            mex.printStackTrace();
        }
        return false;

    }

}

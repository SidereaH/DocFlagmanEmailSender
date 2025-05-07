package flagman.docmailsender.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import flagman.docmailsender.models.ApproveRequest;
import flagman.docmailsender.models.EmailEntity;
import flagman.docmailsender.models.dto.EmailDTO;
import flagman.docmailsender.repositories.EmailEntityRepository;
import flagman.docmailsender.services.MailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.AccessType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@RestController
@RequestMapping("/mail")
public class MailController {
    private final MailSender mailSender;
    private EmailEntityRepository emailEntityRepository;
    @Autowired
    public MailController(MailSender mailSender, EmailEntityRepository emailEntityRepository) {
        this.mailSender = mailSender;
        this.emailEntityRepository = emailEntityRepository;
    }
    @PostMapping("/send")
    public ResponseEntity<ApproveRequest> sendMail(@RequestBody ApproveRequest request) {
       try{

           mailSender.sendNotification(request);
       }
       catch (JsonProcessingException e) {
           e.printStackTrace();
           return ResponseEntity.badRequest().body(new ApproveRequest("error", 0L));
       }
        return ResponseEntity.ok(request);
    }
    @GetMapping
    public ResponseEntity<List<EmailDTO>> getMails() {
        var  mail = new EmailDTO();
        List<EmailDTO> mailList = new ArrayList<>();

        var mails = emailEntityRepository.findAll();

        for(EmailEntity mailEntity : mails) {
            mailList.add(mailEntity.toEmailDTO());
        }
        return ResponseEntity.ok(mailList);

    }

    @GetMapping("/by-mail")
    public ResponseEntity<List<EmailDTO>> getMail(@RequestParam String email) {
        List<EmailEntity> mail;
        try{
            mail = emailEntityRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("email not found"));

        }
        catch (RuntimeException e) {
            List<EmailDTO> list = new ArrayList<>();
            list.add(
                    new EmailDTO(e.getMessage(), 0L, null, null)
            );
            return ResponseEntity.badRequest().body(list);
        }
        List<EmailDTO> finalList = new ArrayList<>();
        for (EmailEntity mailEntity : mail) {
            finalList.add(mailEntity.toEmailDTO());
        }
        return ResponseEntity.ok(finalList) ;
    }

    @GetMapping("/by-phone")
    public ResponseEntity<EmailDTO> getByPhone(@RequestParam String phone) {
        EmailEntity mail;
        try{
            mail = emailEntityRepository.findByPhone(phone).orElseThrow(() -> new RuntimeException("phone not found"));
            return ResponseEntity.ok(mail.toEmailDTO());
        }
        catch (RuntimeException e) {
            mail = new EmailEntity();
            return ResponseEntity.badRequest().body(mail.toEmailDTO());
        }
    }
    @GetMapping("/by-phone-equals")
    public ResponseEntity<Boolean> getEqualsByPhone(@RequestParam String phone, @RequestParam Long code) {
        EmailEntity mail;
        try{
            mail = emailEntityRepository.findByPhone(phone).orElseThrow(() -> new RuntimeException("phone not found"));
            if(mail.getCode().equals(code)) {
                return ResponseEntity.ok(true);
            }

        }
        catch (RuntimeException e) {
            log.error("bad request in gettin equals");
        }
        return ResponseEntity.ok(true);
    }

}

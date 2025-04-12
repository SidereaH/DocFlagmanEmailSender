package flagman.docmailsender;

import com.fasterxml.jackson.core.JsonProcessingException;
import flagman.docmailsender.models.ApproveRequest;
import flagman.docmailsender.services.MailSender;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mail")
public class MailController {
    private final MailSender mailSender;
    public MailController(MailSender mailSender) {
        this.mailSender = mailSender;
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
}

package flagman.docmailsender.models;

import flagman.docmailsender.models.dto.EmailDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private Long code;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    public EmailDTO toEmailDTO() {
        return new EmailDTO(this.email, this.createdAt, this.sentAt);

    }
}

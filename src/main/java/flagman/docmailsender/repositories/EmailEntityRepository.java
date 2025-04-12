package flagman.docmailsender.repositories;

import flagman.docmailsender.models.EmailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailEntityRepository extends JpaRepository<EmailEntity, Long> {
    Optional<List<EmailEntity>> findByEmail(String email);

}

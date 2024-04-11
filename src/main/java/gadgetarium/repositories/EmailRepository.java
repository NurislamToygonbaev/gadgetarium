package gadgetarium.repositories;

import gadgetarium.entities.EmailAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepository extends JpaRepository<EmailAddress, Long> {
}
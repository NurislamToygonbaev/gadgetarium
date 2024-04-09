package gadgetarium.repositories;
import gadgetarium.entities.Mailing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailingRepository extends JpaRepository<Mailing, Long> {
}
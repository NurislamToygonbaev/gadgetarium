package gadgetarium.repositories;

import gadgetarium.entities.EmailAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmailRepository extends JpaRepository<EmailAddress, Long> {
    @Query("select case when count(e) > 0 then true else false end from EmailAddress e where lower(e.email) = lower(:email) ")
    boolean existsByEmail(String email);
}
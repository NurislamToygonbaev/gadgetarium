package gadgetarium.repositories;

import gadgetarium.entities.PasswordResetToken;
import gadgetarium.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    default PasswordResetToken getByToken(String token){
        return findByToken(token).orElseThrow(() ->
                new NotFoundException("token not found"));
    }
}
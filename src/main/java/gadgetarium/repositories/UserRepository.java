package gadgetarium.repositories;

import gadgetarium.entities.User;
import gadgetarium.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    default User getByEmail(String email) {
        return findByEmail(email).orElseThrow(() ->
                new NotFoundException("User with email: " + email + " not found"));
    }

    @Query("select case when count(u) > 0 then true else false end from User u where u.email like :email")
    boolean existsByEmail(String email);
}
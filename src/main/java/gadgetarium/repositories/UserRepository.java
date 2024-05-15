package gadgetarium.repositories;

import gadgetarium.entities.SubGadget;
import gadgetarium.entities.User;
import gadgetarium.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u where u.email like :email")
    Optional<User> findByEmail(String email);

    default User getByEmail(String email) {
        return findByEmail(email).orElseThrow(() ->
                new NotFoundException("User with email: " + email + " not found"));
    }

    @Query("select case when count(u) > 0 then true else false end from User u where u.email like :email")
    boolean existsByEmail(String email);


    @Query("select u from User u join u.basket b where key(b) = :subGadget")
    List<User> findByBasketContainingKey(SubGadget subGadget);

}
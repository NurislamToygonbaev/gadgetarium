package gadgetarium.repositories;

import gadgetarium.entities.SubGadget;
import gadgetarium.entities.User;
import gadgetarium.exceptions.NotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Modifying
    @Transactional
    @Query(value = "delete from users_viewed v where v.viewed_id = :subGadgetId", nativeQuery = true)
    void clearViewed(Long subGadgetId);

    @Modifying
    @Transactional
    @Query(value = "delete from users_likes l where l.likes_id = :subGadgetId", nativeQuery = true)
    void clearLikes(Long subGadgetId);

    @Modifying
    @Transactional
    @Query(value = "delete from users_comparison c where c.comparison_id = :subGadgetId", nativeQuery = true)
    void clearComparison(Long subGadgetId);

    @Modifying
    @Transactional
    @Query(value = "delete from user_basket b where b.basket_key = :subGadgetId", nativeQuery = true)
    void clearBasket(Long subGadgetId);
}
package gadgetarium.repositories;
import gadgetarium.entities.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
    void deleteByEndDateBefore(LocalDate date);
}
package gadgetarium.repositories;
import gadgetarium.entities.Gadget;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GadgetRepository extends JpaRepository<Gadget, Long> {
}
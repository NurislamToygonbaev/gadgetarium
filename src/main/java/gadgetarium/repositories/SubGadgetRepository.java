package gadgetarium.repositories;
import gadgetarium.entities.Gadget;
import gadgetarium.entities.SubGadget;
import gadgetarium.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubGadgetRepository extends JpaRepository<SubGadget, Long> {

    List<SubGadget> findByNameOfGadget(String name);

}
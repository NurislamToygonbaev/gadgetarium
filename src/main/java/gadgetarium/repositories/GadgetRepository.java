package gadgetarium.repositories;
import gadgetarium.entities.Gadget;
import gadgetarium.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GadgetRepository extends JpaRepository<Gadget, Long> {

    Optional<Gadget> findById(Long id);

    default Gadget getGadgetById(Long gadgetId){
        return findById(gadgetId).orElseThrow(() ->
                new NotFoundException("Gadget with id: "+gadgetId+" not found"));
    }

}
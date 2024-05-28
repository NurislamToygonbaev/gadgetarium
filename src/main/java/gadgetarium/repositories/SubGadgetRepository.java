package gadgetarium.repositories;

import gadgetarium.entities.SubGadget;
import gadgetarium.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubGadgetRepository extends JpaRepository<SubGadget, Long> {

   default SubGadget getByID(Long subGadgetId){
      return findById(subGadgetId).orElseThrow(() ->
              new NotFoundException("SubGadget with id: " + subGadgetId + " not found!"));
   }

}
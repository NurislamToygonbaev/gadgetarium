package gadgetarium.repositories;

import gadgetarium.entities.CharValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Map;

public interface CharValueRepository extends JpaRepository<CharValue, Long> {
//    @Query(nativeQuery = true,name = "select c from char_values c join gadgets sub_gadget_char_name sgn on where sgn =:subGadgetCharName")
//    Map<String, String> findTenCharacteristic(Long subGadgetCharName);

}
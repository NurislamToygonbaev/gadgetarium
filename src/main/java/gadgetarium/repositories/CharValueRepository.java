package gadgetarium.repositories;

import gadgetarium.entities.CharValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface CharValueRepository extends JpaRepository<CharValue, Long> {
    @Query(value = """
            select ch.values_key, ch.values 
            from char_value_values ch 
            join char_values c on ch.char_value_id = c.id
            join gadget_char_name gch on c.id = gch.char_name_key
            where gch.gadget_id = :gadgetId
            and gch.char_name like 'Батарея'
            """, nativeQuery = true)
    List<Map<String, String>> findTenCharacteristics(Long gadgetId);

}
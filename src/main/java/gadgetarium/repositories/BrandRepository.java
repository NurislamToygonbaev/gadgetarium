package gadgetarium.repositories;

import gadgetarium.entities.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    boolean existsByBrandName(String name);
}
package gadgetarium.repositories;

import gadgetarium.entities.Brand;
import gadgetarium.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    boolean existsByBrandName(String name);

    default Brand getBrandById(Long brandId){
       return findById(brandId).orElseThrow(() ->
               new NotFoundException("brand with id: "+brandId+" not found"));
    }
}
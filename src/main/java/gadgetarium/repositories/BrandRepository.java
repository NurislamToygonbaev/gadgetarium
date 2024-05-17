package gadgetarium.repositories;

import gadgetarium.dto.response.BrandResponse;
import gadgetarium.entities.Brand;
import gadgetarium.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    boolean existsByBrandName(String name);

    default Brand getBrandById(Long brandId){
       return findById(brandId).orElseThrow(() ->
               new NotFoundException("brand with id: "+brandId+" not found"));
    }

    @Query("select new gadgetarium.dto.response.BrandResponse(b.id, b.logo, b.brandName) from Brand b")
    List<BrandResponse> getAllBrands();
}
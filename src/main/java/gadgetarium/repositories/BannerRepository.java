package gadgetarium.repositories;
import gadgetarium.dto.response.GetAllBannerResponse;
import gadgetarium.entities.Banner;
import gadgetarium.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    default Banner getBannerById(Long bannerId){
        return findById(bannerId).orElseThrow(() ->
                new NotFoundException("Banner with id: "+bannerId+" not found"));
    }
}
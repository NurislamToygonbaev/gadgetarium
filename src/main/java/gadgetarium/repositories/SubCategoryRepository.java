package gadgetarium.repositories;
import gadgetarium.entities.SubCategory;
import gadgetarium.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {

    default SubCategory getSubCategoryById(Long subCategoryId){
        return findById(subCategoryId).orElseThrow(() ->
                new NotFoundException("Subcategory with id: "+subCategoryId+" not found"));
    }
}
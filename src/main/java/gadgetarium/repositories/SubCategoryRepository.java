package gadgetarium.repositories;
import gadgetarium.dto.response.CatResponse;
import gadgetarium.entities.SubCategory;
import gadgetarium.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {

    default SubCategory getSubCategoryById(Long subCategoryId){
        return findById(subCategoryId).orElseThrow(() ->
                new NotFoundException("Subcategory with id: "+subCategoryId+" not found"));
    }

    @Query("select new gadgetarium.dto.response.CatResponse(s.id, s.subCategoryName) from SubCategory s where s.category.id =:catId")
    List<CatResponse> getSubCategories(Long catId);
}
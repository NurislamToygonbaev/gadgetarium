package gadgetarium.repositories;
import gadgetarium.dto.response.CatResponse;
import gadgetarium.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("select new gadgetarium.dto.response.CatResponse(c.id, c.categoryName) from Category c")
    List<CatResponse> getAllCategories();
}
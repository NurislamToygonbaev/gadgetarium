package gadgetarium.repositories;

import gadgetarium.entities.Feedback;
import gadgetarium.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    @Query("select f from Feedback f where f.reviewType = 'NOT_READ'")
    List<Feedback> findUnansweredFeedbacks();

    @Query("select f from Feedback f where f.reviewType = 'READ'")
    List<Feedback> findAnsweredFeedbacks();

    default Feedback getByIdd(Long id){
       return findById(id).orElseThrow(() ->
                new NotFoundException("Feedback with id: " + id + " not found!"));
    }
}
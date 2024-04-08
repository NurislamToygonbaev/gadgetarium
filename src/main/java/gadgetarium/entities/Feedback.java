package gadgetarium.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.DETACH;
@Getter
@Setter
@Entity
@Table(name = "feedbacks")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "feedback_seq")
    @SequenceGenerator(name = "feedback_seq", allocationSize = 1)
    private Long id;
    private int rating;
    private String description;
    private LocalDateTime dateAndTime;
    private String responseAdmin;

    @ElementCollection
    private List<String> images;

    @ManyToOne(cascade = {DETACH})
    private Gadget gadget;

    @ManyToOne(cascade = {DETACH})
    private User user;

    private void addImage(String image){
        if (this.images == null) this.images = new ArrayList<>();
        this.images.add(image);
    }
}

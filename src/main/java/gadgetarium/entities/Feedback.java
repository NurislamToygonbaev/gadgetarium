package gadgetarium.entities;

import gadgetarium.enums.ReviewType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
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
    @SequenceGenerator(name = "feedback_seq", allocationSize = 1, initialValue = 60)
    private Long id;
    private int rating;

    @Column(length = 500)
    private String description;
    private LocalDateTime dateAndTime;
    @Column(length = 500)
    private String responseAdmin;

    @Enumerated(EnumType.STRING)
    private ReviewType reviewType;

    @Size(max = 1000)
    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> images;

    @ManyToOne(cascade = {DETACH}, fetch = FetchType.LAZY)
    private Gadget gadget;

    @ManyToOne(cascade = {DETACH}, fetch = FetchType.LAZY)
    private User user;

    @PrePersist
    private void initialReview() {
        this.reviewType = ReviewType.NOT_READ;
        this.dateAndTime = LocalDateTime.now();
        this.images = new ArrayList<>();
        this.dateAndTime = LocalDateTime.now();
    }

    @PreUpdate
    private void updatedReview(){
        this.dateAndTime = LocalDateTime.now();
    }
}

package gadgetarium.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    @ManyToOne
    private Gadget gadget;

    @ManyToOne
    private User user;
}

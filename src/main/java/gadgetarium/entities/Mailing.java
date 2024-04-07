package gadgetarium.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "mailings")
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Mailing {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mailing_seq")
    @SequenceGenerator(name = "mailing_seq", allocationSize = 1)
    private Long id;
    private String image;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}
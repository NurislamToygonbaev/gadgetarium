package gadgetarium.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

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
    @SequenceGenerator(name = "mailing_seq", allocationSize = 1, initialValue = 60)
    private Long id;
    @Column(length = 1000)
    private String image;
    private String title;
    @Column(length = 500)
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}
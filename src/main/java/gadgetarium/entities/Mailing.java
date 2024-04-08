package gadgetarium.entities;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.GenerationType;
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
    @SequenceGenerator(name = "mailing_seq", allocationSize = 1)
    private Long id;
    private String image;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}
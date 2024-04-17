package gadgetarium.entities;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;
import static jakarta.persistence.CascadeType.DETACH;

@Getter
@Setter
@Entity
@Table(name = "discounts")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "discount_seq")
    @SequenceGenerator(name = "discount_seq", allocationSize = 1)
    private Long id;
    private int percent;
    private LocalDate startDate;
    private LocalDate endDate;

    @OneToOne(cascade = {DETACH})
    private SubGadget subGadget;
}
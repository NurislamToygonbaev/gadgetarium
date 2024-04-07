package gadgetarium.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

    @OneToOne
    private SubGadget subGadget;
}
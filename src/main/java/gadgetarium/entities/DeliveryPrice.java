package gadgetarium.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "delivery")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "delivery_seq")
    @SequenceGenerator(name = "delivery_seq", allocationSize = 1)
    private Long id;
    private BigDecimal price;

    @OneToOne(cascade = {CascadeType.DETACH})
    private Order order;
}

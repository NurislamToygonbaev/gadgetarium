package gadgetarium.entities;

import gadgetarium.enums.Payment;
import gadgetarium.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq")
    @SequenceGenerator(name = "order_seq", allocationSize = 1)
    private Long id;
    private Long number;
    private boolean typeOrder;
    private LocalDate createdAt;
    private BigDecimal deliveryPrice;

    @Enumerated(EnumType.STRING)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToMany(mappedBy = "orders")
    private List<Gadget> gadgets;

    @ManyToOne
    private User user;
}
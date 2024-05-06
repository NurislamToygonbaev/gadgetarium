package gadgetarium.entities;

import gadgetarium.enums.Payment;
import gadgetarium.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.REFRESH;
import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.DETACH;

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
    @SequenceGenerator(name = "order_seq", allocationSize = 1, initialValue = 60)
    private Long id;
    private Long number;
    private boolean typeOrder;
    private LocalDate createdAt;
    private BigDecimal deliveryPrice;
    @Enumerated(EnumType.STRING)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToMany(cascade = {DETACH, MERGE, REFRESH})
    private List<Gadget> gadgets;

    @ManyToOne(cascade = {DETACH})
    private User user;

    private void addGadget(Gadget gadget) {
        if (this.gadgets == null) this.gadgets = new ArrayList<>();
        this.gadgets.add(gadget);
    }

    @PrePersist
    private void initialReview() {
        this.gadgets = new ArrayList<>();
    }
}
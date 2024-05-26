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

import static jakarta.persistence.CascadeType.*;

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
    private BigDecimal totalPrice;
    private BigDecimal discountPrice;

    @Enumerated(EnumType.STRING)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToMany(cascade = {DETACH, PERSIST, MERGE, REFRESH}, fetch = FetchType.EAGER)
    private List<SubGadget> subGadgets;

    @ManyToOne(cascade = {DETACH})
    private User user;

    public void addGadget(SubGadget subGadget) {
        if (this.subGadgets == null) this.subGadgets = new ArrayList<>();
        this.subGadgets.add(subGadget);
    }

    @PrePersist
    private void initialReview() {
        this.subGadgets = new ArrayList<>();
        this.createdAt = LocalDate.now();
    }
}
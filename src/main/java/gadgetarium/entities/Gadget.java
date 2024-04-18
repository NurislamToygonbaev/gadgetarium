package gadgetarium.entities;

import gadgetarium.enums.Memory;
import gadgetarium.enums.Ram;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.CascadeType.REFRESH;
import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.DETACH;
import static jakarta.persistence.CascadeType.PERSIST;

@Getter
@Setter
@Entity
@Table(name = "gadgets")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Gadget {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gadget_seq")
    @SequenceGenerator(name = "gadget_seq", allocationSize = 1, initialValue = 60)
    private Long id;
    private int warranty;
    private LocalDate releaseDate;
    private Long article;
    @Column(length = 500)
    private String videoUrl;
    @Column(length = 500)
    private String PDFUrl;
    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    private Memory memory;

    @Enumerated(EnumType.STRING)
    private Ram ram;

    @OneToOne(mappedBy = "gadget", cascade = {PERSIST, REFRESH, REMOVE}, fetch = FetchType.EAGER)
    private SubGadget subGadget;

    @OneToMany(mappedBy = "gadget", cascade = {REMOVE, MERGE, REFRESH}, fetch = FetchType.EAGER)
    private List<Feedback> feedbacks;

    @ManyToMany(mappedBy = "gadgets", cascade = {DETACH, MERGE, REFRESH})
    private List<Order> orders;

    @ManyToOne(cascade = {DETACH})
    private SubCategory subCategory;

    @ManyToOne(fetch = FetchType.EAGER)
    private Brand brand;

    private void addFeedback(Feedback feedback) {
        if (this.feedbacks == null) this.feedbacks = new ArrayList<>();
        this.feedbacks.add(feedback);
    }

    private void addOrder(Order order) {
        if (this.orders == null) this.orders = new ArrayList<>();
        this.orders.add(order);
    }
}
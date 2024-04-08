package gadgetarium.entities;
import gadgetarium.enums.Memory;
import gadgetarium.enums.Ram;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ManyToMany;
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
    @SequenceGenerator(name = "gadget_seq", allocationSize = 1)
    private Long id;
    private int warranty;
    private LocalDate releaseDate;
    private Long article;
    private String videoUrl;
    private String PDFUrl;
    private String description;

    @Enumerated(EnumType.STRING)
    private Memory memory;

    @Enumerated(EnumType.STRING)
    private Ram ram;

    @OneToOne(mappedBy = "gadget", cascade = {PERSIST, REFRESH, REMOVE})
    private SubGadget subGadget;

    @OneToMany(mappedBy = "gadget", cascade = {REMOVE, MERGE, REFRESH})
    private List<Feedback> feedbacks;

    @ManyToMany(mappedBy = "gadgets", cascade = {DETACH, MERGE, REFRESH})
    private List<Order> orders;

    @ManyToOne(cascade = {DETACH})
    private Brand brand;

    private void addFeedback(Feedback feedback){
        if (this.feedbacks == null) this.feedbacks = new ArrayList<>();
        this.feedbacks.add(feedback);
    }

    private void addOrder(Order order){
        if (this.orders == null) this.orders = new ArrayList<>();
        this.orders.add(order);
    }
}
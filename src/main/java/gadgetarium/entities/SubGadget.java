package gadgetarium.entities;

import gadgetarium.enums.Memory;
import gadgetarium.enums.Ram;
import gadgetarium.enums.RemotenessStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.*;

import static jakarta.persistence.CascadeType.*;

@Getter
@Setter
@Entity
@Table(name = "sub_gadgets")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubGadget {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sub_gadget_seq")
    @SequenceGenerator(name = "sub_gadget_seq", allocationSize = 1, initialValue = 60)
    private Long id;
    private int quantity;
    private String mainColour;
    private int countSim;
    private BigDecimal price;
    private Long article;

    @Enumerated(EnumType.STRING)
    private Memory memory;

    @Enumerated(EnumType.STRING)
    private Ram ram;

    @Enumerated(EnumType.STRING)
    private RemotenessStatus remotenessStatus;

    @Size(max = 1000)
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> images;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> uniFiled;

    @ManyToOne(cascade = {DETACH, MERGE, REFRESH})
    private Gadget gadget;

    @ManyToMany(mappedBy = "subGadgets", cascade = {DETACH, REFRESH}, fetch = FetchType.EAGER)
    private List<Order> orders;

    public void addImage(String image) {
        if (this.images == null) this.images = new ArrayList<>();
        this.images.add(image);
    }

    public void addUniField(String filed) {
        if (this.uniFiled == null) this.uniFiled = new ArrayList<>();
        this.uniFiled.add(filed);
    }

    public void addOrder(Order order) {
        if (this.orders == null) this.orders = new ArrayList<>();
        this.orders.add(order);
    }


    @PrePersist
    private void addNewInfo(){
        this.orders = new ArrayList<>();
        this.images = new ArrayList<>();
        this.uniFiled = new ArrayList<>();
        this.remotenessStatus = RemotenessStatus.NOT_REMOTE;
    }

}
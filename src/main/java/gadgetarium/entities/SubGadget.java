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
    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> images;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> uniFiled;

    @ManyToOne(cascade = {DETACH, MERGE, REFRESH}, fetch = FetchType.LAZY)
    private Gadget gadget;

    @ManyToMany(mappedBy = "subGadgets", cascade = {DETACH, REFRESH}, fetch = FetchType.LAZY)
    private List<Order> orders;

    public void addUniField(String filed) {
        if (this.uniFiled == null) this.uniFiled = new ArrayList<>();
        this.uniFiled.add(filed);
    }

    @PrePersist
    private void addNewInfo(){
        this.orders = new ArrayList<>();
        this.images = new ArrayList<>();
        this.uniFiled = new ArrayList<>();
        this.remotenessStatus = RemotenessStatus.NOT_REMOTE;
    }

}
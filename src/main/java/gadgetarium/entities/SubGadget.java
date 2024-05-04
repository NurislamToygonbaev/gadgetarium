package gadgetarium.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.*;

import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.CascadeType.REFRESH;
import static jakarta.persistence.CascadeType.MERGE;

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
    private String nameOfGadget;
    private BigDecimal price;
    private int quantity;
    private double rating;
    private String mainColour;
    private int countSim;
    private BigDecimal currentPrice;

    @Size(max = 1000)
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> images;

    @ElementCollection(fetch = FetchType.EAGER)
    @Size(max = 3000)
    private Map<String, String> characteristics;

    @ElementCollection(fetch = FetchType.EAGER)
    @Size(max = 3000)
    private Map<CharValue,String> charName;

    @OneToOne(cascade = {REMOVE, MERGE, REFRESH})
    private Gadget gadget;

    @OneToOne(mappedBy = "subGadget", cascade = {REMOVE, REFRESH, MERGE})
    private Discount discount;

    public void addImage(String image) {
        if (this.images == null) this.images = new ArrayList<>();
        this.images.add(image);
    }

    public void addCharacteristic(String key, String value) {
        if (this.characteristics == null) this.characteristics = new LinkedHashMap<>();
        this.characteristics.put(key, value);
    }

    @PrePersist
    private void addNewInfo(){
        this.characteristics = new LinkedHashMap<>();
        this.images = new ArrayList<>();
        this.charName = new LinkedHashMap<>();
    }

}
package gadgetarium.entities;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @SequenceGenerator(name = "sub_gadget_seq", allocationSize = 1)
    private Long id;
    private String nameOfGadget;
    private BigDecimal price;
    private int quantity;
    private int rating;
    private String mainColour;

    @ElementCollection
    private List<String> images;

    @ElementCollection
    private Map<String, String> characteristics;

    @OneToOne(cascade = {REMOVE, MERGE, REFRESH})
    private Gadget gadget;

    @OneToOne(mappedBy = "subGadget", cascade = {REMOVE, REFRESH, MERGE})
    private Discount discount;

    private void addImage(String image) {
        if (this.images == null) this.images = new ArrayList<>();
        this.images.add(image);
    }

    private void addCharacteristic(String key, String value) {
        if (this.characteristics == null) this.characteristics = new HashMap<>();
        this.characteristics.put(key, value);
    }
}
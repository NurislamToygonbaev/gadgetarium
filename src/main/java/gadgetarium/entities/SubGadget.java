package gadgetarium.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    @ElementCollection
    private List<String> images;

    @ElementCollection
    private Map<String, String> characteristics;

    @OneToOne
    private Gadget gadget;

    @OneToOne(mappedBy = "subGadget")
    private Discount discount;
}
package gadgetarium.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "brands")
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "brand_seq")
    @SequenceGenerator(name = "brand_seq", allocationSize = 1)
    private Long id;
    private String brandName;
    private String logo;

    @ManyToOne
    private SubCategory subCategory;

    @OneToMany(mappedBy = "brand")
    private List<Gadget> gadgets;
}
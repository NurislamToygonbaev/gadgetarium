package gadgetarium.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.CascadeType.REFRESH;
import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.DETACH;

@Getter
@Setter
@Entity
@Table(name = "brands")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "brand_seq")
    @SequenceGenerator(name = "brand_seq", allocationSize = 1, initialValue = 60)
    private Long id;
    private String brandName;

    @Column(length = 1000)
    private String logo;

    @OneToMany(mappedBy = "brand")
    private List<Gadget> gadgets;
}
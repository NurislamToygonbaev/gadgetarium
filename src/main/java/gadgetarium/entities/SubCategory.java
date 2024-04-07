package gadgetarium.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "sub_categories")
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class SubCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sub_category_seq")
    @SequenceGenerator(name = "sub_category_seq", allocationSize = 1)
    private Long id;
    private String subCategoryName;

    @ManyToOne
    private Category category;

    @OneToMany(mappedBy = "subCategory")
    private List<Gadget> gadgets;
}
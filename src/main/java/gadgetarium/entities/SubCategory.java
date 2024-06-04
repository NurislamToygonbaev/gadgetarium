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
    @SequenceGenerator(name = "sub_category_seq", allocationSize = 1, initialValue = 60)
    private Long id;
    private String subCategoryName;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @OneToMany(mappedBy = "subCategory", cascade = {MERGE, REFRESH, REMOVE}, orphanRemoval = true,fetch = FetchType.LAZY)
    private List<Gadget> gadgets;

    public void addGadget(Gadget gadget) {
        if (this.gadgets == null) this.gadgets = new ArrayList<>();
        this.gadgets.add(gadget);
    }

    @PrePersist
    private void initialReview() {
        this.gadgets = new ArrayList<>();
    }
}
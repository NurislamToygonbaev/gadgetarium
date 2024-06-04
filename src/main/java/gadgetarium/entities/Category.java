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
@Table(name = "categories")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_seq")
    @SequenceGenerator(name = "category_seq", allocationSize = 1, initialValue = 60)
    private Long id;
    private String categoryName;

    @OneToMany(cascade = {REMOVE, MERGE, REFRESH}, mappedBy = "category", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<SubCategory> subCategories;

    @PrePersist
    private void initialReview() {
        this.subCategories = new ArrayList<>();
    }
}
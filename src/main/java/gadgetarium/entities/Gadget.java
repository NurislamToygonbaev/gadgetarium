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

import java.time.LocalDate;
import java.util.*;

import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.CascadeType.REFRESH;
import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.DETACH;
import static jakarta.persistence.CascadeType.PERSIST;

@Getter
@Setter
@Entity
@Table(name = "gadgets")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Gadget {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gadget_seq")
    @SequenceGenerator(name = "gadget_seq", allocationSize = 1, initialValue = 60)
    private Long id;
    private int warranty;
    private LocalDate releaseDate;
    @Column(length = 500)
    private String videoUrl;
    @Column(length = 500)
    private String PDFUrl;
    @Column(length = 1000)
    private String description;
    private String nameOfGadget;
    private double rating;

    @OneToMany(mappedBy = "gadget", cascade = {MERGE, REFRESH, REMOVE}, fetch = FetchType.EAGER)
    private List<SubGadget> subGadgets;

    @OneToMany(mappedBy = "gadget", cascade = {REMOVE, MERGE, REFRESH}, fetch = FetchType.EAGER)
    private List<Feedback> feedbacks;

    @ManyToOne(cascade = {DETACH})
    private SubCategory subCategory;

    @ManyToOne(fetch = FetchType.EAGER)
    private Brand brand;

    @ElementCollection(fetch = FetchType.EAGER)
    @Size(max = 3000)
    private Map<CharValue,String> charName;

    @OneToOne(mappedBy = "gadget", cascade = {REMOVE, REFRESH, MERGE})
    private Discount discount;

    public void addFeedback(Feedback feedback) {
        if (this.feedbacks == null) this.feedbacks = new ArrayList<>();
        this.feedbacks.add(feedback);
    }

    public void addSUbGadget(SubGadget subGadget) {
        if (this.subGadgets == null) this.subGadgets = new ArrayList<>();
        this.subGadgets.add(subGadget);
    }

    public void addCharName(CharValue key, String value) {
        if (this.charName == null) {
            this.charName = new HashMap<>();
        }
        this.charName.put(key, value);
    }

    @PrePersist
    private void initialReview() {
        this.subGadgets = new ArrayList<>();
        this.feedbacks = new ArrayList<>();
        this.releaseDate = LocalDate.now();
        this.charName = new LinkedHashMap<>();
    }
}
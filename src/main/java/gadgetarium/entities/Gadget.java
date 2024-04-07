package gadgetarium.entities;

import gadgetarium.enums.Memory;
import gadgetarium.enums.Ram;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;


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
    @SequenceGenerator(name = "gadget_seq", allocationSize = 1)
    private Long id;
    private String brandName;
    private String brandLogo;
    private int warranty;
    private LocalDate releaseDate;
    private Long article;
    private String videoUrl;
    private String PDFUrl;
    private String description;
    private String mainColour;

    @Enumerated(EnumType.STRING)
    private Memory memory;

    @Enumerated(EnumType.STRING)
    private Ram ram;

    @ManyToOne
    private SubCategory subCategory;

    @OneToOne(mappedBy = "gadget")
    private SubGadget subGadget;

    @OneToMany(mappedBy = "gadget")
    private List<Feedback> feedbacks;

    @ManyToMany
    private List<Order> orders;
}
package gadgetarium.entities;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@Entity
@Table(name = "contacts")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_seq")
    @SequenceGenerator(name = "contact_seq", allocationSize = 1, initialValue = 60)
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String phoneNumber;
    private String message;
}
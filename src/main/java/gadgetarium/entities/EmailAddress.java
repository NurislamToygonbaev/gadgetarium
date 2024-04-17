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
@Table(name = "email_addresses")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "email_seq")
    @SequenceGenerator(name = "email_seq", allocationSize = 1, initialValue = 60)
    private Long id;
    private String email;
}
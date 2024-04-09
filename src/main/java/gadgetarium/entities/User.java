package gadgetarium.entities;

import gadgetarium.enums.Role;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.CascadeType.REFRESH;
import static jakarta.persistence.CascadeType.DETACH;
import static jakarta.persistence.CascadeType.MERGE;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", allocationSize = 1)
    private Long id;
    private String firstName;
    private String lastName;
    private String image;
    private String phoneNumber;
    private String email;
    private String password;
    private String address;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = {DETACH, MERGE, REFRESH})
    private List<Order> orders;

    @OneToMany(mappedBy = "user", cascade = {REMOVE, MERGE, REFRESH})
    private List<Feedback> feedbacks;

    @ManyToMany(cascade = {REFRESH, MERGE})
    private List<SubGadget> basket;

    @ManyToMany(cascade = {REFRESH, MERGE})
    private List<SubGadget> comparison;

    @ManyToMany(cascade = {REFRESH, MERGE})
    private List<SubGadget> viewed;

    @ManyToMany(cascade = {REFRESH, MERGE})
    private List<SubGadget> likes;

    private void addOrder(Order order) {
        if (this.orders == null) this.orders = new ArrayList<>();
        this.orders.add(order);
    }

    private void addFeedback(Feedback feedback) {
        if (this.feedbacks == null) this.feedbacks = new ArrayList<>();
        this.feedbacks.add(feedback);
    }

    private void addBasket(SubGadget basket) {
        if (this.basket == null) this.basket = new ArrayList<>();
        this.basket.add(basket);
    }

    private void addComparison(SubGadget comparison) {
        if (this.comparison == null) this.comparison = new ArrayList<>();
        this.comparison.add(comparison);
    }

    private void addViewed(SubGadget viewed) {
        if (this.viewed == null) this.viewed = new ArrayList<>();
        this.viewed.add(viewed);
    }

    private void addLikes(SubGadget likes) {
        if (this.likes == null) this.likes = new ArrayList<>();
        this.likes.add(likes);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(role);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

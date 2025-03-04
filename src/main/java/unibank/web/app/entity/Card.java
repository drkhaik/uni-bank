package unibank.web.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
//@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String cid;

    @Column(unique = true, nullable = false)
    private String cardNumber;
    private String cardHolder;
    private Double balance;
    private String cvv;
    private LocalDateTime exp;
    private String pin;
    private String billingAddress;

    @CreationTimestamp
    private LocalDateTime issuedAt;
    @UpdateTimestamp
    private LocalDateTime lastUpdated;

    @OneToOne
    @JoinColumn(name = "owner_id")
    private User owner;
}
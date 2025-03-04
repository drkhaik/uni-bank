package unibank.web.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uid;

    private String firstName;
    private String lastName;

    @Column(nullable = false, unique = true)
    private String userName;
    private Date dob;
    private long tel;
    private String tag;
    private String password;
    private String gender;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToMany
    private List<String> roles;

    @OneToOne(mappedBy = "owner")
    private Card card;
}

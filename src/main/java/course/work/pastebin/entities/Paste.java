package course.work.pastebin.entities;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;


@Entity
@Table(name = "pastes")
@Data
public class Paste {
    @Id
    private Long id;

    @Column(unique = true, nullable = false)
    private String slug;

    private String title;

    @Lob
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date creationDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessType accessType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        this.creationDate = new Date();
    }

    public boolean isExpired() {
        return expirationDate != null && expirationDate.before(new Date());
    }
}

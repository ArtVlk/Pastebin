package course.work.pastebin.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "pastes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Paste implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    private String slug;

    private String title;

    @Column(columnDefinition = "TEXT")
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

    @Column(name = "create_time_ms")
    private long createTimeMs;

    public long getCreateTimeMs() {
        return createTimeMs;
    }
    public void setCreateTimeMs(long createTimeMs) {
        this.createTimeMs = createTimeMs;
    }

    @PrePersist
    protected void onCreate() {
        this.creationDate = new Date();
    }

    public boolean isExpired() {
        return expirationDate != null && expirationDate.before(new Date());
    }
}

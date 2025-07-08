package shop.matjalalzz.domain.comment.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.matjalalzz.domain.mock.MockParty;
import shop.matjalalzz.domain.mock.MockUser;
import shop.matjalalzz.global.unit.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Table(
    indexes = {
        @Index(name = "idx_comment_writer", columnList = "writer_id"),
        @Index(name = "idx_comment_party", columnList = "party_id")
    }
)
public class Comment extends BaseEntity {

    @Id
    @Column(name = "comment_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 65_535, nullable = false)
    @Lob
    private String content;

    @Column(nullable = false)
    private boolean deleted = false;

    @ManyToOne
    private Comment parent;

    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> children;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private MockParty party;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private MockUser writer;


}

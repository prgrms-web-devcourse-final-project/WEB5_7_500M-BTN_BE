package shop.matjalalzz.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.user.entity.User;

@Entity
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
    indexes = {
        @Index(name = "idx_chatMessage_load", columnList = "party_id, chat_message_id"),
    }
)
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {

    @Id
    @Column(name = "chat_message_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id")
    private Party party;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    MessageType type;


    //TODO: 응답에 시간 포함
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime sendAt;

}

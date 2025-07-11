package shop.matjalalzz.comment.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shop.matjalalzz.comment.dao.CommentRepository;
import shop.matjalalzz.comment.dto.CommentCreateRequest;
import shop.matjalalzz.comment.dto.CommentResponse;
import shop.matjalalzz.comment.entity.Comment;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.party.dao.PartyRepository;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PartyRepository partyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    @Nested
    @DisplayName("댓글 생성 테스트")
    class CreateCommentTest {

        @Test
        @DisplayName("댓글 생성 성공")
        void createComment_success() {
            // given
            Long partyId = 1L;
            Long writerId = 1L;
            Long commentId = 1L;

            CommentCreateRequest request = CommentCreateRequest.builder()
                .content("테스트 댓글")
                .build();

            Party party = mock(Party.class);

            User writer = mock(User.class);
            when(writer.getId()).thenReturn(writerId);

            Comment comment = Comment.builder()
                .id(commentId)
                .content(request.content())
                .party(party)
                .writer(writer)
                .build();

            when(partyRepository.findById(partyId)).thenReturn(Optional.of(party));
            when(userRepository.findById(writerId)).thenReturn(Optional.of(writer));
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);

            // when
            CommentResponse response = commentService.createComment(request, partyId, writerId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.commentId()).isEqualTo(commentId);
            assertThat(response.content()).isEqualTo(request.content());
            assertThat(response.writer().userId()).isEqualTo(writerId);

            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("대댓글 생성 성공")
        void createReplyComment_success() {
            // given
            Long partyId = 1L;
            Long writerId = 1L;
            Long parentId = 1L;
            Long commentId = 2L;

            CommentCreateRequest request = CommentCreateRequest.builder()
                .parentId(parentId)
                .content("테스트 대댓글")
                .build();

            Party party = mock(Party.class);

            User writer = mock(User.class);
            when(writer.getId()).thenReturn(writerId);

            Comment parentComment = Comment.builder()
                .id(parentId)
                .content("부모 댓글")
                .party(party)
                .writer(writer)
                .build();

            Comment childComment = Comment.builder()
                .id(commentId)
                .content(request.content())
                .parent(parentComment)
                .party(party)
                .writer(writer)
                .build();

            when(partyRepository.findById(partyId)).thenReturn(Optional.of(party));
            when(userRepository.findById(writerId)).thenReturn(Optional.of(writer));
            when(commentRepository.findById(parentId)).thenReturn(Optional.of(parentComment));
            when(commentRepository.save(any(Comment.class))).thenReturn(childComment);

            // when
            CommentResponse response = commentService.createComment(request, partyId, writerId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.commentId()).isEqualTo(commentId);
            assertThat(response.parentId()).isEqualTo(parentId);
            assertThat(response.content()).isEqualTo(request.content());
            assertThat(response.writer().userId()).isEqualTo(writerId);

            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("존재하지 않는 파티에 댓글 생성 실패")
        void createComment_partyNotFound_fail() {
            // given
            Long partyId = 1L;
            Long writerId = 1L;

            CommentCreateRequest request = CommentCreateRequest.builder()
                .content("테스트 댓글")
                .build();

            when(partyRepository.findById(partyId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.createComment(request, partyId, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND);

            verify(commentRepository, never()).save(any(Comment.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자가 댓글 생성 실패")
        void createComment_userNotFound_fail() {
            // given
            Long partyId = 1L;
            Long writerId = 1L;

            CommentCreateRequest request = CommentCreateRequest.builder()
                .content("테스트 댓글")
                .build();

            Party party = mock(Party.class);

            when(partyRepository.findById(partyId)).thenReturn(Optional.of(party));
            when(userRepository.findById(writerId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.createComment(request, partyId, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND);

            verify(commentRepository, never()).save(any(Comment.class));
        }

        @Test
        @DisplayName("존재하지 않는 부모 댓글로 대댓글 생성 실패")
        void createReplyComment_parentNotFound_fail() {
            // given
            Long partyId = 1L;
            Long writerId = 1L;
            Long parentId = 1L;

            CommentCreateRequest request = CommentCreateRequest.builder()
                .parentId(parentId)
                .content("테스트 대댓글")
                .build();

            Party party = mock(Party.class);

            User writer = mock(User.class);

            when(partyRepository.findById(partyId)).thenReturn(Optional.of(party));
            when(userRepository.findById(writerId)).thenReturn(Optional.of(writer));
            when(commentRepository.findById(parentId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.createComment(request, partyId, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND);

            verify(commentRepository, never()).save(any(Comment.class));
        }
    }
}
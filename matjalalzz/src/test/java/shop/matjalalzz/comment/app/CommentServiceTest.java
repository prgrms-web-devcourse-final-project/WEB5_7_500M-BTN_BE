package shop.matjalalzz.comment.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
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

    @Nested
    @DisplayName("댓글 조회 테스트")
    class FindCommentTest {

        @Test
        @DisplayName("단일 댓글 조회 성공")
        void findComment_success() {
            // given
            Long commentId = 1L;
            Long writerId = 1L;

            Party party = mock(Party.class);

            User writer = mock(User.class);
            when(writer.getId()).thenReturn(writerId);
            when(writer.getNickname()).thenReturn("작성자");

            Comment comment = Comment.builder()
                .id(commentId)
                .content("테스트 댓글")
                .party(party)
                .writer(writer)
                .build();

            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // when
            CommentResponse response = commentService.findComment(commentId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.commentId()).isEqualTo(commentId);
            assertThat(response.content()).isEqualTo("테스트 댓글");
            assertThat(response.writer().userId()).isEqualTo(writerId);
            assertThat(response.writer().nickname()).isEqualTo("작성자");
        }

        @Test
        @DisplayName("존재하지 않는 댓글 조회 실패")
        void findComment_notFound_fail() {
            // given
            Long commentId = 1L;

            when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.findComment(commentId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND);
        }

        @Test
        @DisplayName("모임별 댓글 목록 조회 성공")
        void findCommentsByParty_success() {
            // given
            Long partyId = 1L;
            Long writerId = 1L;

            Party party = mock(Party.class);

            User writer = mock(User.class);
            when(writer.getId()).thenReturn(writerId);

            Comment comment1 = Comment.builder()
                .id(1L)
                .content("테스트 댓글 1")
                .party(party)
                .writer(writer)
                .build();

            Comment comment2 = Comment.builder()
                .id(2L)
                .content("테스트 댓글 2")
                .party(party)
                .writer(writer)
                .build();

            List<Comment> comments = Arrays.asList(comment1, comment2);

            when(commentRepository.findAllByPartyId(partyId)).thenReturn(comments);

            // when
            List<CommentResponse> responses = commentService.findCommentsByParty(partyId);

            // then
            assertThat(responses).isNotNull();
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).commentId()).isEqualTo(1L);
            assertThat(responses.get(0).content()).isEqualTo("테스트 댓글 1");
            assertThat(responses.get(1).commentId()).isEqualTo(2L);
            assertThat(responses.get(1).content()).isEqualTo("테스트 댓글 2");
        }

        @Test
        @DisplayName("모임별 댓글이 없는 경우 빈 목록 반환")
        void findCommentsByParty_emptyList() {
            // given
            Long partyId = 1L;

            when(commentRepository.findAllByPartyId(partyId)).thenReturn(List.of());

            // when
            List<CommentResponse> responses = commentService.findCommentsByParty(partyId);

            // then
            assertThat(responses).isNotNull();
            assertThat(responses).isEmpty();
        }
    }
}
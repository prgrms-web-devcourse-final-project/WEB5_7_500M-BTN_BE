package shop.matjalalzz.comment.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
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
import shop.matjalalzz.comment.dto.CommentResponse;
import shop.matjalalzz.comment.entity.Comment;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.inquiry.dao.InquiryRepository;
import shop.matjalalzz.inquiry.entity.Inquiry;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Role;

@ExtendWith(MockitoExtension.class)
class CommentQueryServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserService userService;

    @Mock
    private InquiryRepository inquiryRepository;

    @InjectMocks
    private CommentQueryService commentQueryService;

    @Nested
    @DisplayName("단일 댓글 조회 테스트")
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
            CommentResponse response = commentQueryService.findComment(commentId);

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
            assertThatThrownBy(() -> commentQueryService.findComment(commentId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND);
        }

        @Test
        @DisplayName("getComment 메소드 테스트 - 성공")
        void getComment_success() {
            // given
            Long commentId = 1L;
            Comment comment = mock(Comment.class);

            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // when
            Comment result = commentQueryService.getComment(commentId);

            // then
            assertThat(result).isEqualTo(comment);
        }

        @Test
        @DisplayName("getComment 메소드 테스트 - 실패")
        void getComment_notFound_fail() {
            // given
            Long commentId = 1L;

            when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentQueryService.getComment(commentId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("모임별 댓글 조회 테스트")
    class FindCommentsByPartyTest {

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
            List<CommentResponse> responses = commentQueryService.findCommentsByParty(partyId);

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
            List<CommentResponse> responses = commentQueryService.findCommentsByParty(partyId);

            // then
            assertThat(responses).isNotNull();
            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("문의별 댓글 조회 테스트")
    class FindCommentsByInquiryTest {

        @Test
        @DisplayName("관리자가 문의 댓글 조회 성공")
        void findCommentsByInquiry_admin_success() {
            // given
            Long inquiryId = 1L;
            Long adminId = 1L;
            Long inquiryWriterId = 2L;

            User admin = mock(User.class);
            when(admin.getId()).thenReturn(adminId);
            when(admin.getRole()).thenReturn(Role.ADMIN);

            User inquiryWriter = mock(User.class);
            when(inquiryWriter.getId()).thenReturn(inquiryWriterId);

            Inquiry inquiry = mock(Inquiry.class);
            when(inquiry.getUser()).thenReturn(inquiryWriter);

            Comment comment1 = Comment.builder()
                .id(1L)
                .content("문의 댓글 1")
                .inquiry(inquiry)
                .writer(admin)
                .build();

            Comment comment2 = Comment.builder()
                .id(2L)
                .content("문의 댓글 2")
                .inquiry(inquiry)
                .writer(inquiryWriter)
                .build();

            List<Comment> comments = Arrays.asList(comment1, comment2);

            when(userService.getUserById(adminId)).thenReturn(admin);
            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));
            when(commentRepository.findAllByInquiryId(inquiryId)).thenReturn(comments);

            // when
            List<CommentResponse> responses = commentQueryService.findCommentsByInquiry(inquiryId,
                adminId);

            // then
            assertThat(responses).isNotNull();
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).commentId()).isEqualTo(1L);
            assertThat(responses.get(0).content()).isEqualTo("문의 댓글 1");
            assertThat(responses.get(1).commentId()).isEqualTo(2L);
            assertThat(responses.get(1).content()).isEqualTo("문의 댓글 2");
        }

        @Test
        @DisplayName("문의 작성자가 자신의 문의 댓글 조회 성공")
        void findCommentsByInquiry_writer_success() {
            // given
            Long inquiryId = 1L;
            Long writerId = 1L;

            User writer = mock(User.class);
            when(writer.getId()).thenReturn(writerId);
            when(writer.getRole()).thenReturn(Role.USER);

            Inquiry inquiry = mock(Inquiry.class);
            when(inquiry.getUser()).thenReturn(writer);

            Comment comment = Comment.builder()
                .id(1L)
                .content("문의 댓글")
                .inquiry(inquiry)
                .writer(writer)
                .build();

            List<Comment> comments = List.of(comment);

            when(userService.getUserById(writerId)).thenReturn(writer);
            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));
            when(commentRepository.findAllByInquiryId(inquiryId)).thenReturn(comments);

            // when
            List<CommentResponse> responses = commentQueryService.findCommentsByInquiry(inquiryId,
                writerId);

            // then
            assertThat(responses).isNotNull();
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).commentId()).isEqualTo(1L);
            assertThat(responses.get(0).content()).isEqualTo("문의 댓글");
        }

        @Test
        @DisplayName("권한 없는 사용자가 문의 댓글 조회 실패")
        void findCommentsByInquiry_forbidden_fail() {
            // given
            Long inquiryId = 1L;
            Long unauthorizedUserId = 2L;
            Long inquiryWriterId = 1L;

            User unauthorizedUser = mock(User.class);
            when(unauthorizedUser.getId()).thenReturn(unauthorizedUserId);
            when(unauthorizedUser.getRole()).thenReturn(Role.USER);

            User inquiryWriter = mock(User.class);
            when(inquiryWriter.getId()).thenReturn(inquiryWriterId);

            Inquiry inquiry = mock(Inquiry.class);
            when(inquiry.getUser()).thenReturn(inquiryWriter);

            when(userService.getUserById(unauthorizedUserId)).thenReturn(unauthorizedUser);
            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));

            // when & then
            assertThatThrownBy(
                () -> commentQueryService.findCommentsByInquiry(inquiryId, unauthorizedUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACCESS);
        }

        @Test
        @DisplayName("존재하지 않는 문의의 댓글 조회 실패")
        void findCommentsByInquiry_inquiryNotFound_fail() {
            // given
            Long inquiryId = 1L;
            Long userId = 1L;

            User user = mock(User.class);

            when(userService.getUserById(userId)).thenReturn(user);
            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentQueryService.findCommentsByInquiry(inquiryId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FIND_INQUIRY);
        }

        @Test
        @DisplayName("문의별 댓글이 없는 경우 빈 목록 반환")
        void findCommentsByInquiry_emptyList() {
            // given
            Long inquiryId = 1L;
            Long writerId = 1L;

            User writer = mock(User.class);
            when(writer.getId()).thenReturn(writerId);
            when(writer.getRole()).thenReturn(Role.USER);

            Inquiry inquiry = mock(Inquiry.class);
            when(inquiry.getUser()).thenReturn(writer);

            when(userService.getUserById(writerId)).thenReturn(writer);
            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));
            when(commentRepository.findAllByInquiryId(inquiryId)).thenReturn(List.of());

            // when
            List<CommentResponse> responses = commentQueryService.findCommentsByInquiry(inquiryId,
                writerId);

            // then
            assertThat(responses).isNotNull();
            assertThat(responses).isEmpty();
        }
    }
}

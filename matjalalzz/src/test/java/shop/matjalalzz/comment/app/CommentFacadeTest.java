package shop.matjalalzz.comment.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import shop.matjalalzz.comment.dto.CommentCreateRequest;
import shop.matjalalzz.comment.dto.CommentResponse;
import shop.matjalalzz.comment.entity.Comment;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.inquiry.dao.InquiryRepository;
import shop.matjalalzz.inquiry.entity.Inquiry;
import shop.matjalalzz.party.app.PartyService;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Role;

@ExtendWith(MockitoExtension.class)
class CommentFacadeTest {

    @Mock
    private UserService userService;

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private PartyService partyService;

    @Mock
    private CommentQueryService commentQueryService;

    @Mock
    private CommentCommandService commentCommandService;

    @InjectMocks
    private CommentFacade commentFacade;

    @Nested
    @DisplayName("모임 댓글 생성 테스트")
    class CreateCommentTest {

        @Test
        @DisplayName("모임 댓글 생성 성공")
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
            when(writer.getNickname()).thenReturn("작성자");

            Comment comment = Comment.builder()
                .id(commentId)
                .content(request.content())
                .party(party)
                .writer(writer)
                .build();

            when(partyService.findById(partyId)).thenReturn(party);
            when(userService.getUserById(writerId)).thenReturn(writer);
            when(commentCommandService.save(any(Comment.class))).thenReturn(comment);

            // when
            CommentResponse response = commentFacade.createComment(request, partyId, writerId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.commentId()).isEqualTo(commentId);
            assertThat(response.content()).isEqualTo(request.content());
            assertThat(response.writer().userId()).isEqualTo(writerId);
            assertThat(response.writer().nickname()).isEqualTo("작성자");

            verify(commentCommandService).save(any(Comment.class));
        }

        @Test
        @DisplayName("모임 대댓글 생성 성공")
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
            when(writer.getNickname()).thenReturn("작성자");

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

            when(partyService.findById(partyId)).thenReturn(party);
            when(userService.getUserById(writerId)).thenReturn(writer);
            when(commentQueryService.getComment(parentId)).thenReturn(parentComment);
            when(commentCommandService.save(any(Comment.class))).thenReturn(childComment);

            // when
            CommentResponse response = commentFacade.createComment(request, partyId, writerId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.commentId()).isEqualTo(commentId);
            assertThat(response.parentId()).isEqualTo(parentId);
            assertThat(response.content()).isEqualTo(request.content());
            assertThat(response.writer().userId()).isEqualTo(writerId);

            verify(commentCommandService).save(any(Comment.class));
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

            when(partyService.findById(partyId)).thenThrow(
                new BusinessException(ErrorCode.DATA_NOT_FOUND));

            // when & then
            assertThatThrownBy(
                () -> commentFacade.createComment(request, partyId, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND);

            verify(commentCommandService, never()).save(any(Comment.class));
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

            when(partyService.findById(partyId)).thenReturn(party);
            when(userService.getUserById(writerId)).thenThrow(
                new BusinessException(ErrorCode.DATA_NOT_FOUND));

            // when & then
            assertThatThrownBy(
                () -> commentFacade.createComment(request, partyId, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND);

            verify(commentCommandService, never()).save(any(Comment.class));
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

            when(partyService.findById(partyId)).thenReturn(party);
            when(userService.getUserById(writerId)).thenReturn(writer);
            when(commentQueryService.getComment(parentId)).thenThrow(
                new BusinessException(ErrorCode.DATA_NOT_FOUND));

            // when & then
            assertThatThrownBy(
                () -> commentFacade.createComment(request, partyId, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND);

            verify(commentCommandService, never()).save(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("문의 댓글 생성 테스트")
    class CreateInquiryCommentTest {

        @Test
        @DisplayName("관리자가 문의 댓글 생성 성공")
        void createInquiryComment_admin_success() {
            // given
            Long inquiryId = 1L;
            Long adminId = 1L;
            Long commentId = 1L;

            CommentCreateRequest request = CommentCreateRequest.builder()
                .content("관리자 답변")
                .build();

            User inquiryWriter = mock(User.class);
            when(inquiryWriter.getId()).thenReturn(2L);

            User admin = mock(User.class);
            when(admin.getId()).thenReturn(adminId);
            when(admin.getRole()).thenReturn(Role.ADMIN);
            when(admin.getNickname()).thenReturn("관리자");

            Inquiry inquiry = mock(Inquiry.class);
            when(inquiry.getUser()).thenReturn(inquiryWriter);

            Comment comment = Comment.builder()
                .id(commentId)
                .content(request.content())
                .inquiry(inquiry)
                .writer(admin)
                .build();

            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));
            when(userService.getUserById(adminId)).thenReturn(admin);
            when(commentCommandService.save(any(Comment.class))).thenReturn(comment);

            // when
            CommentResponse response = commentFacade.createInquiryComment(request, inquiryId,
                adminId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.commentId()).isEqualTo(commentId);
            assertThat(response.content()).isEqualTo(request.content());
            assertThat(response.writer().userId()).isEqualTo(adminId);
            assertThat(response.writer().nickname()).isEqualTo("관리자");

            verify(commentCommandService).save(any(Comment.class));
        }

        @Test
        @DisplayName("문의 작성자가 자신의 문의에 댓글 생성 성공")
        void createInquiryComment_writer_success() {
            // given
            Long inquiryId = 1L;
            Long writerId = 1L;
            Long commentId = 1L;

            CommentCreateRequest request = CommentCreateRequest.builder()
                .content("작성자 추가 댓글")
                .build();

            User writer = mock(User.class);
            when(writer.getId()).thenReturn(writerId);
            when(writer.getRole()).thenReturn(Role.USER);
            when(writer.getNickname()).thenReturn("작성자");

            Inquiry inquiry = mock(Inquiry.class);
            when(inquiry.getUser()).thenReturn(writer);

            Comment comment = Comment.builder()
                .id(commentId)
                .content(request.content())
                .inquiry(inquiry)
                .writer(writer)
                .build();

            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));
            when(userService.getUserById(writerId)).thenReturn(writer);
            when(commentCommandService.save(any(Comment.class))).thenReturn(comment);

            // when
            CommentResponse response = commentFacade.createInquiryComment(request, inquiryId,
                writerId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.commentId()).isEqualTo(commentId);
            assertThat(response.content()).isEqualTo(request.content());
            assertThat(response.writer().userId()).isEqualTo(writerId);

            verify(commentCommandService).save(any(Comment.class));
        }

        @Test
        @DisplayName("권한 없는 사용자가 문의 댓글 생성 실패")
        void createInquiryComment_forbidden_fail() {
            // given
            Long inquiryId = 1L;
            Long unauthorizedUserId = 2L;

            CommentCreateRequest request = CommentCreateRequest.builder()
                .content("권한 없는 댓글")
                .build();

            User inquiryWriter = mock(User.class);
            when(inquiryWriter.getId()).thenReturn(1L);

            User unauthorizedUser = mock(User.class);
            when(unauthorizedUser.getId()).thenReturn(unauthorizedUserId);
            when(unauthorizedUser.getRole()).thenReturn(Role.USER);

            Inquiry inquiry = mock(Inquiry.class);
            when(inquiry.getUser()).thenReturn(inquiryWriter);

            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));
            when(userService.getUserById(unauthorizedUserId)).thenReturn(unauthorizedUser);

            // when & then
            assertThatThrownBy(() -> commentFacade.createInquiryComment(request, inquiryId,
                unauthorizedUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACCESS);

            verify(commentCommandService, never()).save(any(Comment.class));
        }

        @Test
        @DisplayName("존재하지 않는 문의에 댓글 생성 실패")
        void createInquiryComment_inquiryNotFound_fail() {
            // given
            Long inquiryId = 1L;
            Long userId = 1L;

            CommentCreateRequest request = CommentCreateRequest.builder()
                .content("댓글")
                .build();

            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                () -> commentFacade.createInquiryComment(request, inquiryId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FIND_INQUIRY);

            verify(commentCommandService, never()).save(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("댓글 삭제 테스트")
    class DeleteCommentTest {

        @Test
        @DisplayName("댓글 삭제 성공")
        void deleteComment_success() {
            // given
            Long commentId = 1L;
            Long writerId = 1L;

            Party party = mock(Party.class);
            User writer = mock(User.class);
            when(writer.getId()).thenReturn(writerId);

            Comment comment = Comment.builder()
                .id(commentId)
                .content("테스트 댓글")
                .party(party)
                .writer(writer)
                .children(new ArrayList<>())
                .build();

            when(commentQueryService.getComment(commentId)).thenReturn(comment);

            // when
            commentFacade.deleteComment(commentId, writerId);

            // then
            verify(commentQueryService).getComment(commentId);
            verify(commentCommandService).delete(comment);
        }

        @Test
        @DisplayName("존재하지 않는 댓글 삭제 실패")
        void deleteComment_notFound_fail() {
            // given
            Long commentId = 1L;
            Long writerId = 1L;

            when(commentQueryService.getComment(commentId)).thenThrow(
                new BusinessException(ErrorCode.DATA_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> commentFacade.deleteComment(commentId, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND);

            verify(commentCommandService, never()).delete(any(Comment.class));
        }

        @Test
        @DisplayName("작성자가 아닌 사용자가 댓글 삭제 시도 실패")
        void deleteComment_notWriter_fail() {
            // given
            Long commentId = 1L;
            Long writerId = 1L;
            Long unauthorizedUserId = 2L;

            Party party = mock(Party.class);
            User writer = mock(User.class);
            when(writer.getId()).thenReturn(writerId);

            Comment comment = Comment.builder()
                .id(commentId)
                .content("테스트 댓글")
                .party(party)
                .writer(writer)
                .build();

            when(commentQueryService.getComment(commentId)).thenReturn(comment);

            // when & then
            assertThatThrownBy(
                () -> commentFacade.deleteComment(commentId, unauthorizedUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACCESS);

            verify(commentCommandService, never()).delete(any(Comment.class));
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

            when(commentQueryService.getComment(commentId)).thenReturn(comment);

            // when
            CommentResponse response = commentFacade.findComment(commentId);

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

            when(commentQueryService.getComment(commentId)).thenThrow(
                new BusinessException(ErrorCode.DATA_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> commentFacade.findComment(commentId))
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
            when(writer.getNickname()).thenReturn("작성자");

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

            when(commentQueryService.findCommentsByParty(partyId)).thenReturn(comments);

            // when
            List<CommentResponse> responses = commentFacade.findCommentsByParty(partyId);

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

            when(commentQueryService.findCommentsByParty(partyId)).thenReturn(List.of());

            // when
            List<CommentResponse> responses = commentFacade.findCommentsByParty(partyId);

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
            when(admin.getNickname()).thenReturn("관리자");

            User inquiryWriter = mock(User.class);
            when(inquiryWriter.getId()).thenReturn(inquiryWriterId);
            when(inquiryWriter.getNickname()).thenReturn("문의작성자");

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
            when(commentQueryService.findCommentsByInquiry(inquiryId)).thenReturn(comments);

            // when
            List<CommentResponse> responses = commentFacade.findCommentsByInquiry(inquiryId,
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
            when(writer.getNickname()).thenReturn("작성자");

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
            when(commentQueryService.findCommentsByInquiry(inquiryId)).thenReturn(comments);

            // when
            List<CommentResponse> responses = commentFacade.findCommentsByInquiry(inquiryId,
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
                () -> commentFacade.findCommentsByInquiry(inquiryId, unauthorizedUserId))
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
            assertThatThrownBy(() -> commentFacade.findCommentsByInquiry(inquiryId, userId))
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
            when(commentQueryService.findCommentsByInquiry(inquiryId)).thenReturn(List.of());

            // when
            List<CommentResponse> responses = commentFacade.findCommentsByInquiry(inquiryId,
                writerId);

            // then
            assertThat(responses).isNotNull();
            assertThat(responses).isEmpty();
        }
    }
}

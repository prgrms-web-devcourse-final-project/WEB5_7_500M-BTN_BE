package shop.matjalalzz.comment.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import shop.matjalalzz.inquiry.dao.InquiryRepository;
import shop.matjalalzz.inquiry.entity.Inquiry;
import shop.matjalalzz.party.app.PartyService;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Role;

@ExtendWith(MockitoExtension.class)
class CommentCommandServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PartyService partyService;

    @Mock
    private UserService userService;

    @Mock
    private InquiryRepository inquiryRepository;

    @InjectMocks
    private CommentCommandService commentCommandService;

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

            when(partyService.findById(partyId)).thenReturn(party);
            when(userService.getUserById(writerId)).thenReturn(writer);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);

            // when
            CommentResponse response = commentCommandService.createComment(request, partyId,
                writerId);

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

            when(partyService.findById(partyId)).thenReturn(party);
            when(userService.getUserById(writerId)).thenReturn(writer);
            when(commentRepository.findById(parentId)).thenReturn(Optional.of(parentComment));
            when(commentRepository.save(any(Comment.class))).thenReturn(childComment);

            // when
            CommentResponse response = commentCommandService.createComment(request, partyId,
                writerId);

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

            when(partyService.findById(partyId)).thenThrow(
                new BusinessException(ErrorCode.DATA_NOT_FOUND));

            // when & then
            assertThatThrownBy(
                () -> commentCommandService.createComment(request, partyId, writerId))
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

            when(partyService.findById(partyId)).thenReturn(party);
            when(userService.getUserById(writerId)).thenThrow(
                new BusinessException(ErrorCode.DATA_NOT_FOUND));

            // when & then
            assertThatThrownBy(
                () -> commentCommandService.createComment(request, partyId, writerId))
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

            when(partyService.findById(partyId)).thenReturn(party);
            when(userService.getUserById(writerId)).thenReturn(writer);
            when(commentRepository.findById(parentId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                () -> commentCommandService.createComment(request, partyId, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND);

            verify(commentRepository, never()).save(any(Comment.class));
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
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);

            // when
            CommentResponse response = commentCommandService.createInquiryComment(request,
                inquiryId, adminId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.commentId()).isEqualTo(commentId);
            assertThat(response.content()).isEqualTo(request.content());
            assertThat(response.writer().userId()).isEqualTo(adminId);

            verify(commentRepository).save(any(Comment.class));
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
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);

            // when
            CommentResponse response = commentCommandService.createInquiryComment(request,
                inquiryId, writerId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.commentId()).isEqualTo(commentId);
            assertThat(response.content()).isEqualTo(request.content());
            assertThat(response.writer().userId()).isEqualTo(writerId);

            verify(commentRepository).save(any(Comment.class));
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
            assertThatThrownBy(() -> commentCommandService.createInquiryComment(request, inquiryId,
                unauthorizedUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACCESS);

            verify(commentRepository, never()).save(any(Comment.class));
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
                () -> commentCommandService.createInquiryComment(request, inquiryId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FIND_INQUIRY);

            verify(commentRepository, never()).save(any(Comment.class));
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

            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // when
            commentCommandService.deleteComment(commentId, writerId);

            // then
            verify(commentRepository).findById(commentId);
        }

        @Test
        @DisplayName("대댓글이 있는 댓글 삭제 성공")
        void deleteCommentWithChildren_success() {
            // given
            Long commentId = 1L;
            Long childId = 2L;
            Long writerId = 1L;

            Party party = mock(Party.class);
            User writer = mock(User.class);
            when(writer.getId()).thenReturn(writerId);

            Comment childComment = Comment.builder()
                .id(childId)
                .content("자식 댓글")
                .party(party)
                .writer(writer)
                .build();

            Comment parentComment = Comment.builder()
                .id(commentId)
                .content("부모 댓글")
                .party(party)
                .writer(writer)
                .children(List.of(childComment))
                .build();

            when(commentRepository.findById(commentId)).thenReturn(Optional.of(parentComment));

            // when
            commentCommandService.deleteComment(commentId, writerId);

            // then
            verify(commentRepository).findById(commentId);
        }

        @Test
        @DisplayName("존재하지 않는 댓글 삭제 실패")
        void deleteComment_notFound_fail() {
            // given
            Long commentId = 1L;
            Long writerId = 1L;

            when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentCommandService.deleteComment(commentId, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND);
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

            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(
                () -> commentCommandService.deleteComment(commentId, unauthorizedUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACCESS);
        }
    }
}

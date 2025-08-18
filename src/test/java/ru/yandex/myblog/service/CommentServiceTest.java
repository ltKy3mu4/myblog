package ru.yandex.myblog.service;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.myblog.model.domain.Comment;
import ru.yandex.myblog.model.exception.CommentNotFoundException;
import ru.yandex.myblog.repository.JdbcCommentRepository;

import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private JdbcCommentRepository commentRepo;

    @Mock
    private Logger log;

    @InjectMocks
    private CommentService commentService;

    @Test
    void save_ShouldCallRepositorySave() {
        long postId = 1L;
        String commentText = "Test comment";

        commentService.save(postId, commentText);

        verify(commentRepo).save(argThat(comment -> comment.getPostId() == postId && comment.getText().equals(commentText)));
    }

    @Test
    void update_ShouldCallRepositoryUpdate_WhenCommentExists() {
        long commentId = 1L;
        long postId = 1L;
        String commentText = "Updated comment";
        Comment comment = new Comment(commentId, postId, commentText);

        when(commentRepo.update(comment)).thenReturn(true);

        commentService.update(commentId, postId, commentText);

        verify(commentRepo).update(comment);
    }

    @Test
    void update_ShouldLogError_WhenCommentDoesNotExist() {
        long commentId = 1L;
        long postId = 1L;
        String commentText = "Updated comment";
        Comment comment = new Comment(commentId, postId, commentText);

        when(commentRepo.update(comment)).thenReturn(false);

        Assertions.assertThrows(CommentNotFoundException.class, () ->  commentService.update(commentId, postId, commentText));
    }

    @Test
    void delete_ShouldCallRepositoryDelete_WhenCommentExists() {
        long commentId = 1L;
        long postId = 1L;

        when(commentRepo.deleteById(commentId, postId)).thenReturn(true);

        commentService.delete(commentId, postId);

        verify(commentRepo).deleteById(commentId, postId);
    }

    @Test
    void delete_ShouldLogError_WhenCommentDoesNotExist() {
        long commentId = 1L;
        long postId = 1L;

        when(commentRepo.deleteById(commentId, postId)).thenReturn(false);

        Assertions.assertThrows(CommentNotFoundException.class, () ->  commentService.delete(commentId, postId));
    }
}
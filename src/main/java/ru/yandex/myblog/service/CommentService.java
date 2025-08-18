package ru.yandex.myblog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.myblog.model.domain.Comment;
import ru.yandex.myblog.model.exception.CommentNotFoundException;
import ru.yandex.myblog.repository.JdbcCommentRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final JdbcCommentRepository commentRepo;

    public void save(long postId,String commentText ) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setText(commentText);
        commentRepo.save(comment);
    }

    public void update(long commentId, long postId, String commentText ) {
        Comment comment = new Comment(commentId, postId, commentText);
        boolean update = commentRepo.update(comment);
        if (!update) {
            log.error("Comment update failed, cause comment {} for post {} was not found", commentId, postId);
            throw new CommentNotFoundException("Comment cannot be updated");
        }
    }

    public void delete(long commentId, long postId) {
        boolean res = commentRepo.deleteById(commentId, postId);
        if (!res) {
            log.error("Comment delete failed, cause comment {} was not found", commentId);
            throw new CommentNotFoundException("Comment cannot be deleted");
        }
    }

}

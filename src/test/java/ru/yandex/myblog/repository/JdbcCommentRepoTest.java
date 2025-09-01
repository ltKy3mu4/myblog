package ru.yandex.myblog.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.myblog.model.domain.Comment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {JdbcCommentRepository.class})
class JdbcCommentRepoTest extends PostgresBaseIntegrationTest {

    @Autowired
    private JdbcCommentRepository repo;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM comments");
        jdbcTemplate.update("ALTER SEQUENCE comments_id_seq RESTART WITH 1;");

        jdbcTemplate.update("DELETE FROM posts");
        jdbcTemplate.update("ALTER SEQUENCE posts_id_seq RESTART WITH 1;");

        jdbcTemplate.update("INSERT INTO posts(title, text, likesCount) VALUES ('Post 1', 'Content 1', 10)");
        jdbcTemplate.update("INSERT INTO posts(title, text, likesCount) VALUES ('Post 2', 'Content 2', 2)");

        jdbcTemplate.update("INSERT INTO comments(post_id, text) VALUES (1, 'First comment')");
        jdbcTemplate.update("INSERT INTO comments(post_id, text) VALUES (1, 'Second comment')");
        jdbcTemplate.update("INSERT INTO comments(post_id, text) VALUES (2, 'Comment for post 2')");
    }

    @Test
    void save_ShouldInsertNewComment() {
        Comment newComment = new Comment(0L, 1L, "New test comment");
        repo.save(newComment);
        List<Comment> comments = jdbcTemplate.query(
                "SELECT id, post_id, text FROM comments WHERE post_id = 1 AND text = 'New test comment'",
                (rs, rowNum) -> new Comment(
                        rs.getLong("id"),
                        rs.getLong("post_id"),
                        rs.getString("text")
                )
        );
        assertEquals(1, comments.size());
        assertEquals("New test comment", comments.get(0).getText());
    }

    @Test
    void update_ShouldModifyExistingComment() {
        Comment updatedComment = new Comment(1L, 1L, "Updated comment text");
        boolean result = repo.update(updatedComment);
        assertTrue(result);
        String text = jdbcTemplate.queryForObject(
                "SELECT text FROM comments WHERE id = 1",
                String.class
        );
        assertEquals("Updated comment text", text);
    }

    @Test
    void update_ShouldReturnFalse_WhenCommentNotFound() {
        Comment nonExistentComment = new Comment(999L, 1L, "Non-existent comment");
        boolean result = repo.update(nonExistentComment);
        assertFalse(result);
    }

    @Test
    void deleteById_ShouldRemoveComment_WhenExists() {
        long commentId = 1L;
        long postId = 1L;

        Integer initialCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM comments WHERE id = 1",
                Integer.class
        );
        assertEquals(1, initialCount);

        boolean result = repo.deleteById(commentId, postId);

        assertTrue(result);
        Integer finalCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM comments WHERE id = 1",
                Integer.class
        );
        assertEquals(0, finalCount);
    }

    @Test
    void deleteById_ShouldReturnFalse_WhenCommentNotFound() {
        long nonExistentCommentId = 999L;
        long postId = 1L;

        boolean result = repo.deleteById(nonExistentCommentId, postId);

        assertFalse(result);
    }

    @Test
    void deleteAllForPost_ShouldRemoveAllCommentsForPost() {
        long postId = 1L;

        Integer initialCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM comments WHERE post_id = 1",
                Integer.class
        );
        assertEquals(2, initialCount);

        int deletedCount = repo.deleteAllForPost(postId);

        assertEquals(2, deletedCount);
        Integer finalCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM comments WHERE post_id = 1",
                Integer.class
        );
        assertEquals(0, finalCount);
    }

    @Test
    void getAllForPost_ShouldReturnAllCommentsForPost() {
        long postId = 1L;

        List<Comment> comments = repo.getAllForPost(postId);

        assertEquals(2, comments.size());
        assertTrue(comments.stream().anyMatch(c -> c.getText().equals("First comment")));
        assertTrue(comments.stream().anyMatch(c -> c.getText().equals("Second comment")));
    }

    @Test
    void getAllForPost_ShouldReturnEmptyList_WhenNoCommentsExist() {
        long postIdWithNoComments = 3L;
        List<Comment> comments = repo.getAllForPost(postIdWithNoComments);
        assertTrue(comments.isEmpty());
    }
}
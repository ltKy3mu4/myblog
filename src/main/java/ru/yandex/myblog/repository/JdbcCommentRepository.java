package ru.yandex.myblog.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.myblog.model.domain.Comment;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JdbcCommentRepository {

    private final JdbcTemplate jdbcTemplate;

    public void save(Comment c) {
        jdbcTemplate.update(
                "insert into comments(post_id, text) values(?, ?)",
                c.getPostId(), c.getText()
        );
    }

    public boolean update(Comment c) {
        int res = jdbcTemplate.update(
                "update comments set text = ? where id = ? and post_id = ?",
                c.getText(), c.getId(), c.getPostId()
        );
        return res > 0;
    }


    public boolean deleteById(Long id, Long postId) {
        int res = jdbcTemplate.update("delete from comments where id = ? and post_id = ?", id, postId);
        return res > 0;
    }
    public int deleteAllForPost(Long postId) {
        return jdbcTemplate.update("delete from comments where post_id = ?", postId);
    }

    public List<Comment> getAllForPost(long postId) {
        return jdbcTemplate.query(
                "select id, post_id, text from comments where post_id = ?",
                (rs, rowNum) -> new Comment(
                        rs.getLong("id"),
                        rs.getLong("post_id"),
                        rs.getString("text")
                ),
                postId);
    }

}

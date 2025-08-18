package ru.yandex.myblog.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.myblog.model.domain.Tag;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class JdbcTagRepository {


    private final JdbcTemplate jdbcTemplate;

    public List<Tag> getTagsForPost(long postId) {

        List<Tag> tags = jdbcTemplate.query(
                "select id, name from tags where id in (select tag_id from posts_tags where post_id = ?)",
                (rs, rowNum) -> new Tag(rs.getLong("id"), rs.getString("name")),
                postId);

        return tags;
    }

    public void saveBatch(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate("INSERT INTO tags (name) VALUES (?) ON CONFLICT DO NOTHING",
                tags,
                tags.size(),
                (ps, argument) -> ps.setString(1, argument.getName())
        );
    }

    public void bindTagsToPost(long postId, List<Tag> tags) {
        jdbcTemplate.batchUpdate("INSERT INTO posts_tags (post_id, tag_id) values (?,?)",
                tags,
                tags.size(),
                (ps, argument) -> {
                    ps.setLong(1, postId);
                    ps.setLong(2, argument.getId());
                });
    }

    public List<Tag> getTagsByNames(List<String> tagNames) {
        String inSql = String.join(", ", Collections.nCopies(tagNames.size(), "?"));


        List<Tag> query = jdbcTemplate.query(
                "select id, name from tags where name in (" + inSql + ")",
                (rs, rowNum) -> new Tag(rs.getLong("id"), rs.getString("name")),
                tagNames.toArray()
        );
        return query;
    }

    public int deleteTagToPostConnections(long postId) {
        return jdbcTemplate.update("delete from posts_tags where post_id = ?", postId);
    }
}

package ru.yandex.myblog.repository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.myblog.model.domain.Post;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcPostRepository {


    private final JdbcTemplate jdbcTemplate;

    public List<Post> getPosts(int offset, int limit) {
        return jdbcTemplate.query(
                "select id, title, text, likesCount from posts order by id desc limit ? offset ?",
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("likesCount")
                ),
                limit, offset);
    }

    public List<Post> getPostsByTag(@NonNull String tag, int offset, int limit) {
        return jdbcTemplate.query(
                """
                        select p.id, p.title, p.text, p.likesCount from posts p
                        left join posts_tags pt on p.id = pt.post_id
                        left join tags t on pt.tag_id = t.id
                        where t.name = ?
                        group by p.id
                        order by p.id desc limit ? offset ?
                        """,
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("likesCount")
                ),
                tag, limit, offset);
    }

    public Integer getTotalPostsCount(){
        return jdbcTemplate.queryForObject("select count(*) from posts", Integer.class);
    }

    public Integer getTotalPostsCount(String tag) {
        return jdbcTemplate.queryForObject(
                """
                    select count(distinct p.id) from posts p
                    left join posts_tags pt on p.id = pt.post_id
                    left join tags t on pt.tag_id = t.id
                    where t.name = ?
                    """,
                Integer.class,
                tag);
    }



    public Optional<Post> findById(Long id) {
        List<Post> posts = jdbcTemplate.query(
                "select id, title, text, likesCount from posts where id = ?",
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("likesCount")
                ),
                id);
        return posts.stream().findFirst();
    }

    public void likePost(Long id) {
        jdbcTemplate.update("update posts set likesCount = likescount + 1 where id = ?", id);
    }

    public void unlikePost(Long id) {
        jdbcTemplate.update("update posts set likesCount = likescount - 1 where id = ? and likesCount > 0", id);
    }

    public long save(Post p) {
        Long postId = jdbcTemplate.queryForObject("insert into posts(title, text, likesCount) values(?, ?, ?) returning id",
                Long.class,
                p.getTitle(), p.getText(), p.getLikesCount());

        if (postId == null) {
            throw new IllegalArgumentException("Error while saving post");
        }
        return postId;
    }

    public void update(@NonNull Post post) {
        jdbcTemplate.update("update posts set title = ?, text = ? where id = ?",
                post.getTitle(), post.getText(), post.getId()
        );
    }


    public int deleteById(Long id) {
        return jdbcTemplate.update("delete from posts where id = ?", id);
    }

}

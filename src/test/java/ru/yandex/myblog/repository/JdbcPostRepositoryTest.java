package ru.yandex.myblog.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.myblog.model.domain.Post;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {JdbcPostRepository.class})
class JdbcPostRepositoryTest extends PostgresBaseIntegrationTest {

    @Autowired
    JdbcPostRepository repo;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM posts_tags");
        jdbcTemplate.update("DELETE FROM tags");
        jdbcTemplate.update("ALTER SEQUENCE tags_id_seq RESTART WITH 1;;");
        jdbcTemplate.update("DELETE FROM posts");
        jdbcTemplate.update("ALTER SEQUENCE posts_id_seq RESTART WITH 1;;");

        jdbcTemplate.update("INSERT INTO posts(title, text, likesCount) VALUES ('First Post', 'Content 1', 10)");
        jdbcTemplate.update("INSERT INTO posts(title, text, likesCount) VALUES ('Second Post', 'Content 2', 5)");
        jdbcTemplate.update("INSERT INTO posts(title, text, likesCount) VALUES ('Third Post', 'Content 3', 0)");

        jdbcTemplate.update("INSERT INTO tags(name) VALUES ('tech')");
        jdbcTemplate.update("INSERT INTO tags(name) VALUES ('science')");
        jdbcTemplate.update("INSERT INTO posts_tags(post_id, tag_id) VALUES (1, 1)");
        jdbcTemplate.update("INSERT INTO posts_tags(post_id, tag_id) VALUES (2, 1)");
        jdbcTemplate.update("INSERT INTO posts_tags(post_id, tag_id) VALUES (2, 2)");
    }

    @Test
    void getPosts_shouldReturnPaginatedPosts() {
        List<Post> posts = repo.getPosts(0, 2);
        assertEquals(2, posts.size());
        assertEquals("Third Post", posts.get(0).getTitle());
        assertEquals("Second Post", posts.get(1).getTitle());

        posts = repo.getPosts(2, 2);
        assertEquals(1, posts.size());
        assertEquals("First Post", posts.get(0).getTitle());
    }

    @Test
    void getPostsByTag_shouldReturnFilteredPosts() {
        List<Post> techPosts = repo.getPostsByTag("tech", 0, 10);
        assertEquals(2, techPosts.size());
        assertTrue(techPosts.stream().anyMatch(p -> p.getTitle().equals("First Post")));
        assertTrue(techPosts.stream().anyMatch(p -> p.getTitle().equals("Second Post")));

        List<Post> sciencePosts = repo.getPostsByTag("science", 0, 10);
        assertEquals(1, sciencePosts.size());
        assertEquals("Second Post", sciencePosts.get(0).getTitle());
    }

    @Test
    void getTotalPostsCount_shouldReturnCorrectCount() {
        assertEquals(3, repo.getTotalPostsCount());

        repo.save(new Post(0L,"Fourth Post", "Content 4", 2));
        assertEquals(4, repo.getTotalPostsCount());
    }

    @Test
    void getTotalPostsCount_withTag_shouldReturnFilteredCount() {
        assertEquals(2, repo.getTotalPostsCount("tech"));
        assertEquals(1, repo.getTotalPostsCount("science"));
        assertEquals(0, repo.getTotalPostsCount("nonexistent"));
    }

    @Test
    void findById_shouldReturnPostWhenExists() {
        Optional<Post> post = repo.findById(1L);
        assertTrue(post.isPresent());
        assertEquals("First Post", post.get().getTitle());
        assertEquals("Content 1", post.get().getText());
        assertEquals(10, post.get().getLikesCount());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        Optional<Post> post = repo.findById(999L);
        assertTrue(post.isEmpty());
    }

    @Test
    void likePost_shouldIncrementLikesCount() {
        int initialLikes = jdbcTemplate.queryForObject(
                "SELECT likesCount FROM posts WHERE id = 1", Integer.class);

        repo.likePost(1L);

        int updatedLikes = jdbcTemplate.queryForObject(
                "SELECT likesCount FROM posts WHERE id = 1", Integer.class);
        assertEquals(initialLikes + 1, updatedLikes);
    }

    @Test
    void unlikePost_shouldDecrementLikesCount() {
        int initialLikes = repo.findById(1L).get().getLikesCount();

        repo.unlikePost(1L);

        int updatedLikes = repo.findById(1L).get().getLikesCount();
        assertEquals(initialLikes - 1, updatedLikes);

        repo.unlikePost(3L);
        int zeroLikes = repo.findById(3L).get().getLikesCount();
        assertEquals(0, zeroLikes);
    }

    @Test
    void save_shouldCreateNewPostAndReturnId() {
        Post newPost = new Post(0, "New Post", "New Content", 0);
        long id = repo.save(newPost);

        assertTrue(id > 0);

        Optional<Post> savedPost = repo.findById(id);
        assertTrue(savedPost.isPresent());
        assertEquals("New Post", savedPost.get().getTitle());
        assertEquals("New Content", savedPost.get().getText());
        assertEquals(0, savedPost.get().getLikesCount());
    }

    @Test
    void update_shouldModifyExistingPost() {
        Post postToUpdate = new Post(1L, "Updated Title", "Updated Content", 10);
        repo.update(postToUpdate);

        Optional<Post> updatedPost = repo.findById(1L);
        assertTrue(updatedPost.isPresent());
        assertEquals("Updated Title", updatedPost.get().getTitle());
        assertEquals("Updated Content", updatedPost.get().getText());
        // Likes count shouldn't change
        assertEquals(10, updatedPost.get().getLikesCount());
    }
    
    @Test
    void deleteById_shouldReturnZeroWhenPostNotExists() {
        int deletedCount = repo.deleteById(999L);
        assertEquals(0, deletedCount);
    }


}
package ru.yandex.myblog.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.myblog.model.domain.ImageBlob;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {JdbcImageRepo.class})
class JdbcImageRepoTest extends PostgresBaseIntegrationTest {

    @Autowired
    private JdbcImageRepo repo;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM images");
        jdbcTemplate.update("ALTER SEQUENCE images_id_seq RESTART WITH 1;");

        jdbcTemplate.update("DELETE FROM posts");
        jdbcTemplate.update("ALTER SEQUENCE posts_id_seq RESTART WITH 1;");

        jdbcTemplate.update("INSERT INTO posts(title, text, likesCount) VALUES ('Post 1', 'Content 1', 10)");
        jdbcTemplate.update("INSERT INTO posts(title, text, likesCount) VALUES ('Post 2', 'Content 2', 2)");
        jdbcTemplate.update("INSERT INTO images( post_id, file_name, data) VALUES (1, 'test.jpg', decode('7465737420696D6167652064617461', 'hex'))");
    }

    @Test
    void save_ShouldStoreImage_WhenValidInput(){
        long postId = 2L;
        byte[] testData = "test image data".getBytes();
        MultipartFile file = new MockMultipartFile(
                "test.jpg", "test.jpg", "image/jpeg", testData
        );

        boolean result = repo.save(postId, file);

        assertTrue(result);
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM images WHERE post_id = ?",
                Integer.class,
                postId
        );
        assertEquals(1, count);
    }

    @Test
    void delete_ShouldRemoveImage_WhenImageExists() {
        long postId = 1L;

        Integer initialCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM images WHERE post_id = ?",
                Integer.class,
                postId
        );
        assertEquals(1, initialCount);

        boolean result = repo.delete(postId);

        assertTrue(result);
        Integer finalCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM images WHERE post_id = ?",
                Integer.class,
                postId
        );
        assertEquals(0, finalCount);
    }

    @Test
    void delete_ShouldReturnFalse_WhenNoImageExists() {
        long postId = 99L;
        boolean result = repo.delete(postId);
        assertFalse(result);
    }


    @Test
    void getByPostId_ShouldReturnImage_WhenImageExists() {
        long postId = 1L;

        ImageBlob result = repo.getByPostId(postId);

        assertNotNull(result);
        assertEquals(postId, result.getPost_id());
        assertEquals("test.jpg", result.getFileName());
        assertNotNull(result.getData());
    }

    @Test
    void getByPostId_ShouldThrowException_WhenNoImageExists() {
        long postId = 99L;

        assertThrows(EmptyResultDataAccessException.class, () -> repo.getByPostId(postId));
    }

}
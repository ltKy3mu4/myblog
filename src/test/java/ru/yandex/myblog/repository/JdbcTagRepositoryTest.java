package ru.yandex.myblog.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yandex.myblog.model.domain.Tag;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {JdbcTagRepository.class})
public class JdbcTagRepositoryTest extends PostgresBaseIntegrationTest{

    @Autowired
    JdbcTagRepository repo;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM posts_tags");
        jdbcTemplate.update("DELETE FROM tags");
        jdbcTemplate.update("ALTER SEQUENCE tags_id_seq RESTART WITH 1;;");
        jdbcTemplate.update("DELETE FROM posts");
        jdbcTemplate.update("ALTER SEQUENCE posts_id_seq RESTART WITH 1;;");

        jdbcTemplate.update("INSERT INTO posts(title, text, likesCount) VALUES ('Post 1', 'Content 1', 10)");
        jdbcTemplate.update("INSERT INTO posts(title, text, likesCount) VALUES ('Post 2', 'Content 2', 5)");

        jdbcTemplate.update("INSERT INTO tags(name) VALUES ('tag1')");
        jdbcTemplate.update("INSERT INTO tags(name) VALUES ('tag2')");
        jdbcTemplate.update("INSERT INTO tags(name) VALUES ('tag3')");

        jdbcTemplate.update("INSERT INTO posts_tags(post_id, tag_id) VALUES (1, 1)");
        jdbcTemplate.update("INSERT INTO posts_tags(post_id, tag_id) VALUES (1, 2)");
    }

    @Test
    void getTagsForPost_ShouldReturnTags_WhenPostHasTags() {
        long postId = 1L;

        List<Tag> result = repo.getTagsForPost(postId);

        assertEquals(2, result.size());
        assertEquals("tag1", result.get(0).getName());
        assertEquals("tag2", result.get(1).getName());
    }

    @Test
    void getTagsForPost_ShouldReturnEmptyList_WhenPostHasNoTags() {
        long postId = 99L;

        List<Tag> result = repo.getTagsForPost(postId);

        assertTrue(result.isEmpty());
    }

    @Test
    void saveBatch_ShouldInsertNewTags_WhenTagsDontExist() {
        List<Tag> newTags = Arrays.asList(new Tag("kotlin"), new Tag("java"));

        repo.saveBatch(newTags);

        List<Tag> savedTags = jdbcTemplate.query(
                "SELECT id, name FROM tags WHERE name IN ('java', 'kotlin')",
                (rs, rowNum) -> new Tag(rs.getLong("id"), rs.getString("name"))
        );
        assertEquals(2, savedTags.size());
    }

    @Test
    void saveBatch_ShouldNotFail_WhenTagsAlreadyExist() {
        List<Tag> newTags = Arrays.asList(new Tag("tag1"));
        repo.saveBatch(newTags);

        int javaTagCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tags WHERE name = 'tag1'",
                Integer.class
        );

        assertEquals(1, javaTagCount);
    }

    @Test
    void bindTagsToPost_ShouldCreateRelationships() {
        long postId = 2L;
        List<Tag> tags = Arrays.asList(new Tag(1L, "tag1"), new Tag(3L, "newtag"));
        repo.saveBatch(tags);
        repo.bindTagsToPost(postId, tags);

        int relationshipCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM posts_tags WHERE post_id = ?",
                Integer.class,
                postId
        );
        assertEquals(2, relationshipCount);
    }

    @Test
    void getTagsByNames_ShouldReturnMatchingTags() {
        List<String> tagNames = Arrays.asList("tag1", "tag2", "nonexistent");

        List<Tag> result = repo.getTagsByNames(tagNames);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(t -> t.getName().equals("tag1")));
        assertTrue(result.stream().anyMatch(t -> t.getName().equals("tag2")));
    }

    @Test
    void deleteTagToPostConnections_ShouldRemoveRelationships() {
        long postId = 1L;

        int initialCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM posts_tags WHERE post_id = ?",
                Integer.class,
                postId
        );
        assertEquals(2, initialCount);

        int deletedCount = repo.deleteTagToPostConnections(postId);

        assertEquals(2, deletedCount);
        int remainingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM posts_tags WHERE post_id = ?",
                Integer.class,
                postId
        );
        assertEquals(0, remainingCount);
    }
}
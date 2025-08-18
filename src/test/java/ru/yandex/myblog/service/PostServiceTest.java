package ru.yandex.myblog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.myblog.model.domain.Comment;
import ru.yandex.myblog.model.domain.ImageBlob;
import ru.yandex.myblog.model.domain.Post;
import ru.yandex.myblog.model.domain.Tag;
import ru.yandex.myblog.model.exception.PostNotFoundException;
import ru.yandex.myblog.repository.JdbcCommentRepository;
import ru.yandex.myblog.repository.JdbcImageRepo;
import ru.yandex.myblog.repository.JdbcPostRepository;
import ru.yandex.myblog.repository.JdbcTagRepository;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private JdbcPostRepository postRepo;
    @Mock
    private JdbcTagRepository tagRepo;
    @Mock
    private JdbcCommentRepository commentsRepo;
    @Mock
    private JdbcImageRepo imageRepo;

    @InjectMocks
    private PostService postService;

    private final Post testPost = new Post(1L, "Test Post", "Test Content", 10);
    private final Tag testTag = new Tag(1L, "test-tag");
    private final Comment testComment = new Comment(1L, 1L, "Test comment");
    private final ImageBlob testImage = new ImageBlob(1L, 1L, "image/jpeg", new byte[]{});
    private MultipartFile file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", new byte[]{});

    @Test
    void createPost_ShouldCreatePostWithAllRelatedEntities() {
        String title = "Test Post";
        String text = "Test Content";
        String tagsStr = "tag1 tag2";
        long postId = 1L;

        List<Tag> tags = Arrays.asList(new Tag("tag1"), new Tag("tag2"));
        List<Tag> savedTags = Arrays.asList(new Tag(1L, "tag1"), new Tag(2L, "tag2"));

        when(postRepo.save(any(Post.class))).thenReturn(postId);
        when(tagRepo.getTagsByNames(anyList())).thenReturn(savedTags);

        Post result = postService.createPost(title, text, file, tagsStr);

        assertNotNull(result);
        assertEquals(postId, result.getId());
        assertEquals(title, result.getTitle());
        assertEquals(text, result.getText());
        assertEquals(0, result.getLikesCount());
        assertEquals(2, result.getTags().size());

        verify(postRepo).save(any(Post.class));
        verify(imageRepo).save(postId, file);
        verify(tagRepo).saveBatch(tags);
        verify(tagRepo).getTagsByNames(Arrays.asList("tag1", "tag2"));
        verify(tagRepo).bindTagsToPost(postId, savedTags);
    }

    @Test
    void getPost_ShouldReturnPostWithAllRelatedEntities() {
        long postId = 1L;
        when(postRepo.findById(postId)).thenReturn(Optional.of(testPost));
        when(tagRepo.getTagsForPost(postId)).thenReturn(Collections.singletonList(testTag));
        when(commentsRepo.getAllForPost(postId)).thenReturn(Collections.singletonList(testComment));
        when(imageRepo.getByPostId(postId)).thenReturn(testImage);

        Post result = postService.getPost(postId);

        assertNotNull(result);
        assertEquals(postId, result.getId());
        assertEquals(1, result.getTags().size());
        assertEquals(1, result.getComments().size());
        assertEquals(testImage, result.getImage());

        verify(postRepo).findById(postId);
        verify(tagRepo).getTagsForPost(postId);
        verify(commentsRepo).getAllForPost(postId);
        verify(imageRepo).getByPostId(postId);
    }

    @Test
    void getPost_ShouldThrowExceptionWhenPostNotFound() {
        long postId = 999L;
        when(postRepo.findById(postId)).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> postService.getPost(postId));
        verify(postRepo).findById(postId);
        verifyNoMoreInteractions(tagRepo, commentsRepo, imageRepo);
    }

    @Test
    void likePost_ShouldIncreaseLikesCount() {
        long postId = 1L;
        when(postRepo.findById(postId)).thenReturn(Optional.of(testPost));
        when(tagRepo.getTagsForPost(postId)).thenReturn(Collections.emptyList());
        when(commentsRepo.getAllForPost(postId)).thenReturn(Collections.emptyList());
        when(imageRepo.getByPostId(postId)).thenReturn(null);

        Post result = postService.likePost(postId, true);

        verify(postRepo).likePost(postId);
        verify(postRepo, never()).unlikePost(anyLong());
        verify(postRepo).findById(postId);
    }

    @Test
    void likePost_ShouldDecreaseLikesCount() {
        long postId = 1L;
        when(postRepo.findById(postId)).thenReturn(Optional.of(testPost));
        when(tagRepo.getTagsForPost(postId)).thenReturn(Collections.emptyList());
        when(commentsRepo.getAllForPost(postId)).thenReturn(Collections.emptyList());
        when(imageRepo.getByPostId(postId)).thenReturn(null);

        Post result = postService.likePost(postId, false);

        verify(postRepo).unlikePost(postId);
        verify(postRepo, never()).likePost(anyLong());
        verify(postRepo).findById(postId);
    }

    @Test
    void deletePost_ShouldDeleteAllRelatedEntities() {
        long postId = 1L;
        when(commentsRepo.deleteAllForPost(postId)).thenReturn(3);
        when(tagRepo.deleteTagToPostConnections(postId)).thenReturn(2);
        when(postRepo.deleteById(postId)).thenReturn(1);

        postService.deletePost(postId);

        verify(commentsRepo).deleteAllForPost(postId);
        verify(tagRepo).deleteTagToPostConnections(postId);
        verify(imageRepo).delete(postId);
        verify(postRepo).deleteById(postId);
    }

    @Test
    void deletePost_ShouldHandlePostNotFound() {
        long postId = 999L;
        when(commentsRepo.deleteAllForPost(postId)).thenReturn(0);
        when(tagRepo.deleteTagToPostConnections(postId)).thenReturn(0);
        when(postRepo.deleteById(postId)).thenReturn(0);

        postService.deletePost(postId);

        verify(commentsRepo).deleteAllForPost(postId);
        verify(tagRepo).deleteTagToPostConnections(postId);
        verify(imageRepo).delete(postId);
        verify(postRepo).deleteById(postId);
    }
}
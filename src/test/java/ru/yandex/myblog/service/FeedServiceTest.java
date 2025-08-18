package ru.yandex.myblog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.myblog.model.domain.Comment;
import ru.yandex.myblog.model.domain.ImageBlob;
import ru.yandex.myblog.model.domain.Post;
import ru.yandex.myblog.model.domain.Tag;
import ru.yandex.myblog.model.dto.Paging;
import ru.yandex.myblog.repository.JdbcCommentRepository;
import ru.yandex.myblog.repository.JdbcImageRepo;
import ru.yandex.myblog.repository.JdbcPostRepository;
import ru.yandex.myblog.repository.JdbcTagRepository;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private JdbcPostRepository postRepo;
    @Mock
    private JdbcTagRepository tagRepo;
    @Mock
    private JdbcCommentRepository commentsRepo;
    @Mock
    private JdbcImageRepo imageRepo;

    @InjectMocks
    private FeedService feedService;

    private final Post testPost = new Post(1L, "Test Post", "Test Content", 10);
    private final Tag testTag = new Tag(1L, "test-tag");
    private final Comment testComment = new Comment(1L, 1L, "Test comment");
    private final ImageBlob testImage = new ImageBlob(1L, 1L, "image/jpeg", new byte[]{});


    @Test
    void getPosts_ShouldReturnPostsWithoutTagFilter() {
        int pageSize = 10;
        int pageNumber = 1;
        int offset = 0;
        String searchedTag = "";

        when(postRepo.getPosts(offset, pageSize)).thenReturn(Collections.singletonList(testPost));
        when(tagRepo.getTagsForPost(testPost.getId())).thenReturn(Collections.singletonList(testTag));
        when(commentsRepo.getAllForPost(testPost.getId())).thenReturn(Collections.singletonList(testComment));
        when(imageRepo.getByPostId(testPost.getId())).thenReturn(testImage);

        List<Post> result = feedService.getPosts(searchedTag, pageSize, pageNumber);

        assertEquals(1, result.size());
        Post returnedPost = result.get(0);
        assertEquals(testPost.getId(), returnedPost.getId());
        assertEquals(1, returnedPost.getTags().size());
        assertEquals(1, returnedPost.getComments().size());
        assertEquals(testImage, returnedPost.getImage());

        verify(postRepo).getPosts(offset, pageSize);
        verify(postRepo, never()).getPostsByTag(anyString(), anyInt(), anyInt());
        verify(tagRepo).getTagsForPost(testPost.getId());
        verify(commentsRepo).getAllForPost(testPost.getId());
        verify(imageRepo).getByPostId(testPost.getId());
    }

    @Test
    void getPosts_ShouldReturnPostsWithTagFilter() {
        int pageSize = 10;
        int pageNumber = 1;
        int offset = 0;
        String searchedTag = "test-tag";

        when(postRepo.getPostsByTag(searchedTag, offset, pageSize)).thenReturn(Collections.singletonList(testPost));
        when(tagRepo.getTagsForPost(testPost.getId())).thenReturn(Collections.singletonList(testTag));
        when(commentsRepo.getAllForPost(testPost.getId())).thenReturn(Collections.singletonList(testComment));
        when(imageRepo.getByPostId(testPost.getId())).thenReturn(testImage);

        List<Post> result = feedService.getPosts(searchedTag, pageSize, pageNumber);

        assertEquals(1, result.size());
        verify(postRepo).getPostsByTag(searchedTag, offset, pageSize);
        verify(postRepo, never()).getPosts(anyInt(), anyInt());
    }

    @Test
    void getPosts_ShouldCalculateCorrectOffset() {
        int pageSize = 10;
        int pageNumber = 3;
        int expectedOffset = 20;
        String searchedTag = "";

        when(postRepo.getPosts(expectedOffset, pageSize)).thenReturn(Collections.emptyList());

        feedService.getPosts(searchedTag, pageSize, pageNumber);

        verify(postRepo).getPosts(expectedOffset, pageSize);
    }

    @Test
    void getPageInfo_ShouldReturnCorrectPagingWithoutTag() {
        int pageSize = 10;
        int pageNumber = 2;
        String tag = "";
        int totalCount = 25;

        when(postRepo.getTotalPostsCount()).thenReturn(totalCount);

        Paging result = feedService.getPageInfo(pageSize, pageNumber, tag);

        assertEquals(pageNumber, result.pageNumber());
        assertEquals(pageSize, result.pageSize());
        assertTrue(result.hasNext());
        assertTrue(result.hasPrevious());
        verify(postRepo).getTotalPostsCount();
        verify(postRepo, never()).getTotalPostsCount(anyString());
    }

    @Test
    void getPageInfo_ShouldReturnCorrectPagingWithTag() {
        int pageSize = 10;
        int pageNumber = 1;
        String tag = "test-tag";
        int totalCount = 5;

        when(postRepo.getTotalPostsCount(tag)).thenReturn(totalCount);

        Paging result = feedService.getPageInfo(pageSize, pageNumber, tag);

        assertEquals(pageNumber, result.pageNumber());
        assertEquals(pageSize, result.pageSize());
        assertFalse(result.hasNext());
        assertFalse(result.hasPrevious());
        verify(postRepo).getTotalPostsCount(tag);
        verify(postRepo, never()).getTotalPostsCount();
    }

    @Test
    void getPageInfo_ShouldHandleNullCount() {
        int pageSize = 10;
        int pageNumber = 1;
        String tag = "non-existent-tag";

        when(postRepo.getTotalPostsCount(tag)).thenReturn(null);

        Paging result = feedService.getPageInfo(pageSize, pageNumber, tag);

        assertEquals(pageNumber, result.pageNumber());
        assertEquals(pageSize, result.pageSize());
        assertFalse(result.hasNext());
        assertFalse(result.hasPrevious());
    }

    @Test
    void getPageInfo_ShouldHandleLastPageCorrectly() {
        int pageSize = 10;
        int pageNumber = 3;
        String tag = "";
        int totalCount = 25;

        when(postRepo.getTotalPostsCount()).thenReturn(totalCount);

        Paging result = feedService.getPageInfo(pageSize, pageNumber, tag);

        assertFalse(result.hasNext());
        assertTrue(result.hasPrevious());
    }

    @Test
    void getPageInfo_ShouldHandleFirstPageCorrectly() {
        int pageSize = 10;
        int pageNumber = 1;
        String tag = "";
        int totalCount = 25;

        when(postRepo.getTotalPostsCount()).thenReturn(totalCount);

        Paging result = feedService.getPageInfo(pageSize, pageNumber, tag);

        assertTrue(result.hasNext());
        assertFalse(result.hasPrevious());
    }
}
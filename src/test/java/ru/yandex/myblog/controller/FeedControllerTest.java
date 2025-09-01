package ru.yandex.myblog.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;
import ru.yandex.myblog.model.domain.Post;
import ru.yandex.myblog.model.dto.Paging;
import ru.yandex.myblog.model.mappers.PostMapper;
import ru.yandex.myblog.model.mappers.PostMapperImpl;
import ru.yandex.myblog.service.FeedService;

@WebMvcTest(controllers = {FeedController.class, PostMapper.class})
class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeedService feedService;

    @Test
    void getFeed_ShouldReturnPostsViewWithDefaultParameters() throws Exception {
        List<Post> mockPosts = Collections.singletonList(new Post(1L, "Test Post", "Content", 10));
        Paging mockPaging = new Paging(1, 10, false, false);

        when(feedService.getPosts("", 10, 1)).thenReturn(mockPosts);
        when(feedService.getPageInfo(10, 1, "")).thenReturn(mockPaging);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts"))
        ;

        verify(feedService).getPosts("", 10, 1);
        verify(feedService).getPageInfo(10, 1, "");
    }

    @Test
    void getFeed_ShouldHandleSearchParameter() throws Exception {
        String searchTerm = "test";
        List<Post> mockPosts = Collections.singletonList(new Post(1L, "Test Post", "Content", 10));
        Paging mockPaging = new Paging(1, 10, false, false);

        when(feedService.getPosts(searchTerm, 10, 1)).thenReturn(mockPosts);
        when(feedService.getPageInfo(10, 1, searchTerm)).thenReturn(mockPaging);

        mockMvc.perform(get("/posts")
                        .param("search", searchTerm))
                .andExpect(status().isOk())
                .andExpect(view().name("posts"));

        verify(feedService).getPosts(searchTerm, 10, 1);
        verify(feedService).getPageInfo(10, 1, searchTerm);
    }

    @Test
    void getFeed_ShouldHandleCustomPagination() throws Exception {
        int pageSize = 5;
        int pageNumber = 2;
        List<Post> mockPosts = Collections.singletonList(new Post(1L, "Test Post", "Content", 10));
        Paging mockPaging = new Paging(pageNumber, pageSize, true, true);

        when(feedService.getPosts("", pageSize, pageNumber)).thenReturn(mockPosts);
        when(feedService.getPageInfo(pageSize, pageNumber, "")).thenReturn(mockPaging);

        mockMvc.perform(get("/posts")
                        .param("pageSize", String.valueOf(pageSize))
                        .param("pageNumber", String.valueOf(pageNumber)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts"));

        verify(feedService).getPosts("", pageSize, pageNumber);
        verify(feedService).getPageInfo(pageSize, pageNumber, "");
    }

    @Test
    void getFeed_ShouldHandleEmptyResults() throws Exception {
        when(feedService.getPosts(anyString(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
        when(feedService.getPageInfo(anyInt(), anyInt(), anyString())).thenReturn(new Paging(1, 10, false, false));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts"));

        verify(feedService).getPosts("", 10, 1);
        verify(feedService).getPageInfo(10, 1, "");
    }
}
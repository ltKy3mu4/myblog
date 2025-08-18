package ru.yandex.myblog.controller;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import ru.yandex.myblog.model.domain.Post;
import ru.yandex.myblog.service.PostService;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PostService postService;

    @Mock
    private Model model;

    @InjectMocks
    private PostController postController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(postController)
                .build();
    }

    @Test
    void getPost_ShouldReturnPostView() throws Exception {
        long postId = 1L;
        Post mockPost = new Post(postId, "Test Post", "Content", 10);
        when(postService.getPost(postId)).thenReturn(mockPost);

        mockMvc.perform(get("/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(view().name("post"))
                .andExpect(model().attributeExists("post"));

        verify(postService).getPost(postId);
    }

    @Test
    void savePost_ShouldCreatePostAndRedirect() throws Exception {
        String title = "New Post";
        String text = "Content";
        String tags = "tag1 tag2";
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test image".getBytes());
        when(postService.createPost(title, text, image, tags)).thenReturn( new Post(1L, title, text, 0));

        mockMvc.perform(multipart("/posts")
                        .file(image)
                        .param("title", title)
                        .param("text", text)
                        .param("tags", tags))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(postService).createPost(title, text, image, tags);
    }

    @Test
    void updatePost_ShouldUpdatePostAndRedirect() throws Exception {
        long postId = 1L;
        String title = "Updated Post";
        String text = "Updated Content";
        String tags = "tag1 tag2";
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test image".getBytes());
        Post mockPost = new Post(postId, title, text, 5);
        when(postService.update(postId, title, text, image, tags)).thenReturn(mockPost);

        mockMvc.perform(multipart("/posts/{id}", postId)
                        .file(image)
                        .param("title", title)
                        .param("text", text)
                        .param("tags", tags))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(postService).update(postId, title, text, image, tags);
    }

    @Test
    void getPostAddingPage_ShouldReturnAddPostView() throws Exception {
        mockMvc.perform(get("/posts/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-post"));
    }

    @Test
    void getPostEditingPage_ShouldReturnAddPostViewWithPost() throws Exception {
        long postId = 1L;
        Post mockPost = new Post(postId, "Test Post", "Content", 10);
        when(postService.getPost(postId)).thenReturn(mockPost);

        mockMvc.perform(get("/posts/{id}/edit", postId))
                .andExpect(status().isOk())
                .andExpect(view().name("add-post"))
                .andExpect(model().attributeExists("post"));

        verify(postService).getPost(postId);
    }

    @Test
    void likePost_ShouldLikePostAndRedirect() throws Exception {
        long postId = 1L;
        boolean like = true;
        Post mockPost = new Post(postId, "Test Post", "Content", 11);
        when(postService.likePost(postId, like)).thenReturn(mockPost);

        mockMvc.perform(post("/posts/{id}/like", postId)
                        .param("like", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(postService).likePost(postId, like);
    }

    @Test
    void deletePost_ShouldDeletePostAndRedirect() throws Exception {
        long postId = 1L;

        mockMvc.perform(post("/posts/{id}/delete", postId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).deletePost(postId);
    }
}
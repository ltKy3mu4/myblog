package ru.yandex.myblog.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.myblog.service.CommentService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build();
    }

    @Test
    void addCommentToPost_ShouldCallServiceAndRedirect() throws Exception {
        long postId = 1L;
        String commentText = "Test comment";

        mockMvc.perform(post("/posts/{id}/comments", postId)
                        .param("text", commentText))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId));

        verify(commentService).save(postId, commentText);
    }

    @Test
    void editComment_ShouldCallServiceAndRedirect() throws Exception {
        long postId = 1L;
        long commentId = 10L;
        String updatedText = "Updated comment";

        mockMvc.perform(post("/posts/{id}/comments/{commentId}", postId, commentId)
                        .param("text", updatedText))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId));

        verify(commentService).update(commentId, postId, updatedText);
    }

    @Test
    void deleteComment_ShouldCallServiceAndRedirect() throws Exception {
        long postId = 1L;
        long commentId = 10L;

        mockMvc.perform(post("/posts/{id}/comments/{commentId}/delete", postId, commentId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId));

        verify(commentService).delete(commentId, postId);
    }

    @Test
    void addCommentToPost_ShouldHandleEmptyComment() throws Exception {
        long postId = 1L;
        String emptyText = "";

        mockMvc.perform(post("/posts/{id}/comments", postId)
                        .param("text", emptyText))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId));

        verify(commentService).save(postId, emptyText);
    }

    @Test
    void editComment_ShouldHandleEmptyComment() throws Exception {
        long postId = 1L;
        long commentId = 10L;
        String emptyText = "";

        mockMvc.perform(post("/posts/{id}/comments/{commentId}", postId, commentId)
                        .param("text", emptyText))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId));

        verify(commentService).update(commentId, postId, emptyText);
    }
}
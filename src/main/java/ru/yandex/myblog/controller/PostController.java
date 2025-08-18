package ru.yandex.myblog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.myblog.model.domain.Post;
import ru.yandex.myblog.service.PostService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/posts/{id}")
    public String getPost(@PathVariable(name = "id") long id, Model model) {
        Post post = postService.getPost(id);

        model.addAttribute("post", post);

        return "post";
    }

    @PostMapping("/posts")
    public String savePost(         @RequestParam(name = "title") String title,
                                    @RequestParam(name = "text") String text,
                                    @RequestParam(name = "image") MultipartFile image,
                                    @RequestParam(name = "tags") String tags) {

        Post post = postService.createPost(title, text, image, tags);

        return "redirect:/posts/"+post.getId();
    }

    @PostMapping("/posts/{id}")
    public String updatePost(
                                    @PathVariable(name = "id") long id,
                                    @RequestParam(name = "title") String title,
                                    @RequestParam(name = "text") String text,
                                    @RequestParam(name = "image") MultipartFile image,
                                    @RequestParam(name = "tags") String tags) {

        Post post = postService.update(id, title, text, image, tags);

        return "redirect:/posts/"+post.getId();
    }


    @GetMapping("/posts/add")
    public String getPostAddingPage() {
        return "add-post";
    }

    @GetMapping("/posts/{id}/edit")
    public String getPostEditingPage(@PathVariable(name = "id") long id, Model model) {
        Post post = postService.getPost(id);
        model.addAttribute("post", post);
        return "add-post";
    }

    @PostMapping("/posts/{id}/like")
    public String like(@PathVariable(name = "id") long id, @RequestParam(name = "like") boolean like) {
        Post post = postService.likePost(id, like);
        return "redirect:/posts/"+post.getId();
    }

    @PostMapping(value = "/posts/{id}/delete")
    public String delete(@PathVariable(name = "id") Long id) {
        postService.deletePost(id);
        return "redirect:/posts";
    }
}

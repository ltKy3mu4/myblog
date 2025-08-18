package ru.yandex.myblog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.myblog.service.CommentService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{id}/comments")
    public String addCommentToPost(@PathVariable(name = "id") long id, @RequestParam(name = "text") String text) {
        commentService.save(id, text);
        return "redirect:/posts/"+id;
    }

    @PostMapping("/posts/{id}/comments/{commentId}")
    public String editComment(@PathVariable(name = "id") long id, @PathVariable(name = "commentId") long commentId, @RequestParam (name = "text") String text) {
        commentService.update(commentId, id, text);
        return "redirect:/posts/"+id;
    }

    @PostMapping("/posts/{id}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable(name = "id") long id, @PathVariable(name = "commentId")  long commentId) {
        commentService.delete(commentId, id);
        return "redirect:/posts/"+id;
    }

}

package ru.yandex.myblog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yandex.myblog.model.domain.Post;
import ru.yandex.myblog.model.dto.FeedPostDto;
import ru.yandex.myblog.model.dto.Paging;
import ru.yandex.myblog.model.mappers.PostMapper;
import ru.yandex.myblog.service.FeedService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final PostMapper postMapper;

    @GetMapping(value = "/")
    public String redirect() {
        return "redirect:/posts";
    }

    @GetMapping("/posts")
    public String getFeed(
            @RequestParam(name = "search", defaultValue = "", required = false) String search,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(name = "pageNumber",defaultValue = "1", required = false) int pageNumber,
            Model model) {

        List<Post> posts = feedService.getPosts(search, pageSize, pageNumber);
        List<FeedPostDto> feedPostDtoList = postMapper.toFeedPostDtoList(posts);

        Paging pageInfo = feedService.getPageInfo(pageSize, pageNumber, search);

        model.addAttribute("paging", pageInfo);
        model.addAttribute("posts", feedPostDtoList);
        model.addAttribute("search", search);


        return "posts";
    }

}

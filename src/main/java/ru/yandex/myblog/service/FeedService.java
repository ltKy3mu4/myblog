package ru.yandex.myblog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.myblog.model.domain.Post;
import ru.yandex.myblog.model.dto.Paging;
import ru.yandex.myblog.repository.JdbcCommentRepository;
import ru.yandex.myblog.repository.JdbcImageRepo;
import ru.yandex.myblog.repository.JdbcPostRepository;
import ru.yandex.myblog.repository.JdbcTagRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FeedService {

    private final JdbcPostRepository postRepo;
    private final JdbcTagRepository tagRepo;
    private final JdbcCommentRepository commentsRepo;
    private final JdbcImageRepo imageRepo;

    public List<Post> getPosts(String searchedTag, int pageSize, int pageNumber) {
        int offset = (pageNumber - 1) * pageSize;
        List<Post> lastPosts = searchedTag.isEmpty() ? postRepo.getPosts(offset, pageSize) : postRepo.getPostsByTag(searchedTag, offset, pageSize);

        for (Post post : lastPosts) {
            post.setTags(tagRepo.getTagsForPost(post.getId()));
            post.setComments(commentsRepo.getAllForPost(post.getId()));
            post.setImage(imageRepo.getByPostId(post.getId()));
        }

        return lastPosts;
    }

    public Paging getPageInfo(int pageSize, int pageNumber, String tag){
        Integer count = tag.isEmpty() ? postRepo.getTotalPostsCount() : postRepo.getTotalPostsCount(tag);
        if (count == null){
            count = 0;
        }
        int pageCount = count / pageSize + (count % pageSize == 0 ? 0 : 1);
        boolean isLastPage = pageNumber == pageCount;
        boolean isFirstPage = (pageNumber == 1);
        return new Paging(pageNumber, pageSize,  !isLastPage && count != 0, !isFirstPage && count != 0);
    }
}

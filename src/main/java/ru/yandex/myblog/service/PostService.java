package ru.yandex.myblog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.myblog.model.domain.Comment;
import ru.yandex.myblog.model.domain.Post;
import ru.yandex.myblog.model.domain.Tag;
import ru.yandex.myblog.model.exception.PostNotFoundException;
import ru.yandex.myblog.repository.JdbcCommentRepository;
import ru.yandex.myblog.repository.JdbcImageRepo;
import ru.yandex.myblog.repository.JdbcPostRepository;
import ru.yandex.myblog.repository.JdbcTagRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final JdbcPostRepository postRepo;
    private final JdbcTagRepository tagRepo;
    private final JdbcCommentRepository commentsRepo;
    private final JdbcImageRepo imageRepo;


    @Transactional
    public Post createPost(String title, String  text, MultipartFile file, String tagsStr){
        Post post = new Post();
        post.setTitle(title);
        post.setText(text);
        post.setLikesCount(0);

        long postId = postRepo.save(post);
        post.setId(postId);

        imageRepo.save(postId, file);

        List<Tag> tags = Arrays.stream(tagsStr.split(" ")).map(e -> new Tag(e)).toList();
        tagRepo.saveBatch(tags);

        List<Tag> tagsByNames = tagRepo.getTagsByNames(tags.stream().map(e -> e.getName()).toList());
        tagRepo.bindTagsToPost(postId, tagsByNames);
        post.setTags(tagsByNames);
        return post;
    }

    @Transactional
    public Post update(long id, String title, String  text, MultipartFile file, String tagsStr){
        Post post = this.getPost(id);
        post.setTitle(title);
        post.setText(text);

        long postId = postRepo.save(post);
        post.setId(postId);

        imageRepo.delete(postId);
        imageRepo.save(postId, file);

        tagRepo.deleteTagToPostConnections(id);

        List<Tag> tags = Arrays.stream(tagsStr.split(" ")).map(e -> new Tag(e)).toList();
        tagRepo.saveBatch(tags);

        List<Tag> tagsByNames = tagRepo.getTagsByNames(tags.stream().map(e -> e.getName()).toList());
        tagRepo.bindTagsToPost(postId, tagsByNames);
        post.setTags(tagsByNames);
        return post;
    }

    public Post getPost(long id) {
        Optional<Post> postOp = postRepo.findById(id);
        if (postOp.isEmpty()){
            throw  new PostNotFoundException("Post with id "+id+" not found");
        }
        Post post = postOp.get();

        post.setTags(tagRepo.getTagsForPost(id));
        post.setComments(commentsRepo.getAllForPost(id));
        post.setImage(imageRepo.getByPostId(id));
        return post;
    }

    @Transactional
    public Post likePost(long id, boolean liked) {
        if (liked){
            postRepo.likePost(id);
        } else {
            postRepo.unlikePost(id);
        }
        return getPost(id);
    }

    @Transactional
    public void deletePost(long id) {
        int res = commentsRepo.deleteAllForPost(id);
        log.info("Post {} had {} comments, which were removed", id, res);

        int tagsConnections = tagRepo.deleteTagToPostConnections(id);
        log.info("Post {} had {} tags, which were removed", id, tagsConnections);

        imageRepo.delete(id);

        int count = postRepo.deleteById(id);
        if (count == 0){
            log.info("post with id {} not found and cannot be deleted", id);
        }
    }

}

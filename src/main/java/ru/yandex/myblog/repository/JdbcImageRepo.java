package ru.yandex.myblog.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.myblog.model.domain.ImageBlob;

import java.io.IOException;

@Repository
@RequiredArgsConstructor
public class JdbcImageRepo {

    private final JdbcTemplate jdbcTemplate;

    public boolean save(long postId, MultipartFile file) {
        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        jdbcTemplate.update(
                "insert into images (post_id, file_name, data) values(?, ?, ?)",
                postId, file.getOriginalFilename(), data
        );
        return true;
    }

    public boolean delete(long postId) {
        int update = jdbcTemplate.update("delete from images where post_id = ?", postId);
        return update > 0;
    }

    public ImageBlob getByPostId(long postId) {
        return jdbcTemplate.queryForObject(
                "SELECT id, post_id, file_name, data FROM images WHERE post_id = ?",
                (rs, rowNum) -> new ImageBlob(
                        rs.getLong("id"),
                        rs.getLong("post_id"),
                        rs.getString("file_name"),
                        rs.getBytes("data")
                ),
                postId
        );
    }
}

package com.example.webservice.domain.posts;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PostRepositoryTest {
    @Autowired PostRepository postRepository;
    @After
    public void cleanup(){
        postRepository.deleteAll();
    }
    @Test
    public void 게시글저장_불러오기() throws Exception{
        // given
        String title = "테스트 게시글";
        String content = "테스트 본문";
        String author = "rltn2121@naver.com";
        postRepository.save(Posts.builder()
        .title(title)
        .content(content)
        .author(author)
        .build());

        // when
        List<Posts> postsList = postRepository.findAll();

        // then
        Posts posts = postsList.get(0);
        Assertions.assertThat(posts.getTitle()).isEqualTo(title);
        Assertions.assertThat(posts.getContent()).isEqualTo(content);
        Assertions.assertThat(posts.getAuthor()).isEqualTo(author);

    }

    @Test
    public void BaseTimeEntity_등록() throws Exception{
        // given
        LocalDateTime now = LocalDateTime.of(2019, 6, 4, 0, 0, 0);
        postRepository.save(Posts.builder()
        .title("title")
        .content("content")
        .author("author")
        .build()
        );
        // when
        List<Posts> postsList = postRepository.findAll();

        // then
        Posts posts = postsList.get(0);

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println("posts.getCreatedDate() = " + posts.getCreatedDate());
        System.out.println("posts.getModifiedDate() = " + posts.getModifiedDate());
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        Assertions.assertThat(posts.getCreatedDate()).isAfter(now);
        Assertions.assertThat(posts.getCreatedDate()).isAfter(now);

    }
}
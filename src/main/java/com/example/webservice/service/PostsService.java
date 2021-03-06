package com.example.webservice.service;

import com.example.webservice.domain.posts.PostRepository;
import com.example.webservice.domain.posts.Posts;
import com.example.webservice.web.dto.PostsListResponseDto;
import com.example.webservice.web.dto.PostsResponseDto;
import com.example.webservice.web.dto.PostsSaveRequestDto;
import com.example.webservice.web.dto.PostsUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class PostsService {
    private final PostRepository postRepository;

    // 등록
    public Long save(PostsSaveRequestDto requestDto){
        // Dto를 entity로 변경해서 저장
        return postRepository.save(requestDto.toEntity()).getId();
    }

    // 수정
    public Long update(Long id, PostsUpdateRequestDto requestDto) {
        Posts posts = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. id = " + id));
        posts.update(requestDto.getTitle(), requestDto.getContent());
        return id;
    }

    // 조회
    public PostsResponseDto findById(Long id) {
        Posts posts = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. id = " + id));
        // 엔티티로 조회해서 dto로 리턴
        return new PostsResponseDto(posts);
    }

    public void delete(Long id){
        Posts posts = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. id = " + id));
        postRepository.delete(posts);
    }
    @Transactional(readOnly = true)
    public List<PostsListResponseDto> findAllDesc(){
        return postRepository.findAllDesc()
                .stream()
                .map(PostsListResponseDto::new)
                .collect(Collectors.toList());
    }
}

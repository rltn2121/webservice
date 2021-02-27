package com.example.webservice.web;

import com.example.webservice.config.auth.LoginUser;
import com.example.webservice.config.auth.dto.SessionUser;
import com.example.webservice.domain.user.User;
import com.example.webservice.service.PostsService;
import com.example.webservice.web.dto.PostsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpSession;

@RequiredArgsConstructor
@Controller
public class IndexController {
    private final PostsService postsService;
    private final HttpSession httpSession;
    @GetMapping("/")
    public String index(Model model, @LoginUser SessionUser user){
        model.addAttribute("posts", postsService.findAllDesc());

        // index 메소드 외에 다른 컨트롤러와 메소드에서 세션값이 필요하면 그 때마다 직접 세션에서 값을 가져와야 함
        // 같은 코드 반복됨 -> 메소드 인자로 세션값을 바로 받을 수 있도록 변경
        //SessionUser user = (SessionUser) httpSession.getAttribute("user");
        if(user!=null){
            model.addAttribute("userName", user.getName());
        }
        return "index";
    }

    // 등록
    @GetMapping("/posts/save")
    public String postsSave(){
        return "posts-save";
    }

    // 수정
    @GetMapping("/posts/update/{id}")
    public String postsUpdate(@PathVariable Long id, Model model){
        PostsResponseDto dto = postsService.findById(id);
        model.addAttribute("post", dto);
        return "posts-update";
    }
}

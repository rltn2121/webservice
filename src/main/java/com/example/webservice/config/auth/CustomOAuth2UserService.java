package com.example.webservice.config.auth;

import com.example.webservice.config.auth.dto.OAuthAttributes;
import com.example.webservice.config.auth.dto.SessionUser;
import com.example.webservice.domain.user.User;
import com.example.webservice.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Collections;

// 구글 로그인 이후 가져온 사용자의 정보(email, name, picture 등)들을 기반으로 가입 및 정보수정, 세션 저장 등의 기능 지원
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // delegate: 대리인
        OAuth2UserService delegate = new DefaultOAuth2UserService();
        // OAuth2UserSerivce를 통해서 사용자 정보 불러옴
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 현재 로그인 진행 중인 서비스를 구분하는 콛
        // 네이버 로그인, 구글 로그인 구분하기 위해 사용
        String registrationId =
                userRequest
                        .getClientRegistration()
                        .getRegistrationId();

        // OAuth2 로그인 진행 시 키가 되는 필드값. Primary Key와 같은 의미
        // 구글의 경우 기본적으로 코드를 지원하지만, 네이버, 카카오 등은 지원하지 않음
        // 네이버 로그인, 구글 로그인 동시 지원할 때 사용됨
        String userNameAttributeName =
                userRequest
                        .getClientRegistration()
                        .getProviderDetails()
                        .getUserInfoEndpoint()
                        .getUserNameAttributeName();

        // OAuth2UserService를 통해 가져온 OAuth2User의 attribute를 담은 클래스
        // 이후 네이버 등 다른 소셜 로그인도 이 클래스 사용
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        User user = saveOrUpdate(attributes);

        // SessionUser(): 세션에 사용자 정보를 저장하기 위한 dto 클래스
        httpSession.setAttribute("user", new SessionUser(user));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey()
        );
    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        // 이메일로 user entity 찾음
        User user = userRepository.findByEmail(attributes.getEmail())
                // 1) user entity의 이름과 사진 업데이트
                .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                // 2) user entity 없으면 attribute를 entity로 변환
                .orElse(attributes.toEntity());

        // userRepository 통해서 변경사항 저장
        return userRepository.save(user);
    }
}

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

// 소셜 로그인으로 가져온 사용자의 정보(email, name, picture 등)들을 기반으로
// 회원가입 및 정보수정, 세션 저장 등의 기능 지원
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 현재 로그인 진행 중인 서비스를 구분하는 코드
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

        // saveOrUpdate(): 사용자 생성 또는 갱신 후에 user 엔티티 반환
        // SessionUser(): 세션에 사용자 정보를 저장하기 위한 dto 클래스
        User user = saveOrUpdate(attributes);
        httpSession.setAttribute("user", new SessionUser(user));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey()
        );
    }

    // 이메일로 user entity 찾음 (findByEmail)
    // 1) user 존재 -> entity의 이름과 사진 업데이트
    // 2) user 없음 -> OAuthAttributes.toEntity() 통해서 User entity 생성
    // userRepository 통해서 변경사항 저장
    private User saveOrUpdate(OAuthAttributes attributes) {
        User user = userRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                .orElse(attributes.toEntity());
        return userRepository.save(user);
    }
}

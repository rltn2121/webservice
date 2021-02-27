package com.example.webservice.config.auth.dto;

import com.example.webservice.domain.user.Role;
import com.example.webservice.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
// User class를 직접 사용하지 않는 이유
// User 클래스를 세션에 저장하기 위해서는 User 클래스에 직렬화를 구현해야 함
// 그러나, 엔티티 클래스는 언제 다른 엔티티와 관계가 형성될 지 모름
// 자식 엔티티를 갖고 있다면 직렬화 대상에 자식들까지 포함 -> 성능 이슈, 부수 효과 발생
// 따라서, 직렬화 기능을 가진 세션 DTO 하나를 추가로 만듦
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;

    // OAuth2User에서 반환하는 사용자 정보는 Map이기 때문에 값 하나하나를 변환해야 함
    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes){
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes){
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // User 엔티티 생성
    // OAuthAttributes에서 엔티티를 생성하는 시점 -> 처음 가입할 때
    // 가입할 때의 기본 권한을 GUEST로 주기 위해서 role 빌더 값에 Role.GUEST 사용
    public User toEntity(){
        return User.builder()
                .name(name)
                .email(email)
                .picture(picture)
                .role(Role.GUEST)
                .build();
    }
}

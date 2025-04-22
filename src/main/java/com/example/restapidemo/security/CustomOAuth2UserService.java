package com.example.restapidemo.security;

import com.example.restapidemo.entity.User;
import com.example.restapidemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        // Xử lý trường hợp email null
        if (email == null) {
            if (provider.equals("github")) {
                String login = oauth2User.getAttribute("login");
                email = login != null ? login + "@github.com" : "unknown-" + System.currentTimeMillis() + "@github.com";
            } else {
                email = "unknown-" + System.currentTimeMillis() + "@" + provider + ".com";
            }
        }

        // Lưu hoặc cập nhật người dùng trong database
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(name != null ? name : "Unknown");
            user.setPassword("");
            user.setRole("USER");
            user.setProvider(provider);
            userRepository.save(user);
        } else {
            user.setProvider(provider);
            userRepository.save(user);
        }

        // Tạo attributes mới để đảm bảo email không null
        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
        attributes.put("email", email);

        return new DefaultOAuth2User(
                Collections.singletonList(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email");
    }
}
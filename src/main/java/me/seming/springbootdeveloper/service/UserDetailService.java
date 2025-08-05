package me.seming.springbootdeveloper.service;

import lombok.RequiredArgsConstructor;
import me.seming.springbootdeveloper.domain.User;
import me.seming.springbootdeveloper.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
// 스프링 시큐리티에서 사용자 정보를 가져오는 인터페이스
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public User loadUserByUsername(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException((email)));
    }
}

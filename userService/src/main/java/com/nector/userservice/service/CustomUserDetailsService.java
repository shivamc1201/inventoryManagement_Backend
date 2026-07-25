package com.nector.userservice.service;

import com.nector.userservice.model.User;
import com.nector.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Entering loadUserByUsername() for username: {}", username);

        User user = userRepository.findByUsernameWithRolesAndPermissions(username)
            .orElseThrow(() -> {
                log.warn("Exiting loadUserByUsername() - User not found: {}", username);
                return new UsernameNotFoundException("User not found: " + username);
            });

        CustomUserPrincipal principal = new CustomUserPrincipal(user, getAuthorities(user));
        log.info("Exiting loadUserByUsername() - User loaded successfully: {}", username);
        return principal;
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRoleType()));
        return authorities;
    }
}

package com.nector.userservice.service;

import com.nector.userservice.model.User;
import com.nector.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameWithRolesAndPermissions(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return new CustomUserPrincipal(user, getAuthorities(user));
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        // Add role-based authority
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRoleType().name()));
        
        // Add feature-based authorities from user's roles
        user.getRoles().forEach(role -> 
            role.getPermissions().forEach(permission -> 
                authorities.add(new SimpleGrantedAuthority("FEATURE_" + permission.getFeature().name()))
            )
        );
        
        return authorities;
    }
}
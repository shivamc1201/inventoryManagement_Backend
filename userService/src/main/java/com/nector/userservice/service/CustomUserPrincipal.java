package com.nector.userservice.service;

import com.nector.userservice.common.UserStatus;
import com.nector.userservice.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class CustomUserPrincipal implements UserDetails {
    
    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;
    
    public CustomUserPrincipal(User user, Collection<? extends GrantedAuthority> authorities) {
        this.user = user;
        this.authorities = authorities;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    @Override
    public String getUsername() {
        return user.getUsername();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != UserStatus.SUSPENDED;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE;
    }
    
    public Long getUserId() {
        return user.getId();
    }
}
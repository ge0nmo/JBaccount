package com.jbaacount.global.security.utiles;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomAuthorityUtils
{
    @Value("${mail.address.admin}")
    private String adminEmail;

    private final List<GrantedAuthority> ADMIN_ROLES = AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER");
    private final List<GrantedAuthority> USER_ROLES = AuthorityUtils.createAuthorityList("ROLE_USER");

    private final List<String> ADMIN_ROLES_STRING = List.of("ADMIN", "USER");

    private final List<String> USER_ROLES_STRING = List.of("USER");

    public List<GrantedAuthority> createAuthorities(String email)
    {
        if(email.equals(adminEmail))
            return ADMIN_ROLES;

        return USER_ROLES;
    }

    public List<GrantedAuthority> createAuthorities(List<String> roles)
    {
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> role.startsWith("ROLE_") ? new SimpleGrantedAuthority(role) : new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        return authorities;
    }

    public List<String> createRoles(String email)
    {
        if(email.equals(adminEmail))
            return ADMIN_ROLES_STRING;

        return USER_ROLES_STRING;
    }

}

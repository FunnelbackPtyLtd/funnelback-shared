package com.funnelback.publicui.integration.saml;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.funnelback.springmvc.api.config.security.FunnelbackRealmProperties;
import com.funnelback.springmvc.web.security.DefaultUserTokenSalt;
import com.funnelback.springmvc.web.security.HeaderOrParameterTokenBasedRememberMeServices;

public class TokenUtils {

    public static String makeToken(File searchHome, String serverSecret, String userName, String salt) {
        UserDetailsService userDetailsService = new UserDetailsService() {
            
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                return new UserDetails() {
                    
                    @Override
                    public boolean isEnabled() {
                        // TODO Auto-generated method stub
                        return false;
                    }
                    
                    @Override
                    public boolean isCredentialsNonExpired() {
                        // TODO Auto-generated method stub
                        return false;
                    }
                    
                    @Override
                    public boolean isAccountNonLocked() {
                        // TODO Auto-generated method stub
                        return false;
                    }
                    
                    @Override
                    public boolean isAccountNonExpired() {
                        // TODO Auto-generated method stub
                        return false;
                    }
                    
                    @Override
                    public String getUsername() {
                        return username;
                    }
                    
                    @Override
                    public String getPassword() {
                        // TODO Auto-generated method stub
                        return new FunnelbackRealmProperties(new File(searchHome, "conf/realm.properties")).getUserInfo(username)
                            .getEncodedPassword();
                    }
                    
                    @Override
                    public Collection<? extends GrantedAuthority> getAuthorities() {
                        // TODO Auto-generated method stub
                        return null;
                    }
                };
            }
        };
            //new FunnelbackUserDetailsManager(new FunnelbackRealmProperties(new File("/opt/")), resourceManager, searchHome, masterPasswordEncoder);
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        map.put(userName, salt);
        DefaultUserTokenSalt userTokenSalt = new DefaultUserTokenSalt(map);
        HeaderOrParameterTokenBasedRememberMeServices tokenService = new HeaderOrParameterTokenBasedRememberMeServices(serverSecret, userDetailsService, userTokenSalt);
        return tokenService.generateTokenForUser(userName, 10000, TimeUnit.DAYS);
        
    }
}

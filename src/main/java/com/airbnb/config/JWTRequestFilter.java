package com.airbnb.config;

import com.airbnb.entity.PropertyUser;
import com.airbnb.repository.PropertyUserRepository;
import com.airbnb.service.JWTService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

@Component
public class JWTRequestFilter extends OncePerRequestFilter {

    private JWTService jwtService;
    private PropertyUserRepository userRepository;


    public JWTRequestFilter(JWTService jwtService, PropertyUserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String tokenHeader = request.getHeader("Authorization");
        if(tokenHeader!=null && tokenHeader.startsWith("Bearer ")){
            String token = tokenHeader.substring(8,tokenHeader.length()-1);
            String userName = jwtService.getUserName(token);
            Optional<PropertyUser> opUser = userRepository.findByUsername(userName);
            if(opUser.isPresent()){
                PropertyUser propertyUser = opUser.get();

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(propertyUser, null, Collections.singleton(new SimpleGrantedAuthority(propertyUser.getUserRole())));
                authenticationToken.setDetails( new WebAuthenticationDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            }

        }
        filterChain.doFilter(request,response);

    }
}

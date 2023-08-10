package com.inn.zevo.login.security.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.inn.zevo.login.security.services.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthTokenFilter extends OncePerRequestFilter {
  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private UserDetailsServiceImpl userDetailsService;
  @Value("${jwt.token.header}")
  private String tokenHeader;
  private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
		try {
			final String authHeader = request.getHeader(tokenHeader);
			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				filterChain.doFilter(request, response);
				return;
			}
			final String jwt = authHeader.substring(7);
				String username = jwtUtils.extractUsername(jwt);
				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
					if (jwtUtils.isTokenValid(jwt, userDetails)) {
						UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
								null, userDetails.getAuthorities());
						authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(authToken);
					}
				}
				filterChain.doFilter(request, response);
		} catch (Exception e) {
      logger.error("Cannot set user authentication: {}", e);
    }

   
  }

 
}

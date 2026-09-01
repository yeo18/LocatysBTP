package com.cms.security.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cms.security.jwt.JwtService;
import com.cms.security.service.CustomUserDetailsService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtre d'authentification JWT.
 *
 * <p>Pour chaque requete porteuse d'un header {@code Authorization: Bearer ...} :
 * <ol>
 *   <li>le token est valide (signature HS256 + expiration) ;</li>
 *   <li>l'utilisateur est recharge depuis la base ({@code loadUserById}) ;</li>
 *   <li>l'Authentication est positionnee dans le SecurityContext.</li>
 * </ol>
 *
 * <p>En cas de token invalide ou expire, aucune authentification n'est
 * positionnee : la requete sera rejetee 401 par la chaine de securite.
 * Le token ne contient jamais de permissions (regle LOOP 3.8) : les droits
 * sont relus en base a chaque requete.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String PREFIXE_BEARER = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(PREFIXE_BEARER)) {
            String token = header.substring(PREFIXE_BEARER.length());
            try {
                if (jwtService.estValide(token)) {
                    Long userId = jwtService.extraireUserId(token);
                    UserDetails userDetails = userDetailsService.loadUserById(userId);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Authentification JWT acceptee : userId={}", userId);
                }
            } catch (JwtException | IllegalArgumentException | AuthenticationException ex) {
                logger.warn("Authentification JWT rejetee : {}", ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

}

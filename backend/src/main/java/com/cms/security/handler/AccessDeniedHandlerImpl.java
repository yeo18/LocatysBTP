package com.cms.security.handler;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.cms.common.constants.Messages;
import com.cms.common.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Gestionnaire d'acces refuse : reponse 403 uniforme (format ErrorResponse)
 * lorsqu'un utilisateur authentifie n'a pas le droit d'acceder a une ressource.
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public AccessDeniedHandlerImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ErrorResponse corps = ErrorResponse.of("ACCES_REFUSE", Messages.ACCES_REFUSE);
        response.getWriter().write(objectMapper.writeValueAsString(corps));
    }

}

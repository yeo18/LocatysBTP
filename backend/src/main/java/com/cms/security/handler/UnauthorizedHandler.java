package com.cms.security.handler;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.cms.common.constants.Messages;
import com.cms.common.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Point d'entree d'authentification : reponse 401 uniforme (format ErrorResponse)
 * lorsque la ressource est protegee et qu'aucune authentification valide n'est
 * fournie.
 */
@Component
public class UnauthorizedHandler implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public UnauthorizedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ErrorResponse corps = ErrorResponse.of("AUTHENTIFICATION_REQUISE", Messages.AUTHENTIFICATION_REQUISE);
        response.getWriter().write(objectMapper.writeValueAsString(corps));
    }

}

package com.parkeaya.local_paneladmi.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JwtAuthenticationFilter: toma el TOKEN de la sesión (atributo "TOKEN")
 * y, si está presente y válido, establece una Authentication simple
 * en el SecurityContext para permitir que Spring Security considere
 * la petición como autenticada.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		HttpSession session = request.getSession(false);

		if (session != null) {
			String token = (String) session.getAttribute("TOKEN");
			Object userPrincipal = session.getAttribute("USER");

			if (token != null && isValidToken(token)) {
				// Construimos una autenticación mínima basada en la sesión
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
						userPrincipal,
						null,
						Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
				);

				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		}

		filterChain.doFilter(request, response);
	}

	private boolean isValidToken(String token) {
		// Validación mínima: no vacío. Extendible para comprobar expiración/firmas.
		return token != null && !token.trim().isEmpty();
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.equals("/login") ||
				path.equals("/do-login") ||
				path.equals("/error") ||
				path.equals("/favicon.ico") ||
				path.startsWith("/css/") ||
				path.startsWith("/js/") ||
				path.startsWith("/images/") ||
				path.startsWith("/static/") ||
				path.startsWith("/webjars/");
	}
}

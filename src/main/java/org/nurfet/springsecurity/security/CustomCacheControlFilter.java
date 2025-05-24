package org.nurfet.springsecurity.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class CustomCacheControlFilter implements Filter {


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        // Применяем кэширование только для GET запросов
        if ("GET".equals(req.getMethod())) {
            String uri = req.getRequestURI();

            if (uri.startsWith("/public/")) {
                // Публичные ресурсы - кэширование на 1 час
                res.setHeader("Cache-Control", "public, max-age=3600, must-revalidate");
                res.setHeader("Vary", "Accept-Encoding");

            } else if (uri.startsWith("/api/secure/") || uri.startsWith("/admin/")) {
                // Защищённые эндпоинты - запрет кэширования
                res.setHeader("Cache-Control", "no-store, no-cache, max-age=0, must-revalidate, private");
                res.setHeader("Pragma", "no-cache");
                res.setHeader("Expires", "0");

            } else if (uri.startsWith("/api/")) {
                // Обычные API - короткое кэширование
                res.setHeader("Cache-Control", "private, max-age=300, must-revalidate");
                res.setHeader("Vary", "Accept-Encoding");
            }
            // Для остальных путей кэширование не устанавливаем
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}

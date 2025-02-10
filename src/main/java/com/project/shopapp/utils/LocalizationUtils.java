package com.project.shopapp.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class LocalizationUtils {
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    public String getLocalizeMessage(String message, Object... params) {//spread operator 1 or 2 phan tu
        HttpServletRequest request = WebUtils.getRequest();
        Locale locale = localeResolver.resolveLocale(request);
        return messageSource.getMessage(message, params, locale);
    }

}

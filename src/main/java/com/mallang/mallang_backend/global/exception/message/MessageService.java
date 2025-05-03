package com.mallang.mallang_backend.global.exception.message;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageSource messageSource;

    public String getMessage(String code) {
        return messageSource.getMessage(code, null, Locale.KOREA);
    }
}

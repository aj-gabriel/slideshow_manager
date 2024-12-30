package org.slideshow.validation;

import java.util.Map;

public record ValidationError(
        String code,
        String message,
        Map<String, Object> exchangeContext) {
}

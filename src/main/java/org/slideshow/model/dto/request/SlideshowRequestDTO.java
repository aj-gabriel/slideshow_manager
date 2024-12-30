package org.slideshow.model.dto.request;

import java.util.List;

public record SlideshowRequestDTO(List<ImageDetailsRequestDTO> images) {
}

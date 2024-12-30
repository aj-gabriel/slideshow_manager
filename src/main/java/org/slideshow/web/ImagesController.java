package org.slideshow.web;


import lombok.RequiredArgsConstructor;
import org.slideshow.service.impl.ImageServiceImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/images")
public class ImagesController {

  private final ImageServiceImpl imageService;
}

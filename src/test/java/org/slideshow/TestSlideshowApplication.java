package org.slideshow;

import org.springframework.boot.SpringApplication;

public class TestSlideshowApplication {

  public static void main(String[] args) {
    SpringApplication.from(SlideshowApplication::main).with(TestcontainersConfiguration.class).run(args);
  }

}

package org.slideshow.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slideshow.model.domain.ImageEntity;
import org.slideshow.model.dto.request.ImageCreateRequestDTO;
import org.slideshow.model.dto.response.ImageCreationResponseDTO;
import org.slideshow.model.dto.response.ValidationErrorResponseDTO;
import org.slideshow.service.ImageService;
import org.slideshow.service.SlideshowServiceFacade;
import org.slideshow.validation.ImagesValidationFacade;
import org.slideshow.validation.ValidationErrorCodes;
import org.slideshow.validation.validators.ImageValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.slideshow.web.ImagesController.API_V1_IMAGES_PATH;

@ExtendWith(SpringExtension.class)
@WebFluxTest(ImagesController.class)
public class ImagesControllerTest {

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ImageService imageService;

  @MockitoBean
  private ImagesValidationFacade validationFacade;

  @MockitoBean
  private ImageValidationService validator;

  @MockitoBean
  private SlideshowServiceFacade slideshowServiceFacade;


  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void tearDown() throws Exception {
  }

  @Test
  void shouldCreateNewImage() {
    //prepare
    short duration = 10;
    String url = "test_url";
    ImageCreateRequestDTO requestDTO = new ImageCreateRequestDTO(url, duration);
    ImageEntity imageEntity = new ImageEntity();
    imageEntity.setId(1L);
    imageEntity.setDuration(duration);
    imageEntity.setUrl(url);

    doReturn(Mono.empty()).when(validationFacade).validateRequest(any());
    doReturn(Mono.just(imageEntity)).when(imageService).createImage(any());

    //execute
    ImageCreationResponseDTO result = webTestClient.post()
            .uri(uriBuilder -> uriBuilder
                    .path(API_V1_IMAGES_PATH)
                    .build())
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(requestDTO), ImageCreateRequestDTO.class)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(ImageCreationResponseDTO.class)
            .returnResult()
            .getResponseBody();

    //verify
    assertNotNull(result);
    assertEquals(1L, result.id());
    assertEquals(duration, result.duration());
    assertEquals(url, result.url());

    verify(validationFacade).validateRequest(any());
    verify(imageService).createImage(any());

  }

  @Test
  void shouldNotCreateNewImageDueInvalidUrl() {
    //prepare
    short duration = 10;
    String url = "test_url";
    ImageCreateRequestDTO requestDTO = new ImageCreateRequestDTO(url, duration);
    ImageEntity imageEntity = new ImageEntity();
    imageEntity.setId(1L);
    imageEntity.setDuration(duration);
    imageEntity.setUrl(url);

    doReturn(Mono.just(new ValidationErrorResponseDTO(
            ValidationErrorCodes.INVALID_IMAGE_URL.getCode(),
            ValidationErrorCodes.INVALID_IMAGE_URL.getDefaultMessage()
    ))).when(validationFacade).validateRequest(any());

    //execute
    ImageCreationResponseDTO result = webTestClient.post()
            .uri(uriBuilder -> uriBuilder
                    .path(API_V1_IMAGES_PATH)
                    .build())
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(requestDTO), ImageCreateRequestDTO.class)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(ImageCreationResponseDTO.class)
            .returnResult()
            .getResponseBody();

    //verify
    assertNotNull(result);
    assertNull(result.id());
    assertNull(result.duration());
    assertNull(result.url());

    verify(validationFacade).validateRequest(any());
    verify(imageService, never()).createImage(any());

  }

  @Test
  void shouldDeleteImageByIdSuccessfully() {
    // prepare
    long idToDelete = 1L;

    when(slideshowServiceFacade.deleteImageAndUpdateSlideshow(any()))
            .thenReturn(Mono.empty());

    // execute
    webTestClient.delete()
            .uri(API_V1_IMAGES_PATH + "/" + idToDelete)
            .exchange()
            .expectStatus().isOk();

    // verify
    verify(slideshowServiceFacade).deleteImageAndUpdateSlideshow(any());
  }

  @Test
  void shouldHandleFailureWhenDeletingImageById() {
    // prepare
    long idToDelete = 1L;

    when(slideshowServiceFacade.deleteImageAndUpdateSlideshow(any()))
            .thenThrow(new RuntimeException("some runtime exception"));

    // execute
    webTestClient.delete()
            .uri(API_V1_IMAGES_PATH + "/" + idToDelete)
            .exchange()
            .expectStatus().is5xxServerError();

    // verify
    verify(slideshowServiceFacade, times(1)).deleteImageAndUpdateSlideshow(any(Mono.class));
  }
}

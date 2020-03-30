package com.skillbox.sw.controller;

import static com.skillbox.sw.utils.JwtConstants.EMAIL;
import static com.skillbox.sw.utils.JwtConstants.GOOD_TOKEN;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.skillbox.sw.domain.Person;
import com.skillbox.sw.repository.PersonRepository;
import com.skillbox.sw.utils.IntegrationTest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@IntegrationTest
public class StorageControllerIntegrationTest {

  @Autowired
  TestRestTemplate testRestTemplate;

  @Autowired
  private PersonRepository personRepository;

  static Person person;


  @BeforeEach
  public void init() {
    person = Person.builder()
        .id(1)
        .firstName("Вася")
        .lastName("Пупкин")
        .birthDate(LocalDate.of(1982, 11, 9))
        .regDate(LocalDate.now())
        .email(EMAIL)
        .password("123456789")
        .phone(123456)
        .lastOnlineTime(LocalDateTime.now())
        .photo("pic")
        .build();
    personRepository.save(person);
  }

  @Test
  void fileUpload() {
    HttpHeaders headers = getHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
    parameters.add("files", genFileSystemResource("bil.jpg"));

    HttpEntity<MultiValueMap<String, Object>> theHttpEntity = new HttpEntity<>(parameters, headers);

    ResponseEntity<Object> response = testRestTemplate
        .exchange("/storage?type=IMAGE",
            HttpMethod.POST, theHttpEntity, Object.class);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  public static HttpHeaders getHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(AUTHORIZATION, GOOD_TOKEN);
    return headers;
  }

  private FileSystemResource genFileSystemResource(String fileName) {
    return new FileSystemResource(this.getClass().getResource("/" + fileName).getPath());
  }
}

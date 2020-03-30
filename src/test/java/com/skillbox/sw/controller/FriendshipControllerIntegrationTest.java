package com.skillbox.sw.controller;

import com.skillbox.sw.api.request.UserIdListApi;
import com.skillbox.sw.api.response.FriendshipStatusApi;
import com.skillbox.sw.api.response.FriendshipStatusListApi;
import com.skillbox.sw.api.response.PersonListApi;
import com.skillbox.sw.api.response.ReportApi;
import com.skillbox.sw.api.response.ResponseApi;
import com.skillbox.sw.api.response.ResponsePersonApi;
import com.skillbox.sw.domain.enums.FriendshipCode;
import com.skillbox.sw.mapper.PersonMapper;
import com.skillbox.sw.repository.PersonRepository;
import com.skillbox.sw.utils.IntegrationTest;
import com.skillbox.sw.utils.JwtConstants;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@IntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FriendshipControllerIntegrationTest {

  @Autowired
  TestRestTemplate restTemplate;

  private static ResponsePersonApi ID1;
  private static ResponsePersonApi ID2;
  private static ResponsePersonApi ID3;
  private static ResponsePersonApi ID4;
  private static ResponsePersonApi ID5;
  private static ResponsePersonApi ID6;

  private static HttpHeaders GOOD_AUTH_ID1 = JwtConstants.getHeaders(JwtConstants.GOOD_TOKEN);
  private static HttpHeaders GOOD_AUTH_ID6 = JwtConstants.getHeaders(JwtConstants.GOOD_TOKEN_ID6);
  private static HttpHeaders BAD_AUTH = JwtConstants.getHeaders(JwtConstants.BAD_TOKEN);

  @BeforeAll
  static void init(
      @Autowired PersonRepository personRepository,
      @Autowired PersonMapper personMapper) {
    ID1 = personMapper.personToPersonApi(personRepository.findById(1).get());
    ID2 = personMapper.personToPersonApi(personRepository.findById(2).get());
    ID3 = personMapper.personToPersonApi(personRepository.findById(3).get());
    ID4 = personMapper.personToPersonApi(personRepository.findById(4).get());
    ID5 = personMapper.personToPersonApi(personRepository.findById(5).get());
    ID6 = personMapper.personToPersonApi(personRepository.findById(6).get());
  }

  @Test
  @Order(1)
  void getFriends() {
    List<ResponsePersonApi> friends = new ArrayList<>();
    friends.add(ID2);
    friends.add(ID3);

    HttpEntity<String> entity = new HttpEntity<>("body", GOOD_AUTH_ID1);

    ParameterizedTypeReference<PersonListApi> listParameterizedTypeReference =
        new ParameterizedTypeReference<PersonListApi>() {
          @Override
          public Type getType() {
            return super.getType();
          }
        };

    ResponseEntity<PersonListApi> response =
        restTemplate.exchange("/friends/", HttpMethod.GET, entity, listParameterizedTypeReference);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(2, response.getBody().getTotal());
    Assertions.assertEquals(friends, response.getBody().getPersonList());
  }

  @Test
  @Order(2)
  void getFriendsWithPagination() {
    List<ResponsePersonApi> friends = new ArrayList<>();
    friends.add(ID2);

    HttpEntity<String> entity = new HttpEntity<>("body", GOOD_AUTH_ID1);

    ParameterizedTypeReference<PersonListApi> listParameterizedTypeReference =
        new ParameterizedTypeReference<PersonListApi>() {
          @Override
          public Type getType() {
            return super.getType();
          }
        };

    ResponseEntity<PersonListApi> response =
        restTemplate.exchange(
            "/friends?offset=0&itemPerPage=1",
            HttpMethod.GET,
            entity,
            listParameterizedTypeReference);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(1, response.getBody().getPersonList().size());
    Assertions.assertEquals(friends, response.getBody().getPersonList());

    friends.clear();
    friends.add(ID3);

    response =
        restTemplate.exchange(
            "/friends?offset=1&itemPerPage=1",
            HttpMethod.GET,
            entity,
            listParameterizedTypeReference);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(1, response.getBody().getPersonList().size());
    Assertions.assertEquals(friends, response.getBody().getPersonList());
  }

  @Test
  @Order(3)
  void sendFriendshipRequest() {
    HttpEntity<String> entity = new HttpEntity<>("body", GOOD_AUTH_ID1);
    ResponseEntity<ResponseApi<? extends ReportApi>> response =
        getExchangeMessage("/friends/6", HttpMethod.POST, entity);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNull(response.getBody().getError());
    Assertions.assertEquals("ok", response.getBody().getData().getMessage());
  }

  @Test
  void sendFriendshipRequest_withNonexistentUser_resultBadRequest() {
    HttpEntity<String> entity = new HttpEntity<>("body", GOOD_AUTH_ID1);
    ResponseEntity<ResponseApi<? extends ReportApi>> response =
        getExchangeMessage("/friends/0", HttpMethod.POST, entity);

    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @Order(4)
  void getFriendshipRequest() {
    List<ResponsePersonApi> friends = new ArrayList<>();
    friends.add(ID1);

    HttpEntity<String> entity = new HttpEntity<>("body", GOOD_AUTH_ID6);

    ParameterizedTypeReference<PersonListApi> listParameterizedTypeReference =
        new ParameterizedTypeReference<PersonListApi>() {
          @Override
          public Type getType() {
            return super.getType();
          }
        };

    ResponseEntity<PersonListApi> response =
        restTemplate.exchange(
            "/friends/request", HttpMethod.GET, entity, listParameterizedTypeReference);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(1, response.getBody().getTotal());
    Assertions.assertEquals(friends, response.getBody().getPersonList());
  }

  @Test
  @Order(5)
  void approveFriendshipRequest() {
    HttpEntity<String> entity = new HttpEntity<>(null, GOOD_AUTH_ID6);
    ResponseEntity<ResponseApi<? extends ReportApi>> response =
        getExchangeMessage("/friends/1", HttpMethod.POST, entity);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNull(response.getBody().getError());
    Assertions.assertEquals("ok", response.getBody().getData().getMessage());
  }

  @Test
  @Order(6)
  void deleteFriend() {
    HttpEntity<String> entity = new HttpEntity<>(null, GOOD_AUTH_ID6);
    ResponseEntity<ResponseApi<? extends ReportApi>> response =
        getExchangeMessage("/friends/1", HttpMethod.DELETE, entity);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNull(response.getBody().getError());
    Assertions.assertEquals("ok", response.getBody().getData().getMessage());
  }

  @Test
  void deleteFriend_withNonexistentUser_resultBadRequest() {
    HttpEntity<String> entity = new HttpEntity<>(null, GOOD_AUTH_ID6);
    ResponseEntity<ResponseApi<? extends ReportApi>> response =
        getExchangeMessage("/friends/0", HttpMethod.DELETE, entity);

    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @Order(8)
  void cancelSubscription() {
    HttpEntity<String> entity = new HttpEntity<>(null, GOOD_AUTH_ID1);
    ResponseEntity<ResponseApi<? extends ReportApi>> response =
        getExchangeMessage("/friends/6", HttpMethod.DELETE, entity);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNull(response.getBody().getError());
    Assertions.assertEquals("ok", response.getBody().getData().getMessage());
  }

  @Test
  @Order(9)
  void resendFriendshipRequest() {
    HttpEntity<String> entity = new HttpEntity<>(null, GOOD_AUTH_ID6);
    ResponseEntity<ResponseApi<? extends ReportApi>> response =
        getExchangeMessage("/friends/1", HttpMethod.POST, entity);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNull(response.getBody().getError());
    Assertions.assertEquals("ok", response.getBody().getData().getMessage());
  }

  @Test
  @Order(10)
  void ignoreFriendshipRequest() {
    HttpEntity<String> entity = new HttpEntity<>(null, GOOD_AUTH_ID1);
    ResponseEntity<ResponseApi<? extends ReportApi>> response =
        getExchangeMessage("/friends/6", HttpMethod.DELETE, entity);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNull(response.getBody().getError());
    Assertions.assertEquals("ok", response.getBody().getData().getMessage());
  }

  @Test
  @Order(11)
  void revokeFriendshipRequest() {
    HttpEntity<String> entity = new HttpEntity<>(null, GOOD_AUTH_ID6);
    ResponseEntity<ResponseApi<? extends ReportApi>> response =
        getExchangeMessage("/friends/1", HttpMethod.DELETE, entity);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNull(response.getBody().getError());
    Assertions.assertEquals("ok", response.getBody().getData().getMessage());
  }

  @Test
  @Order(12)
  void getRecommendations() {
    List<ResponsePersonApi> recommended = new ArrayList<>();
    recommended.add(ID4);
    recommended.add(ID5);

    HttpEntity<String> entity = new HttpEntity<>("body", GOOD_AUTH_ID1);

    ParameterizedTypeReference<PersonListApi> listParameterizedTypeReference =
        new ParameterizedTypeReference<PersonListApi>() {
          @Override
          public Type getType() {
            return super.getType();
          }
        };

    ResponseEntity<PersonListApi> response =
        restTemplate.exchange(
            "/friends/recommendations", HttpMethod.GET, entity, listParameterizedTypeReference);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(2, response.getBody().getTotal());
    Assertions.assertEquals(recommended, response.getBody().getPersonList());
  }

  @Test
  void checkIfFriends() {
    List<FriendshipStatusApi> statuses = new ArrayList<>();
    statuses.add(new FriendshipStatusApi(ID2.getId(), FriendshipCode.FRIEND));
    statuses.add(new FriendshipStatusApi(ID3.getId(), FriendshipCode.FRIEND));

    List<Integer> ids = new ArrayList<>();
    ids.add(2);
    ids.add(3);
    ids.add(5);

    HttpEntity<UserIdListApi> entity = new HttpEntity<>(new UserIdListApi(ids), GOOD_AUTH_ID1);

    ResponseEntity<FriendshipStatusListApi> response =
        getExchangeFriendshipStatus("/is/friends", HttpMethod.POST, entity);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(statuses, response.getBody().getFriendshipStatusList());
  }

  private ResponseEntity<ResponseApi<? extends ReportApi>> getExchangeMessage(
      String url, HttpMethod HttpMethod, HttpEntity<String> entity) {
    return restTemplate.exchange(
        url,
        HttpMethod,
        entity,
        new ParameterizedTypeReference<ResponseApi<? extends ReportApi>>() {
          @Override
          public Type getType() {
            return super.getType();
          }
        });
  }

  private ResponseEntity<FriendshipStatusListApi> getExchangeFriendshipStatus(
      String url, HttpMethod HttpMethod, HttpEntity<UserIdListApi> entity) {
    return restTemplate.exchange(
        url,
        HttpMethod,
        entity,
        new ParameterizedTypeReference<FriendshipStatusListApi>() {
          @Override
          public Type getType() {
            return super.getType();
          }
        });
  }
}

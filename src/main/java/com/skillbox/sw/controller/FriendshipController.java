package com.skillbox.sw.controller;

import static com.skillbox.sw.config.SecurityConstants.HEADER;

import com.skillbox.sw.api.request.UserIdListApi;
import com.skillbox.sw.api.response.AbstractResponse;
import com.skillbox.sw.api.response.ReportApi;
import com.skillbox.sw.api.response.ResponseApi;
import com.skillbox.sw.service.FriendshipService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class FriendshipController {

  private FriendshipService friendshipService;

  @GetMapping("/friends")
  @ResponseStatus(HttpStatus.OK)
  public AbstractResponse getFriends(
      @RequestHeader(value = HEADER) String token,
      @RequestParam(value = "name", defaultValue = "") String name,
      @RequestParam(value = "offset", defaultValue = "0") Integer offset,
      @RequestParam(value = "itemPerPage", defaultValue = "20")
          Integer itemPerPage) {
    return friendshipService.findFriends(token, name, PageRequest.of(offset, itemPerPage));
  }

  @DeleteMapping("/friends/{id}")
  public AbstractResponse deleteFriend(
      @RequestHeader(value = HEADER) String token,
      @PathVariable(value = "id") int id) {
    friendshipService.deleteFriend(token, id);
    return new ResponseApi<>(null, new ReportApi("ok"));
  }

  @PostMapping("/friends/{id}")
  public AbstractResponse sendOrApproveRequest(
      @RequestHeader(value = HEADER) String token,
      @PathVariable(value = "id") int id) {
    friendshipService.sendOrApproveRequest(token, id);
    return new ResponseApi<>(null, new ReportApi("ok"));
  }

  @GetMapping("/friends/request")
  public AbstractResponse getFriendshipRequests(
      @RequestHeader(value = HEADER) String token,
      @RequestParam(value = "name", defaultValue = "") String name,
      @RequestParam(value = "offset", defaultValue = "0") Integer offset,
      @RequestParam(value = "itemPerPage", defaultValue = "20")
          Integer itemPerPage) {
    return friendshipService.getFriendshipRequests(
        token, name, PageRequest.of(offset, itemPerPage));
  }

  @GetMapping("/friends/recommendations")
  public AbstractResponse getRecommendations(
      @RequestHeader(value = HEADER) String token,
      @RequestParam(value = "offset", defaultValue = "0") Integer offset,
      @RequestParam(value = "itemPerPage", defaultValue = "20")
          Integer itemPerPage) {
    return friendshipService.getRecommendations(token, PageRequest.of(offset, itemPerPage));
  }

  @PostMapping("/is/friends")
  public AbstractResponse checkIfFriends(
      @RequestHeader(value = HEADER) String token,
      @RequestBody UserIdListApi userIdList) {
    return friendshipService.checkIfFriends(token, userIdList);
  }
}

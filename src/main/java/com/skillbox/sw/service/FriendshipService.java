package com.skillbox.sw.service;

import static org.springframework.data.jpa.domain.Specification.where;

import com.skillbox.sw.api.request.UserIdListApi;
import com.skillbox.sw.api.response.AbstractResponse;
import com.skillbox.sw.api.response.FriendshipStatusApi;
import com.skillbox.sw.api.response.FriendshipStatusListApi;
import com.skillbox.sw.api.response.PersonListApi;
import com.skillbox.sw.api.response.ResponsePersonApi;
import com.skillbox.sw.config.jwt.JwtProvider;
import com.skillbox.sw.domain.Friendship;
import com.skillbox.sw.domain.FriendshipStatus;
import com.skillbox.sw.domain.Person;
import com.skillbox.sw.domain.enums.FriendshipCode;
import com.skillbox.sw.mapper.PersonMapper;
import com.skillbox.sw.repository.FriendshipRepository;
import com.skillbox.sw.repository.PersonRepository;
import com.skillbox.sw.util.GenericSpecification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class FriendshipService {

  private static final String DELETE_LOG_MSG = "Delete friend request, from: {}, to: {}";
  private static final String ADD_LOG_MSG = "Send or approve request, from: {}, to: {}";

  private FriendshipRepository repository;
  private PersonRepository personRepository;
  private PersonService personService;
  private PersonMapper personMapper;
  private GenericSpecification<Friendship> spec;
  private GenericSpecification<Person> personSpec;

  public AbstractResponse findFriends(String token, String name, Pageable pageable) {
    log.info("Find friends request from {}", JwtProvider.getUsername(token));
    Specification<Friendship> s =
        where(spec.byFieldParam("srcPerson", "%", "email", JwtProvider.getUsername(token)))
            .and(
                spec.byFieldParam("dstPerson", "%", "firstName", name)
                    .or(spec.byFieldParam("dstPerson", "%", "lastName", name)))
            .and(spec.byFieldParam("friendshipStatus", "%", "code", FriendshipCode.FRIEND));
    Page<ResponsePersonApi> page =
        repository.findAll(s, pageable).map(f -> personMapper.personToPersonApi(f.getDstPerson()));
    log.info("Find friends exit");
    return new PersonListApi(
        "ok", page.toList(), page.getTotalElements(), pageable.getOffset(), page.getSize());
  }

  @Transactional
  public void deleteFriend(String token, int id) {
    Person me = personService.getCurrentPersonByToken(token);
    personRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("There is no user with id " + id));

    log.info(DELETE_LOG_MSG, me.getId(), id);

    List<Friendship> relations = findRelations(me.getId(), id);

    log.info(DELETE_LOG_MSG + ", db entities found: {}", me.getId(), id, relations.size());

    if (relations.size() == 2) {
      // check if consistent data
      Friendship meToGivenUserRelation =
          relations.stream()
              .filter(f -> f.getSrcPerson().equals(me))
              .findFirst()
              .orElseThrow(() -> new EntityNotFoundException("No friend with id " + id));
      Friendship givenUserToMeRelation =
          relations.stream()
              .filter(f -> f.getDstPerson().equals(me))
              .findFirst()
              .orElseThrow(() -> new EntityNotFoundException("No friend with id " + id));

      LocalDateTime now = LocalDateTime.now();

      if (meToGivenUserRelation.getFriendshipStatus().getCode().equals(FriendshipCode.REQUEST)) {
        changeFriendshipStatus(meToGivenUserRelation, FriendshipCode.DECLINED, now);
        log.info(DELETE_LOG_MSG + ", request ignored", me.getId(), id);
      } else if (meToGivenUserRelation
          .getFriendshipStatus()
          .getCode()
          .equals(FriendshipCode.FRIEND)) {
        changeFriendshipStatus(meToGivenUserRelation, FriendshipCode.REQUEST, now);
        changeFriendshipStatus(givenUserToMeRelation, FriendshipCode.SUBSCRIBED, now);
        log.info(DELETE_LOG_MSG + ", friend deleted", me.getId(), id);
      } else if (meToGivenUserRelation
          .getFriendshipStatus()
          .getCode()
          .equals(FriendshipCode.SUBSCRIBED)) {
        changeFriendshipStatus(meToGivenUserRelation, FriendshipCode.DECLINED, now);
        changeFriendshipStatus(givenUserToMeRelation, FriendshipCode.DECLINED, now);
        log.info(DELETE_LOG_MSG + ", subscription cancelled", me.getId(), id);
      }
    }
    // in case of inconsistent data throw Exception and answer 500
    // else {}
    repository.saveAll(relations);
  }

  @Transactional
  public void sendOrApproveRequest(String token, int id) {
    Person me = personService.getCurrentPersonByToken(token);
    Person givenUser =
        personRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("There is no user with id " + id));

    log.info(ADD_LOG_MSG, me.getId(), id);

    List<Friendship> relations = findRelations(me.getId(), id);

    log.info(ADD_LOG_MSG + ", db entities found: {}", me.getId(), id, relations.size());

    // send request logic
    if (relations.isEmpty()) {
      // TODO : add FRIEND_REQUEST notification
      // friendship status struct: id, 'rfc date','src.email->dst.email', code
      // me subscribing to given user
      FriendshipStatus meToGivenUserStatus =
          FriendshipStatus.builder()
              .name(me.getEmail() + "->" + givenUser.getEmail())
              .time(LocalDateTime.now())
              .code(FriendshipCode.SUBSCRIBED)
              .build();
      Friendship meToGivenUserRelation =
          Friendship.builder()
              .friendshipStatus(meToGivenUserStatus)
              .srcPerson(me)
              .dstPerson(givenUser)
              .build();

      // There's some embarrassing stuff, but this means the following
      // given user RECEIVED request from me
      FriendshipStatus givenUserToMeStatus =
          FriendshipStatus.builder()
              .name(givenUser.getEmail() + "->" + me.getEmail())
              .time(LocalDateTime.now())
              .code(FriendshipCode.REQUEST)
              .build();
      Friendship givenUserToMeRelation =
          Friendship.builder()
              .friendshipStatus(givenUserToMeStatus)
              .srcPerson(givenUser)
              .dstPerson(me)
              .build();

      relations.add(meToGivenUserRelation);
      relations.add(givenUserToMeRelation);
      log.info(ADD_LOG_MSG + ", new request placed", me.getId(), id);
    } else if (relations.size() == 2) {
      Friendship meToGivenUserRelation =
          relations.stream().filter(f -> f.getSrcPerson().equals(me)).findFirst().get();
      Friendship givenUserToMeRelation =
          relations.stream().filter(f -> f.getDstPerson().equals(me)).findFirst().get();

      if (givenUserToMeRelation.getFriendshipStatus().getCode().equals(FriendshipCode.FRIEND)) {
        log.info(ADD_LOG_MSG + "is already a friend", me.getId(), id);
        return;
      }

      LocalDateTime now = LocalDateTime.now();
      // approve request
      if (givenUserToMeRelation.getFriendshipStatus().getCode().equals(FriendshipCode.SUBSCRIBED)) {
        changeFriendshipStatus(meToGivenUserRelation, FriendshipCode.FRIEND, now);
        changeFriendshipStatus(givenUserToMeRelation, FriendshipCode.FRIEND, now);
        log.info(ADD_LOG_MSG + ", request approved", me.getId(), id);
        // resend request
      } else if (!givenUserToMeRelation
          .getFriendshipStatus()
          .getCode()
          .equals(FriendshipCode.REQUEST)) {
        log.info(ADD_LOG_MSG + ", request resent", me.getId(), id);
        changeFriendshipStatus(meToGivenUserRelation, FriendshipCode.SUBSCRIBED, now);
        changeFriendshipStatus(givenUserToMeRelation, FriendshipCode.REQUEST, now);
      }
    }
    // in case of inconsistent data throw Exception and answer 500
    // else {}
    repository.saveAll(relations);
  }

  public AbstractResponse getFriendshipRequests(String token, String name, Pageable pageable) {
    Specification<Friendship> s =
        spec.byFieldParam("srcPerson", "%", "email", JwtProvider.getUsername(token))
            .and(
                spec.byFieldParam("dstPerson", "%", "firstName", name)
                    .or(spec.byFieldParam("dstPerson", "%", "lastName", name)))
            .and(spec.byFieldParam("friendshipStatus", "%", "code", FriendshipCode.REQUEST));
    Page<ResponsePersonApi> page =
        repository.findAll(s, pageable).map(f -> personMapper.personToPersonApi(f.getDstPerson()));
    return new PersonListApi(
        "ok", page.toList(), page.getTotalElements(), pageable.getOffset(), page.getSize());
  }

  public AbstractResponse getRecommendations(String token, Pageable pageable) {
    Page<ResponsePersonApi> page =
        repository.getRecommendations(personService.getCurrentPersonByToken(token), pageable)
        .map(p -> personMapper.personToPersonApi(p));
    return new PersonListApi(
        "ok", page.toList(), page.getTotalElements(), pageable.getOffset(), page.getSize());
  }

  public AbstractResponse checkIfFriends(String token, UserIdListApi userIdList) {
    String myEmail = JwtProvider.getUsername(token);
    List<Integer> ids = userIdList.getUserIds();
    Specification<Friendship> s =
        spec.byFieldParam("dstPerson", "in", "id", ids)
            .and(spec.byFieldParam("srcPerson", "%", "email", myEmail));
    return new FriendshipStatusListApi(repository.findAll(s).stream()
        .map(f -> new FriendshipStatusApi(f.getDstPerson().getId(),f.getFriendshipStatus().getCode()))
        .collect(Collectors.toList()));
  }

  private List<Friendship> findRelations(int srcId, int dstId) {
    Specification<Friendship> s =
        (spec.byFieldParam("srcPerson", "%", "id", srcId)
                .and(spec.byFieldParam("dstPerson", "%", "id", dstId)))
            .or(
                spec.byFieldParam("dstPerson", "%", "id", srcId)
                    .and(spec.byFieldParam("srcPerson", "%", "id", dstId)));
    return repository.findAll(s);
  }

  private void changeFriendshipStatus(
      Friendship friendship, FriendshipCode code, LocalDateTime time) {
    friendship.getFriendshipStatus().setCode(code);
    friendship.getFriendshipStatus().setTime(time);
  }
}

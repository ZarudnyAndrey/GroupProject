package com.skillbox.sw.service;

import com.skillbox.sw.api.request.RegistrationApi;
import com.skillbox.sw.api.request.RequestNotificationSettingsApi;
import com.skillbox.sw.api.response.AbstractResponse;
import com.skillbox.sw.api.response.ReportApi;
import com.skillbox.sw.api.response.ResponseApi;
import com.skillbox.sw.config.jwt.JwtProvider;
import com.skillbox.sw.domain.Notification;
import com.skillbox.sw.domain.Person;
import com.skillbox.sw.mapper.PersonMapper;
import com.skillbox.sw.repository.PersonRepository;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = false)
@Service
@AllArgsConstructor
public class AccountService {

  private PersonRepository personRepository;
  private PersonService personService;
  private PersonMapper personMapper;
  private MailServerImpl mailServer;
  private PasswordEncoder bCryptPasswordEncoder;

  public AbstractResponse save(RegistrationApi registrationApi) {
      if (registrationApi.getPasswd1().equals(registrationApi.getPasswd2())) {
          Person person = personMapper.registrationApiToPerson(registrationApi);
          String email = person.getEmail();
          if (personRepository.findByEmail(email).isPresent()) {
              throw new EntityNotFoundException("User with such email is already exist!");
          }
          person.setPassword(bCryptPasswordEncoder.encode(person.getPassword()));
          personRepository.save(person);
          // TODO: send verification URL
//           String token = JwtProvider.createToken(registrationApi.getEmail(), null);
//           String domain = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest().getServerName();
//           String URL = "http://" + domain + "/" + token;
//           mailServer.sendMail(new Mail("sn@sn.danya", registrationApi.getEmail(), "Verify your account", "asddaadfafa\n\n"+URL));
      } else {
          throw new EntityNotFoundException("Password mismatched");
      }
    return new ResponseApi<>("User successfully added",
        new ReportApi("Ok"));

  }

  public AbstractResponse changePassword(String token, String password) {
    Person person = getCurrentPersonByToken(token);
    person.setPassword(password);
    personRepository.save(person);
    return new ResponseApi<>("Your password was successfully changed",
        new ReportApi("Ok"));
  }

  public AbstractResponse changeEmail(String token, String newEmail) {
    Person person = getCurrentPersonByToken(token);
    person.setEmail(newEmail);
    personRepository.save(person);
    return new ResponseApi<>("Your email was successfully changed",
        new ReportApi("Ok"));//personMapper.personToPersonApi(person);
  }

  public AbstractResponse changeNotifications(String token,
      RequestNotificationSettingsApi notificationSettingsApi) {
    Person person = getCurrentPersonByToken(token);
    List<Notification> listNotifications = person.getNotifications();
    for (int i = 0; i < listNotifications.size(); i++) {
      if (listNotifications.get(i).getNotificationType()
          .equals(notificationSettingsApi.getNotificationType())) {
        listNotifications.get(i).setEnable(notificationSettingsApi.isEnable());
      }
    }
    person.setNotifications(listNotifications);
    personRepository.save(person);

    return new ReportApi("Ok");
  }

  private Person getCurrentPersonByToken(String token) {
    String email = JwtProvider.getUsername(token);
    return personRepository.findByEmail(email)
        .orElseThrow(() -> new EntityNotFoundException("No such user in Person table"));
  }
}

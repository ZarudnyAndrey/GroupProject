package com.skillbox.sw.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.skillbox.sw.domain.enums.MessagesPermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponsePersonApi extends AbstractResponse {

  private int id;

  @JsonProperty("first_name")
  private String firstName;

  @JsonProperty("last_name")
  private String lastName;

  @JsonProperty("reg_date")
  private long regDate;

  @JsonProperty("birth_date")
  private long birthDate;

  private String email;
  private String phone;
  private String photo;
  private String about;
  private String town;

  @JsonProperty("messages_permission")
  private MessagesPermission messagesPermission;

  @EqualsAndHashCode.Exclude
  @JsonProperty("last_online_time")
  private long lastOnlineTime;

  @JsonProperty("is_blocked")
  private boolean isBlocked;

  private String token;
}
package com.skillbox.sw.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageIdApi extends AbstractResponse {

  @JsonProperty("message_id")
  private int messageId;
}
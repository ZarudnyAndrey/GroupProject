package com.skillbox.sw.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseDialogUsersApi extends AbstractResponse {

  @JsonProperty("user_ids")
  private List<Integer> userIds;
}
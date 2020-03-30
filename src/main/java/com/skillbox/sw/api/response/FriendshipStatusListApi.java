package com.skillbox.sw.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendshipStatusListApi extends AbstractResponse {

  @JsonProperty("data")
  private List<FriendshipStatusApi> friendshipStatusList;
}
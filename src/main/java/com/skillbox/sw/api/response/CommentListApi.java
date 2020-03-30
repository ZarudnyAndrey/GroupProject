package com.skillbox.sw.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentListApi extends ResponseApi {

  @JsonProperty("data")
  private List<CommentApi> commentList;

  private int total;
  private int offset;
  private int perPage;
}
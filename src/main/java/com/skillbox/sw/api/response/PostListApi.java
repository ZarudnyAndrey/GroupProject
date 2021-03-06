package com.skillbox.sw.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostListApi extends ResponseApi {

  @JsonProperty("data")
  private List<ResponsePostApi> postList;
  private long total;
  private int offset;
  private int perPage;

  public PostListApi(String error, List<ResponsePostApi> postList, int total, int offset,
      int perPage) {
    super(error);
    this.postList = postList;
    this.total = total;
    this.offset = offset;
    this.perPage = perPage;
  }
}
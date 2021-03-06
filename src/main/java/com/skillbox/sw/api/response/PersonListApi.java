package com.skillbox.sw.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonListApi extends ResponseApi {

  @JsonProperty("data")
  private List<ResponsePersonApi> personList;
  private long total;
  private long offset;
  private int perPage;

  public PersonListApi(String error, List<ResponsePersonApi> personList, long total, long offset,
      int perPage) {
    super(error);
    this.personList = personList;
    this.total = total;
    this.offset = offset;
    this.perPage = perPage;
  }
}
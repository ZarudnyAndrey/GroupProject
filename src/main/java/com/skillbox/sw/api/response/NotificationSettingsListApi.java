package com.skillbox.sw.api.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationSettingsListApi extends AbstractResponse {

  private String error;
  private long timestamp;
  private List<ResponseNotificationSettingsApi> data;
}
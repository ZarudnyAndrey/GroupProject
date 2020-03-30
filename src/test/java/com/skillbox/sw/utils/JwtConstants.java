package com.skillbox.sw.utils;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import org.springframework.http.HttpHeaders;

public class JwtConstants {
  public final static String EMAIL = "tarakan@mail.ru";
  public final static String GOOD_TOKEN = "skillboxeyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0YXJha2FuQG1haWwucnUiLCJleHAiOjE4OTM0NTYwMDF9.pfKwDjDRMadSuPRPFVaHx35-xersY0GL2UdPmZEjNSpkuX_9EOWGQ5C3e7aP6dBg5bixKCxeLobvX2iXgoCFhg";
  public final static String GOOD_TOKEN_ID6 = "skillboxeyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJTaGVyaXNlTG9ndWVAZXhhbXBsZS5jb20iLCJleHAiOjE4OTM0NTYwMDF9.y46Xw72l5Efk6clEzHY57owNdTrNLXLaWxx-NYyxHSwN--Q7FTzWOvtR5MNFyUEtl-UoW37ED5nJv8ZSal124w";
  public final static String BAD_TOKEN = "1skillboxeyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0YXJha2FuQG1haWwucnUiLCJleHAiOjE4OTM0NTYwMDF9.D2b_r7jpqAw7CrQOJDkFneVvLLx9l5WrFk1j9v_OIc0uqUJL_ZkuukcToJ19EznuZIcCmjZpSF4pXin0Co4WtAeyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0YXJha2FuQG1haWwucnUiLCJleHAiOjE4OTM0NTYwMDF9.D2b_r7jpqAw7CrQOJDkFneVvLLx9l5WrFk1j9v_OIc0uqUJL_ZkuukcToJ19EznuZIcCmjZpSF4pXin0Co4WtA";

  public static HttpHeaders getHeaders(String auth) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(AUTHORIZATION, auth);
    return headers;
  }
}
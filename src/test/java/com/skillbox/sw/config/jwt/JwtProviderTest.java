package com.skillbox.sw.config.jwt;

import com.skillbox.sw.utils.JwtConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class JwtProviderTest {

  @Test
  void getUsername() {
    String email = JwtProvider.getUsername(JwtConstants.GOOD_TOKEN);
    Assertions.assertEquals(JwtConstants.EMAIL, email);
  }

  @Test
  void createTokenAndGetUserName() {
    String username = "Jack Sparrow";
    String token = JwtProvider.createToken(username);
    String usernameFromToken = JwtProvider.getUsername(token);
    Assertions.assertEquals(username, usernameFromToken);
  }

  @Test
  void validateGoodToken() {
    boolean isValid = JwtProvider.validateToken(JwtConstants.GOOD_TOKEN);
    Assertions.assertTrue(isValid);
  }

  @Test
  void validateBadToken() {
    boolean isValid = JwtProvider.validateToken(JwtConstants.BAD_TOKEN);
    Assertions.assertFalse(isValid);
  }
}
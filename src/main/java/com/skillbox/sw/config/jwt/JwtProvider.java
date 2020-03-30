package com.skillbox.sw.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.skillbox.sw.config.SecurityConstants;
import java.util.Date;

public class JwtProvider {

  private static Algorithm algorithm = Algorithm.HMAC512(SecurityConstants.SECRET.getBytes());

  public static String getUsername(String token) {
    return JWT.require(algorithm).build().verify(token.replace(SecurityConstants.PREFIX, ""))
        .getSubject();
  }

  public static String createToken(String username) {
    return JWT.create()
        .withSubject(username)
        .withExpiresAt(
            new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME_IN_MILLIS))
        .sign(algorithm);
  }

  public static boolean validateToken(String token) {
    try {
      JWTVerifier verifier = JWT.require(algorithm)
          .build();
      verifier.verify(token.replace(SecurityConstants.PREFIX, ""));
      return true;
    } catch (JWTDecodeException | TokenExpiredException | AlgorithmMismatchException e) {
      return false;
    }
  }
}
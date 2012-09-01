package com.readmill.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

@SuppressWarnings("UnusedDeclaration")
public class Token implements Serializable {
  private static final long serialVersionUID = 766168501082045382L;

  // JSON object keys
  public static final String KEY_ACCESS_TOKEN = "access_token";
  public static final String KEY_REFRESH_TOKEN = "refresh_token";
  public static final String KEY_EXPIRES_IN = "expires_in";
  public static final String KEY_SCOPE = "scope";

  private String accessToken, refreshToken, scope;
  private long expiresIn;

  /**
   * Constructs a new token with an explicit access-, and refresh token
   *
   * @param accessToken  Access token
   * @param refreshToken Refresh token
   * @param scope        Scope of the token
   */
  public Token(String accessToken, String refreshToken, String scope) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.scope = scope;
  }

  /**
   * Constructs a new non-expiring token.
   * Non expiring tokens does not have a refresh token.
   *
   * @param accessToken The access token
   */
  public Token(String accessToken) {
    this.accessToken = accessToken;
    this.refreshToken = "";
    this.scope = "non-expiring";
  }

  /**
   * Constructs a token from a JSON response.
   *
   * @param json JSON response
   * @throws org.json.JSONException if the given json object is not properly
   *                                formatted as a token
   */
  public Token(JSONObject json) throws JSONException {
    accessToken = json.getString(KEY_ACCESS_TOKEN);
    refreshToken = json.optString(KEY_REFRESH_TOKEN); // refresh token is optional
    scope = json.getString(KEY_SCOPE);
    expiresIn = json.getLong(KEY_EXPIRES_IN);
  }

  /**
   * Gets the access token.
   *
   * @return The Access token
   */
  public String getAccessToken() {
    return accessToken;
  }

  /**
   * Gets the refresh token.
   *
   * @return The refresh token
   */
  public String getRefreshToken() {
    return refreshToken;
  }

  /**
   * Gets the scope.
   *
   * @return The scope
   */
  public String getScope() {
    return scope;
  }

  /**
   * Gets the expires in value.
   *
   * @return the expiration time (based on when the token was created)
   */
  public long getExpiresIn() {
    return expiresIn;
  }

  /**
   * Invalidates the access token
   */
  public void invalidate() {
    this.accessToken = null;
  }

  /**
   * Checks if the token is valid.
   * <p/>
   * Note that this can give false positives, as the token might have been
   * invalidated on the server.
   *
   * @return Token validity
   */
  public boolean isValid() {
    return accessToken != null && refreshToken != null;
  }

  @Override
  public String toString() {
    return "Token{" +
        "access token='" + accessToken + '\'' +
        ", refresh token='" + refreshToken + '\'' +
        ", scope='" + scope + '\'' +
        ", expires in=" + expiresIn +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(o == null) return false;

    if(o instanceof String) {
      return o.equals(accessToken);
    } else if(o instanceof Token) {
      Token otherToken = (Token) o;
      if(accessToken == null ? otherToken.accessToken != null : !accessToken.equals(otherToken.accessToken))
        return false;
      if(refreshToken == null ? otherToken.refreshToken != null : !refreshToken.equals(otherToken.refreshToken))
        return false;
      //noinspection RedundantIfStatement
      if(scope == null ? otherToken.scope != null : !scope.equals(otherToken.scope))
        return false;
      return true;
    } else {
      return super.equals(o);
    }
  }

  @Override
  public int hashCode() {
    int result = accessToken != null ? accessToken.hashCode() : 0;
    result = 31 * result + (refreshToken != null ? refreshToken.hashCode() : 0);
    result = 31 * result + (scope != null ? scope.hashCode() : 0);
    return result;
  }
}

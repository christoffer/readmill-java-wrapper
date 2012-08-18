package com.readmill.api;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class TokenTest {

  @Test
  public void constructFromStrings() {
    Token token = new Token("this is access token", "this is refresh token", "this is scope");
    assertThat(token.getAccessToken(), is("this is access token"));
    assertThat(token.getRefreshToken(), is("this is refresh token"));
    assertThat(token.getScope(), is("this is scope"));
    assertThat(token.getExpiresIn(), is(0L));
  }

  @Test
  public void constructFromValidJson() throws JSONException, IOException {
    String validJson = "{\n" +
        "\"access_token\": \"04u7h-4cc355-70k3n\",\n" +
        "\"expires_in\": 3600,\n" +
        "\"scope\": \"non-expiring\",\n" +
        "\"refresh_token\": \"04u7h-r3fr35h-70k3n\"\n" +
        "}";

    JSONTokener tokener = new JSONTokener(validJson);
    JSONObject json = new JSONObject(tokener);
    Token token = new Token(json);

    assertThat(token.getAccessToken(), is("04u7h-4cc355-70k3n"));
    assertThat(token.getRefreshToken(), is("04u7h-r3fr35h-70k3n"));
    assertThat(token.getScope(), is("non-expiring"));
    assertThat(token.getExpiresIn(), is(3600L));
  }

  @Test
      (expected = IOException.class)
  public void throwsWhenConstructedFromInvalidJson() throws Exception {
    new Token(new JSONObject());
  }

  @Test
  public void invalidate() {
    Token token = new Token("valid_access", "valid_refresh", "");
    assertThat(token.getAccessToken(), is("valid_access"));
    assertThat(token.getRefreshToken(), is("valid_refresh"));
    assertThat(token.isValid(), is(true));
    token.invalidate();

    assertThat(token.getAccessToken(), is(nullValue()));
    assertThat(token.getRefreshToken(), is("valid_refresh"));
    assertThat(token.isValid(), is(false));
  }

  @SuppressWarnings("EqualsBetweenInconvertibleTypes")
  @Test
  public void equality() {
    Token base = new Token("access", "refresh", "scope");
    Token wrongAccess = new Token("haxess", "refresh", "scope");
    Token wrongRefresh = new Token("access", "unfresh", "scope");
    Token wrongScope = new Token("access", "refresh", "slope");
    Token same = new Token("access", "refresh", "scope");

    assertThat(base, is(equalTo(same)));
    assertThat(base, is(equalTo(base)));

    assertThat(base.equals("access"), is(true));
    assertThat(base.equals("haxess"), is(false));

    assertThat(base, is(not(equalTo(null))));
    assertThat(base, is(not(equalTo(wrongAccess))));
    assertThat(base, is(not(equalTo(wrongRefresh))));
    assertThat(base, is(not(equalTo(wrongScope))));
  }
}

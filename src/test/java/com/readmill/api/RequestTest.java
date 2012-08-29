package com.readmill.api;

import org.apache.http.client.methods.*;

import org.junit.Test;
import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.*;

public class RequestTest {

  @Test
  public void requestToUrl() {
    Request request = Request.to("/users");
    assertThat(request.toUrl(), is("/v2/users"));

    request = Request.to("/users/%d/%s", 9001, "readings");
    assertThat(request.toUrl(), is("/v2/users/9001/readings"));
  }

  @Test
  public void requestToWithParams() {
    Request request = Request.to("/readings")
        .withParams("reading[progress]", 14.5f, "private", false);
    assertThat(request.toUrl(), is("/v2/readings?reading%5Bprogress%5D=14.5&private=false"));
  }

  @Test
      (expected = IllegalArgumentException.class)
  public void requestToWithParamsRaisesWhenUnEvenArguments() {
    Request.to("/users").withParams("user_name");
  }

  @Test
  public void usingToken() {
    Token token = new Token("my_access", "my_refresh", "*");
    Request request = Request.to("/protected/resource");

    assertThat(request.build(HttpPost.class).getFirstHeader("Authorization"), is(nullValue()));
    request.usingToken(token);
    assertThat(request.build(HttpGet.class).getFirstHeader("Authorization").getValue(), is("OAuth my_access"));
  }

  @Test
  public void buildEnclosedRequestWithParameters() {
    // Sets the plain resource URI and header for enclosed requests (POST, PUT etc)
    HttpPut putRequestWithParams = Request.to("/readings")
        .withParams("username", "christoffer", "reading[id]", 405)
        .build(HttpPut.class);
    assertThat(putRequestWithParams.getURI().toString(), is("/v2/readings"));
    assertThat(putRequestWithParams.getFirstHeader("Content-Type").getValue(), is("application/x-www-form-urlencoded"));
    assertThat(putRequestWithParams.getEntity(), is(not(nullValue())));
  }

  @Test
  public void buildEnclosedRequestWithoutParameters() {
    // Does not enclose params and set header when empty
    HttpPut putRequest = Request.to("/readings").build(HttpPut.class);
    assertThat(putRequest.getURI().toString(), is("/v2/readings"));
    assertThat(putRequest.getEntity(), is(nullValue()));
  }

  @Test
  public void buildNonEnclosedRequest() {
    HttpGet getRequest = Request.to("/readings")
        .withParams("username", "christoffer", "private", true)
        .build(HttpGet.class);
    assertThat(getRequest.getURI().toString(), is("/v2/readings?username=christoffer&private=true"));
    assertThat(getRequest.getFirstHeader("Content-Type"), is(nullValue()));
  }

  @Test
  public void toUrl() {
    assertThat(Request.to("/users").toUrl(), is("/v2/users"));
    Request request = Request.to("/users").withParams("username", "christoffer", "private", false);
    assertThat(request.toUrl(), is("/v2/users?username=christoffer&private=false"));
  }

  @Test
  public void toUrlWithAbsoluteResource() {
    assertThat(Request.to("http://readmill.com/users").toUrl(), is("http://readmill.com/users"));
  }

  @Test
  public void queryString() {
    assertThat(Request.to("/users").queryString(), is(""));
    assertThat(Request.to("/users").withParams("user[username]", "christoffer", "private", false).queryString(), is("user%5Busername%5D=christoffer&private=false"));
  }

  @Test
  public void equality() {
    Request first = Request.to("/some/endpoint");
    Request second = Request.to("/some/endpoint");
    Request different = Request.to("/some/other/endpoint");
    assertThat(first.equals(second), is(true));
    assertThat(second.equals(first), is(true));

    assertThat(first.equals(different), is(false));
    assertThat(different.equals(first), is(false));

    assertThat(first.equals(4), is(false));
  }
}

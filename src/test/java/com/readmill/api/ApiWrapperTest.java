package com.readmill.api;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ApiWrapperTest {
  private ApiWrapper mWrapper;

  @Before
  public void createWrapperInstance() {
    Environment testEnvironment = new Environment("api.example.com", "www.example.com", false);
    mWrapper = new ApiWrapper("my_client_id", "my_client_secret", testEnvironment);
  }

  @Test
  public void basicGetters() {
    assertThat(mWrapper.getClientId(), is("my_client_id"));
    assertThat(mWrapper.getClientSecret(), is("my_client_secret"));
    assertThat(mWrapper.getEnvironment(), is(not(nullValue())));
  }

  @Test
  public void token() {
    assertThat(mWrapper.getToken(), is(nullValue()));
    Token myToken = new Token("haxxess", "refreshorz", "*");
    mWrapper.setToken(myToken);
    assertThat(mWrapper.getToken(), is(myToken));
  }

  @Test
  public void executeHasCorrectHost() throws IOException {
    HttpClient httpClient = stubbedHttpClient();
    ArgumentCaptor<HttpHost> targetArgument = ArgumentCaptor.forClass(HttpHost.class);

    mWrapper.execute(Request.to("/users/1"), HttpGet.class);

    Mockito.verify(httpClient).execute(targetArgument.capture(), Mockito.any(HttpGet.class));

    assertThat(targetArgument.getValue().getHostName(), is("api.example.com"));
    assertThat(targetArgument.getValue().getSchemeName(), is("http"));
  }

  @Test
  public void executeHasCorrectRequest() throws IOException {
    HttpClient httpClient = stubbedHttpClient();
    ArgumentCaptor<HttpGet> requestArgument = ArgumentCaptor.forClass(HttpGet.class);

    mWrapper.execute(Request.to("/users/1"), HttpGet.class);

    Mockito.verify(httpClient).execute(Mockito.any(HttpHost.class), requestArgument.capture());

    assertThat(requestArgument.getValue().getMethod(), is("GET"));
    assertThat(requestArgument.getValue().getURI().toString(), startsWith("/v2/users/1"));
  }

  @Test
  public void executeAuthorizesRequestsWithClientId() throws IOException {
    HttpClient httpClient = stubbedHttpClient();
    ArgumentCaptor<HttpGet> requestArgument = ArgumentCaptor.forClass(HttpGet.class);

    mWrapper.execute(Request.to("/users/1"), HttpGet.class);
    Mockito.verify(httpClient).execute(Mockito.any(HttpHost.class), requestArgument.capture());

    assertThat(requestArgument.getValue().getURI().toString(), containsString("client_id=my_client_id"));
  }

  @Test
  public void executeAuthorizesRequestsWithRequestToken() throws IOException {
    HttpClient httpClient = stubbedHttpClient();
    ArgumentCaptor<HttpGet> requestArgument = ArgumentCaptor.forClass(HttpGet.class);

    Token wrapperToken = new Token("wrapper_token");
    Token requestToken = new Token("request_token");

    mWrapper.setToken(wrapperToken);
    mWrapper.execute(Request.to("/users/1").usingToken(requestToken), HttpGet.class);
    Mockito.verify(httpClient).execute(Mockito.any(HttpHost.class), requestArgument.capture());

    // Should not pollute with client id when token is present
    assertThat(requestArgument.getValue().getURI().toString(), not(containsString("client_id=my_client_id")));

    // Should include preferred token
    assertThat(extractHeader(requestArgument, "Authorization"), is("OAuth request_token"));
  }

  @Test
  public void executeAuthorizesRequestsWithWrapperToken() throws IOException {
    HttpClient httpClient = stubbedHttpClient();
    ArgumentCaptor<HttpGet> requestArgument = ArgumentCaptor.forClass(HttpGet.class);

    Token wrapperToken = new Token("wrapper_token");

    mWrapper.setToken(wrapperToken);
    mWrapper.execute(Request.to("/users/1"), HttpGet.class);
    Mockito.verify(httpClient).execute(Mockito.any(HttpHost.class), requestArgument.capture());

    assertThat(requestArgument.getValue().getURI().toString(), not(containsString("client_id=my_client_id")));
    assertThat(extractHeader(requestArgument, "Authorization"), is("OAuth wrapper_token"));
  }

  // Helpers

  private HttpClient stubbedHttpClient() {
    HttpClient httpClient = Mockito.mock(HttpClient.class);
    mWrapper = Mockito.spy(mWrapper);
    Mockito.when(mWrapper.getHttpClient()).thenReturn(httpClient);
    return httpClient;
  }

  // Pull out a header from a captured http request argument
  private String extractHeader(ArgumentCaptor<? extends HttpRequest> captor, String headerName) {
    Header[] headers = captor.getValue().getHeaders(headerName);
    assertThat(headers.length, is(1));
    return headers[0].getValue();
  }
}

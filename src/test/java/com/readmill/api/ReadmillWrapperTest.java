package com.readmill.api;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ReadmillWrapperTest {
  private ReadmillWrapper mWrapper;

  @Before
  public void createWrapperInstance() {
    Environment testEnvironment = new Environment("api.example.com", "www.example.com", true);
    mWrapper = new ReadmillWrapper("my_client_id", "my_client_secret", testEnvironment);
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
    assertThat(targetArgument.getValue().getSchemeName(), is("https"));
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

  @Test
  public void getAuthorizationURL() throws MalformedURLException {
    URI redirectURI = URI.create("http://wrappertest.com/callback");
    mWrapper.setRedirectURI(redirectURI);
    URL authorizeURL = mWrapper.getAuthorizationURL();

    assertThat(authorizeURL.toString(), startsWith("https://www.example.com/oauth/authorize"));

    assertThat(authorizeURL.getQuery(), containsString("client_id=my_client_id"));
    assertThat(authorizeURL.getQuery(), containsString("redirect_uri=" + redirectURI));
    assertThat(authorizeURL.getQuery(), containsString("response_type=code"));
    assertThat(authorizeURL.getQuery(), containsString("response_type=code"));
    assertThat(authorizeURL.getQuery(), not(containsString("scope=")));
  }

  @Test
  public void getAuthorizationURLWithScope() throws MalformedURLException {
    URI redirectURI = URI.create("http://wrappertest.com/callback");
    mWrapper.setRedirectURI(redirectURI);
    mWrapper.setScope("non-expiring");
    URL authorizeURL = mWrapper.getAuthorizationURL();

    assertThat(authorizeURL.getQuery(), containsString("scope=non-expiring"));
  }

  @Test
  public void obtainTokenThrows() throws IOException, JSONException {
    URI redirectURI = URI.create("http://wrappertest.com/callback");
    mWrapper.setRedirectURI(redirectURI);

    String tokenJSON = "{" +
        "\"access_token\":  \"04u7h-4cc355-70k3n\"," +
        "\"expires_in\":    3600," +
        "\"scope\":         \"\"," +
        "\"refresh_token\": \"04u7h-r3fr35h-70k3n\"" +
        "}";

    ArgumentCaptor<Request> requestArgument = ArgumentCaptor.forClass(Request.class);
    mWrapper = Mockito.spy(mWrapper);
    Mockito.doReturn(tokenJSON).when(mWrapper).getResponseText(requestArgument.capture(), Mockito.eq(HttpPost.class));

    Token obtainedToken = mWrapper.obtainTokenOrThrow("authcode2000");

    URI sentRequest = URI.create(requestArgument.getValue().toUrl());

    // Assert that request was sent to correct place
    assertThat(sentRequest.toString(), startsWith("https://www.example.com"));
    assertThat(sentRequest.getPath(), is("/oauth/token"));

    // ... with correct parameters
    assertThat(sentRequest.getQuery(), containsString("grant_type=authorization_code"));
    assertThat(sentRequest.getQuery(), containsString("client_id=my_client_id"));
    assertThat(sentRequest.getQuery(), containsString("client_secret=my_client_secret"));
    assertThat(sentRequest.getQuery(), containsString("code=authcode2000"));
    assertThat(sentRequest.getQuery(), containsString("redirect_uri=http://wrappertest.com/callback"));
    assertThat(sentRequest.getQuery(), not(containsString("scope=")));

    // ... and got correct token
    Token expectedToken = new Token(new JSONObject(tokenJSON));

    assertThat(obtainedToken, equalTo(expectedToken));
  }

  @Test
  public void obtainTokenThrowsWithScope() throws IOException, JSONException {
    String tokenJSON = "{" +
        "\"access_token\":  \"04u7h-4cc355-70k3n\"," +
        "\"expires_in\":    3600," +
        "\"scope\":         \"\"," +
        "\"refresh_token\": \"04u7h-r3fr35h-70k3n\"" +
        "}";

    URI redirectURI = URI.create("http://wrappertest.com/callback");
    mWrapper.setRedirectURI(redirectURI);
    mWrapper.setScope("non-expiring");
    ArgumentCaptor<Request> requestArgument = ArgumentCaptor.forClass(Request.class);
    mWrapper = Mockito.spy(mWrapper);
    Mockito.doReturn(tokenJSON).when(mWrapper).getResponseText(requestArgument.capture(), Mockito.eq(HttpPost.class));

    mWrapper.obtainTokenOrThrow("my-code");

    URI sentRequest = URI.create(requestArgument.getValue().toUrl());
    assertThat(sentRequest.getQuery(), containsString("scope=non-expiring"));
  }
  
  @Test
  public void obtainTokenLogin() throws IOException, JSONException {

	    String tokenJSON = "{" +
	        "\"access_token\":  \"04u7h-4cc355-70k3n\"," +
	        "\"expires_in\":    3600," +
	        "\"scope\":         \"\"," +
	        "\"refresh_token\": \"04u7h-r3fr35h-70k3n\"" +
	        "}";

	    ArgumentCaptor<Request> requestArgument = ArgumentCaptor.forClass(Request.class);
	    mWrapper = Mockito.spy(mWrapper);
	    Mockito.doReturn(tokenJSON).when(mWrapper).getResponseText(requestArgument.capture(), Mockito.eq(HttpPost.class));

	    Token obtainedToken = mWrapper.login("testusername", "testpassword");

	    URI sentRequest = URI.create(requestArgument.getValue().toUrl());

	    // Assert that request was sent to correct place
	    assertThat(sentRequest.toString(), startsWith("https://www.example.com"));
	    assertThat(sentRequest.getPath(), is("/oauth/token"));

	    // ... with correct parameters
	    assertThat(sentRequest.getQuery(), containsString("grant_type=password"));	    
	    assertThat(sentRequest.getQuery(), containsString("client_id=my_client_id"));
	    assertThat(sentRequest.getQuery(), containsString("client_secret=my_client_secret"));
	    assertThat(sentRequest.getQuery(), containsString("username=testusername"));
	    assertThat(sentRequest.getQuery(), containsString("password=testpassword"));
	    assertThat(sentRequest.getQuery(), not(containsString("scope=")));

	    // ... and got correct token
	    Token expectedToken = new Token(new JSONObject(tokenJSON));

	    assertThat(obtainedToken, equalTo(expectedToken));
	  }

  @Test
  public void get() {
    RequestBuilder builder = mWrapper.get("/endpoint");
    assertThat(builder.getRequestBaseClass().getName(), is(HttpGet.class.getName()));
    assertThat(builder.getRequest().toUrl(), containsString("/endpoint"));
  }

  @Test
  public void put() {
    RequestBuilder builder = mWrapper.put("/endpoint");
    assertThat(builder.getRequestBaseClass().getName(), is(HttpPut.class.getName()));
    assertThat(builder.getRequest().toUrl(), containsString("/endpoint"));
  }

  @Test
  public void post() {
    RequestBuilder builder = mWrapper.post("/endpoint");
    assertThat(builder.getRequestBaseClass().getName(), is(HttpPost.class.getName()));
    assertThat(builder.getRequest().toUrl(), containsString("/endpoint"));
  }

  @Test
  public void delete() {
    RequestBuilder builder = mWrapper.delete("/endpoint");
    assertThat(builder.getRequestBaseClass().getName(), is(HttpDelete.class.getName()));
    assertThat(builder.getRequest().toUrl(), containsString("/endpoint"));
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

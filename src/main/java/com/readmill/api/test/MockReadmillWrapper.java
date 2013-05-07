package com.readmill.api.test;

import com.readmill.api.Environment;
import com.readmill.api.ReadmillWrapper;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;

/**
 * Mockable wrapper for use in tests
 */
public class MockReadmillWrapper extends ReadmillWrapper {
  private MockHttpClient mMockHttpClient = new MockHttpClient();

  /**
   * Empty constructor provided for ease of use.
   */
  public MockReadmillWrapper() {
    this("mocked-client-id", "mocked-client-secret",
      new Environment("api.example.com", "web.example.com", false)
    );
  }

  public MockReadmillWrapper(String clientId, String clientSecret, Environment env) {
    super(clientId, clientSecret, env);
  }

  @Override
  public HttpClient createHttpClient() {
    return mMockHttpClient;
  }

  /**
   * Mock the response body coming from the server.
   *
   * @param responseText The mocked text in the response body
   */
  public void respondWithText(String responseText) {
    mMockHttpClient.respondWithText(responseText);
  }

  /**
   * Mock the response body and the status code.
   */
  public void respondWithStatusAndText(int httpStatus, String responseText) {
    mMockHttpClient.respondWithStatusAndText(httpStatus, responseText);
  }

  /**
   * Raise an IOException when attempting to make a request.
   */
  public void respondWithIOException() {
    mMockHttpClient.respondWithIOException();
  }

  /**
   * Access of the last made http request
   */
  public HttpRequest getLastRequest() {
    return mMockHttpClient.getLastRequest();
  }
}

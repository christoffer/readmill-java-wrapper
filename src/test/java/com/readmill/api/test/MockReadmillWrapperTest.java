package com.readmill.api.test;

import com.readmill.api.HttpUtils;
import com.readmill.api.Request;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

import static junit.framework.Assert.*;

public class MockReadmillWrapperTest {
  private MockReadmillWrapper mMockWrapper;

  @Before
  public void setUp() {
    mMockWrapper = new MockReadmillWrapper();
  }

  @Test
  public void mockText() throws JSONException {
    mMockWrapper.respondWithText("{ \"unicorn\": 42 }");
    JSONObject response = mMockWrapper.get("/irrelevant/endpoint").fetch();
    assertEquals(42, response.getInt("unicorn"));
  }

  @Test
  public void mockStatusAndText() throws IOException {
    mMockWrapper.respondWithStatusAndText(HttpStatus.SC_PAYMENT_REQUIRED, "hello");
    HttpResponse response = mMockWrapper.get(Request.to("/irrelevant/endpoint"));

    assertEquals(HttpStatus.SC_PAYMENT_REQUIRED, response.getStatusLine().getStatusCode());
    assertEquals("hello", EntityUtils.toString(response.getEntity()));
  }

  @Test
  public void mockIOException() throws JSONException {
    mMockWrapper.respondWithIOException();
    try {
      mMockWrapper.get("/irrelevant/endpoint").sendOrThrow();
      fail("Expected method to raise IOException, but it was not raised");
    } catch (IOException expected) {
      // Succeed
    }
  }

  @Test
  public void getLastRequest() throws IOException {
    assertNull(mMockWrapper.getLastRequest());
    mMockWrapper.get(Request.to("/unicorns").withParams("cute", "and fierce"));

    HttpRequest request = mMockWrapper.getLastRequest();
    assertNotNull(request);

    URI requestUri = URI.create(request.getRequestLine().getUri());
    assertEquals("/v2/unicorns", requestUri.getPath());
    assertTrue(requestUri.getQuery().contains("cute=and+fierce"));
  }
}

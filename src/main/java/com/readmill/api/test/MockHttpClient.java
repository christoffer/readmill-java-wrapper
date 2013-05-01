package com.readmill.api.test;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * Mockable HttpClient
 */
public class MockHttpClient implements HttpClient {
  private HttpResponse mCannedResponse = createMockResponse(200);
  private boolean mRaiseIOException = false;

  @Override
  public HttpParams getParams() {
    return null;
  }

  @Override
  public ClientConnectionManager getConnectionManager() {
    return null;
  }

  @Override
  public HttpResponse execute(HttpUriRequest httpUriRequest) throws IOException, ClientProtocolException {
    return mockedResponse();
  }

  @Override
  public HttpResponse execute(HttpUriRequest httpUriRequest, HttpContext httpContext) throws IOException, ClientProtocolException {
    return mockedResponse();
  }

  @Override
  public HttpResponse execute(HttpHost httpHost, HttpRequest httpRequest) throws IOException, ClientProtocolException {
    return mockedResponse();
  }

  @Override
  public HttpResponse execute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext) throws IOException, ClientProtocolException {
    return mockedResponse();
  }

  // Generic execute methods not currently supported

  @Override
  public <T> T execute(HttpUriRequest httpUriRequest, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T execute(HttpUriRequest httpUriRequest, ResponseHandler<? extends T> responseHandler, HttpContext httpContext) throws IOException, ClientProtocolException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T execute(HttpHost httpHost, HttpRequest httpRequest, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T execute(HttpHost httpHost, HttpRequest httpRequest, ResponseHandler<? extends T> responseHandler, HttpContext httpContext) throws IOException, ClientProtocolException {
    throw new NotImplementedException();
  }

  /**
   * Creates a mocked http response, answering with a 200 (OK) and a given text.
   *
   * @param responseText text body of the response
   */
  public void respondWithText(String responseText) {
    respondWithStatusAndText(200, responseText);
  }

  /**
   * Creates a mocked http response, answering with a 200 (OK) and a given text.
   *
   * @param responseText text body of the response
   */
  public void respondWithStatusAndText(int httpStatus, String responseText) {
    HttpResponse resp = createMockResponse(httpStatus);
    resp.setEntity(getStringEntity(responseText));
    mCannedResponse = resp;
    mRaiseIOException = false;
  }

  /**
   * Raises an IOException when a request is made
   * TODO: Extend this to accept any Exception class
   */
  public void respondWithIOException() {
    mRaiseIOException = true;
  }

  /**
   * Creates a fake http response with the given status code.
   * Response phrase is taken from the standard english catalog.
   *
   * @param statusCode Status code for the fake response.
   */
  public static HttpResponse createMockResponse(int statusCode) {
    String reasonPhrase = EnglishReasonPhraseCatalog.INSTANCE.getReason(statusCode, Locale.ENGLISH);
    return new BasicHttpResponse(HttpVersion.HTTP_1_1, statusCode, reasonPhrase);
  }

  /**
   * Creates a fake http response with the given status code and body.
   * Response phrase is taken from the standard english catalog.
   *
   * @param statusCode Status code for the fake response.
   */
  public static HttpResponse createMockResponse(int statusCode, String responseText) {
    String reasonPhrase = EnglishReasonPhraseCatalog.INSTANCE.getReason(statusCode, Locale.ENGLISH);
    BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, statusCode, reasonPhrase);
    response.setEntity(getStringEntity(responseText));
    return response;
  }

  /**
   * Creates a simple StringEntity with the given text.
   */
  private static HttpEntity getStringEntity(String responseText) {
    try {
      return new StringEntity(responseText);
    } catch(UnsupportedEncodingException e) {
      throw new RuntimeException("Failed to create a http string response entity", e);
    }
  }

  private HttpResponse mockedResponse() throws IOException {
    if(mRaiseIOException) {
      throw new IOException();
    }
    return mCannedResponse;
  }
}

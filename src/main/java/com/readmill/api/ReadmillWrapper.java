package com.readmill.api;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.net.URI;

public class ReadmillWrapper {
  private String mClientId;
  private String mClientSecret;
  private Environment mEnvironment;
  private Token mToken;
  private HttpClient mHttpClient;

  /**
   * Create a wrapper for a given client and environment.
   *
   * @param clientId     Client Identifier
   * @param clientSecret Client secret
   * @param env          Server environment
   */

  public ReadmillWrapper(String clientId, String clientSecret, Environment env) {
    mClientId = clientId;
    mClientSecret = clientSecret;
    mEnvironment = env;
  }

  public String getClientId() {
    return mClientId;
  }

  public String getClientSecret() {
    return mClientSecret;
  }

  public Environment getEnvironment() {
    return mEnvironment;
  }

  public Token getToken() {
    return mToken;
  }

  public void setToken(Token token) {
    mToken = token;
  }

  public HttpClient getHttpClient() {
    if(mHttpClient == null) {
      HttpParams httpParams = new BasicHttpParams();

      Scheme httpScheme = new Scheme("http", PlainSocketFactory.getSocketFactory(), 80);
      Scheme httpsScheme = new Scheme("https", SSLSocketFactory.getSocketFactory(), 443);

      final SchemeRegistry schemeRegistry = new SchemeRegistry();
      schemeRegistry.register(httpScheme);
      schemeRegistry.register(httpsScheme);

      mHttpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, schemeRegistry), httpParams);
    }
    return mHttpClient;
  }

  public HttpResponse get(Request request) {
    return execute(request, HttpGet.class);
  }

  public HttpResponse post(Request request) {
    return execute(request, HttpPost.class);
  }

  public HttpResponse delete(Request request) {
    return execute(request, HttpDelete.class);
  }

  public HttpResponse put(Request request) {
    return execute(request, HttpPut.class);
  }

  /**
   * Protected *
   */

  protected HttpResponse execute(Request request, Class<? extends HttpRequestBase> klass) {
    try {
      authorizeRequest(request);
      return getHttpClient().execute(resolveTarget(request), request.build(klass));
    } catch(IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  protected Request authorizeRequest(Request request) {
    // Include any available token
    if(mToken != null && request.getToken() == null) {
      request.usingToken(mToken);
    }

    // Fall back to authorization with client id when missing a token
    if(request.getToken() == null && mClientId != null) {
      request.withParams("client_id", mClientId);
    }

    return request;
  }

  /**
   * Private *
   */

  private HttpHost resolveTarget(Request request) {
    URI uri = URI.create(mEnvironment.getApiHost().toURI()).resolve(request.toUrl());
    return new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
  }
}
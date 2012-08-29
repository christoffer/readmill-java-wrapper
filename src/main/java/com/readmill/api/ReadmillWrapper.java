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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class ReadmillWrapper {
  private String mClientId;
  private String mClientSecret;
  private Environment mEnvironment;
  private Token mToken;
  private HttpClient mHttpClient;
  private URI mRedirectURI;

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

  public void setRedirectURI(URI redirectURI) {
    mRedirectURI = redirectURI;
  }

  public URL getAuthorizationURL() {
    if(mRedirectURI == null) {
      throw new RuntimeException("Redirect URI must be set before calling getAuthorizationURL()");
    }
    try {
      String template = "%s/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s";
      String authorizeURL = String.format(template, mEnvironment.getWebHost().toURI(), mClientId, mRedirectURI.toString());
      return URI.create(authorizeURL).toURL();
    } catch(MalformedURLException e) {
      e.printStackTrace();
      return null;
    }
  }

  public Token obtainToken(String authorizationCode) {
    if(mRedirectURI == null) {
      throw new RuntimeException("Redirect URI must be set before calling obtainToken()");
    }

    Request obtainRequest = Request.to(mEnvironment.getWebHost().toURI()).withParams(
      "grant_type", "code",
      "client_id", mClientId,
      "client_secret", mClientSecret,
      "code", authorizationCode,
      "redirect_uri", mRedirectURI
    );

    try {
      String tokenResponse = getResponseText(obtainRequest, HttpPost.class);
      JSONObject tokenJson = new JSONObject(tokenResponse);
      return new Token(tokenJson);
    } catch(IOException e) {
      e.printStackTrace();
    } catch(JSONException e) {
      e.printStackTrace();
    }

    return null;
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

  public RequestBuilder get(String uri) {
    return new RequestBuilder(this, HttpGet.class, uri);
  }

  public RequestBuilder post(String uri) {
    return new RequestBuilder(this, HttpPost.class, uri);
  }

  public RequestBuilder delete(String uri) {
    return new RequestBuilder(this, HttpDelete.class, uri);
  }

  public RequestBuilder put(String uri) {
    return new RequestBuilder(this, HttpPut.class, uri);
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

  protected String getResponseText(Request request, Class<? extends HttpRequestBase> klass) throws IOException {
    HttpResponse response = execute(request, klass);
    return HttpUtils.getString(response);
  }

  protected Request authorizeRequest(Request request) {
    if(request.getToken() != null) {
      return request; // Already authenticated with token
    }

    if(mToken != null) {
      return request.usingToken(mToken); // Authenticate with wrapper token
    }

    if(mClientId != null) {
      return request.withParams("client_id", mClientId); // Authenticate with client id
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
package com.readmill.api;

import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Convenience class for constructing HTTP requests.
 * <p/>
 * Example: <code>
 * <pre>
 *  HttpRequest postReading = Request.post("/readings")
 *    .withParams("readings[private]", true)
 *    .usingToken(myValidToken)
 *    .build(HttpGet.class);
 *  httpClient.execute(postReading);
 *
 *  HttpRequest getUsers = Request.get("/user/%2/readings", 1234).build()
 *  httpClient.execute(getUsers);
 * </pre>
 * </code>
 */

public class Request {
  private List<NameValuePair> params = new ArrayList<NameValuePair>();
  private Token mToken;
  private String mResource;

  @SuppressWarnings("UnusedDeclaration")
  public Request() {
  }

  public Request(String resource, Object... args) {
    if(args != null && args.length > 0) {
      resource = String.format(resource, args);
    }
    mResource = resource;
  }

  public static Request to(String resource, Object... args) {
    return new Request(resource, args);
  }

  /**
   * @param args a list of arguments
   * @return this
   */
  public Request withParams(Object... args) {
    if(args != null) {
      if(args.length % 2 != 0)
        throw new IllegalArgumentException("need an even number of arguments");
      for(int i = 0; i < args.length; i += 2) {
        String key = args[i].toString();
        String value = String.valueOf(args[i + 1]);
        params.add(new BasicNameValuePair(key, value));
      }
    }
    return this;
  }

  /**
   * The request should be made with a specific token.
   *
   * @param token the token
   * @return this
   */
  public Request usingToken(Token token) {
    mToken = token;
    return this;
  }

  /**
   * Return the current token for this request.
   *
   * @return Request token or null
   */
  public Token getToken() {
    return mToken;
  }

  /**
   * @return a String that is suitable for use as an
   *         <code>application/x-www-form-urlencoded</code> list of parameters
   *         in an HTTP PUT or HTTP POST.
   */
  public String queryString() {
    return URLEncodedUtils.format(params, "UTF-8");
  }

  /**
   * @return The request url (including querystring)
   */
  public String toUrl() {
    return params.isEmpty() ? getResource() : getResource() + "?" + queryString();
  }

  @Override
  public String toString() {
    return mResource == null ? queryString() : toUrl();
  }

  public <T extends HttpRequestBase> T build(Class<T> klass) {
    try {
      T request = klass.newInstance();
      attachParams(request);
      authorizeRequest(request);
      return request;
    } catch(InstantiationException e) {
      throw new RuntimeException(e);
    } catch(IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch(UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean equals(Object other) {
    if(other instanceof Request) {
      return ((Request) other).toUrl().equals(toUrl());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return toUrl().hashCode();
  }

  /**
   * Private *
   */

  private void attachParams(HttpRequestBase request) throws UnsupportedEncodingException {
    if(request instanceof HttpEntityEnclosingRequestBase) {
      HttpEntityEnclosingRequestBase enclosingRequest = (HttpEntityEnclosingRequestBase) request;
      if(!params.isEmpty()) {
        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
        enclosingRequest.setEntity(new StringEntity(queryString()));
      }
      request.setURI(URI.create(getResource()));
    } else {
      request.setURI(URI.create(toUrl()));
    }
  }

  private void authorizeRequest(HttpRequest request) {
    if(mToken != null && mToken.isValid()) {
      request.setHeader("Authorization", String.format("OAuth %s", mToken.getAccessToken()));
    }
  }

  private String getResource() {
    if(mResource != null) {
      try {
        URI resourceUri = new URI(mResource);
        if(resourceUri.isAbsolute()) {
          return mResource;
        }
      } catch(URISyntaxException ignored) {}
      return (mResource.startsWith("/") ? "/v2" : "/v2/") + mResource;
    }
    return mResource;
  }
}

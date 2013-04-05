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

  /**
   * Constructs an empty request
   */
  public Request() {}

  /**
   * Constructs a request to a given resource, optionally formatted with any
   * number of parameters. The resource is interpreted as a format string
   * if arguments are provided.
   *
   * The following calls are equivalent:
   *
   * <code>
   *   new Request("users/%d/readings", 1);
   * </code>
   * and
   * <code>
   *   new Request(String.format("users/%d/readings", 1));
   * </code>
   *
   * @param resource Resource to request
   * @param args (optional) objects used for formatting the resource
   */
  public Request(String resource, Object... args) {
    if(args != null && args.length > 0) {
      resource = String.format(resource, args);
    }
    mResource = resource;
  }

  /**
   * Constructs a request to a given resource, optionally formatted with any
   * number of parameters.
   *
   * This method is just sugar for <code>new Request(String, Object...)</code>
   *
   * @param resource Resource to request
   * @param args (optional) objects used for formatting the resource
   * @return The constructed request.
   */
  public static Request to(String resource, Object... args) {
    return new Request(resource, args);
  }

  /**
   * Adds parameters to this request.
   *
   * The number of parameters has to given in pairs, but as many pairs as wanted
   * can be added.
   *
   * @param args a list of arguments (must be even in length)
   * @return this request
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
   * Sets the token used to authenticate this request.
   *
   * @param token the token
   * @return this request
   */
  public Request usingToken(Token token) {
    mToken = token;
    return this;
  }

  /**
   * Returns the current token for this request.
   *
   * @return Request token or null
   */
  public Token getToken() {
    return mToken;
  }

  /**
   * Gets the query string for this request.
   *
   * The query string is the current parameters of this request in an
   * <code>application/x-www-form-urlencoded</code> format.
   *
   * @return the query string
   */
  public String queryString() {
    return URLEncodedUtils.format(params, "UTF-8");
  }

  /**
   * Gets this request as an URL
   *
   * @return The url for this request (including query string)
   */
  public String toUrl() {
    return params.isEmpty() ? getResource() : getResource() + "?" + queryString();
  }

  /**
   * Builds a HttpRequest object based to make this request.
   *
   * @param klass HttpRequest class to use
   * @return The (authorized) http request
   */
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
  public String toString() {
    return mResource == null ? queryString() : toUrl();
  }

  @SuppressWarnings("SimplifiableIfStatement")
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

  /**
   * Attaches the params from this request to a HttpRequest
   *
   * @param request HttpRequest to attach params to
   * @throws UnsupportedEncodingException if the param could not be serialized
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

  /**
   * Adds authentication to the provided HttpRequest
   *
   * @param request HttpRequest to authenticate
   */
  private void authorizeRequest(HttpRequest request) {
    if(mToken != null && mToken.isValid()) {
      request.setHeader("Authorization", String.format("OAuth %s", mToken.getAccessToken()));
    }
  }

  /**
   * Gets the current resource.
   *
   * Relative resources are prefixed with /v2/. Absolute resources are left as
   * is.
   *
   * @return The resource
   */
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

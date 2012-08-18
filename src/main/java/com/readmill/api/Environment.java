package com.readmill.api;

import org.apache.http.HttpHost;

@SuppressWarnings("UnusedDeclaration")
public class Environment {
  private final HttpHost mApiHost, mLoginHost;

  /**
   * Create an environment for a Api Wrapper
   * @param apiHost Host to send requests to
   * @param loginHost Host where users can log in to the service
   * @param useSSL use secure connections or not
   */
  public Environment(String apiHost, String loginHost, boolean useSSL) {
    String scheme = useSSL ? "https" : "http";
    mApiHost = new HttpHost(apiHost, -1, scheme);
    mLoginHost = new HttpHost(loginHost, -1, scheme);
  }

  public HttpHost getApiHost() {
    return mApiHost;
  }

  public HttpHost getWebHost() {
    return mLoginHost;
  }

  public static final Environment Live = new Environment("api.readmill.com", "m.readmill.com", true);
  public static final Environment Staging = new Environment("api.stage-readmill.com", "m.stage-readmill.com", false);
}

package com.readmill.api;

import org.apache.http.HttpHost;

@SuppressWarnings("UnusedDeclaration")
public class Environment {
  private final HttpHost mApiHost, mLoginHost;

  /**
   * Create an environment for a Api Wrapper.
   * @param apiHost Host to send requests to
   * @param loginHost Host where users can log in to the service
   * @param useSSL use secure connections or not
   */
  public Environment(String apiHost, String loginHost, boolean useSSL) {
    this(apiHost, -1, loginHost, -1, useSSL);
  }

  /**
   * Create an environment for a Api Wrapper.
   * @param apiHost Host to send requests to
   * @param apiPort Port of api host
   * @param loginHost Host where users can log in to the service
   * @param loginPort Port of login host
   * @param useSSL use secure connections or not
   */
  public Environment(String apiHost, int apiPort, String loginHost, int loginPort, boolean useSSL) {
    String scheme = useSSL ? "https" : "http";
    mApiHost = new HttpHost(apiHost, apiPort, scheme);
    mLoginHost = new HttpHost(loginHost, loginPort, scheme);
  }

  public HttpHost getApiHost() {
    return mApiHost;
  }

  public HttpHost getWebHost() {
    return mLoginHost;
  }

  public static final Environment Live = new Environment("api.readmill.com", "m.readmill.com", true);
}

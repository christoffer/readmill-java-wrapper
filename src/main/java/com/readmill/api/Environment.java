package com.readmill.api;

import org.apache.http.HttpHost;

@SuppressWarnings("UnusedDeclaration")
public class Environment {
  private final HttpHost mApiHost, mWebHost;

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
   * @param webHost Host where users can log in to the service
   * @param webPort Port of login host
   * @param useSSL use secure connections or not
   */
  public Environment(String apiHost, int apiPort, String webHost, int webPort, boolean useSSL) {
    String scheme = useSSL ? "https" : "http";
    mApiHost = new HttpHost(apiHost, apiPort, scheme);
    mWebHost = new HttpHost(webHost, webPort, scheme);
  }

  public HttpHost getApiHost() {
    return mApiHost;
  }

  public HttpHost getWebHost() {
    return mWebHost;
  }

  public static final Environment Live = new Environment("api.readmill.com", "m.readmill.com", true);
}

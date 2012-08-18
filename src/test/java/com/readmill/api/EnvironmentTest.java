package com.readmill.api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnvironmentTest {

  @Test
  public void hostNames() {
    Environment subject = new Environment("some.api.host", "some.web.host", true);
    assertEquals("some.api.host", subject.getApiHost().getHostName());
    assertEquals("some.web.host", subject.getWebHost().getHostName());
  }

  @Test
  public void schemeToggle() {
    Environment env;
    env = new Environment("some.api.host", "some.web.host", false);
    assertEquals("http", env.getApiHost().getSchemeName());
    assertEquals("http", env.getWebHost().getSchemeName());

    env = new Environment("some.api.host", "some.web.host", true);
    assertEquals("https", env.getApiHost().getSchemeName());
    assertEquals("https", env.getWebHost().getSchemeName());
  }
}

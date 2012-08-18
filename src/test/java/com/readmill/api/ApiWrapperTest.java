package com.readmill.api;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class ApiWrapperTest {
  private ApiWrapper mWrapper;

  @Before
  public void createWrapperInstance() {
    Environment testEnvironment = new Environment("api.example.com", "www.example.com", false);
    mWrapper = new ApiWrapper("client identifier", "client secret", testEnvironment);
  }

  @Test
  public void basicGetters() {
    assertThat(mWrapper.getClientId(), is("client identifier"));
    assertThat(mWrapper.getClientSecret(), is("client secret"));
    assertThat(mWrapper.getEnvironment(), is(not(nullValue())));
  }

  @Test
  public void token() {
    assertThat(mWrapper.getToken(), is(nullValue()));
    Token myToken = new Token("haxxess", "refreshorz", "*");
    mWrapper.setToken(myToken);
    assertThat(mWrapper.getToken(), is(myToken));
  }

}

package com.readmill.api;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RequestBuilderTest {
  RequestBuilder instance;
  ReadmillWrapper mWrapper;

  @Before
  public void createInstance() {
    Environment testEnvironment = new Environment("api.example.com", "example.com", true);
    mWrapper = new ReadmillWrapper("client_id", "client_secret", testEnvironment);
    instance = new RequestBuilder(mWrapper, HttpGet.class, "/users/1/readings");
  }

  @Test
  public void getRequest() {
    assertThat(instance.getRequest().toUrl(), containsString("/v2/users/1/readings"));
  }

  @Test
  public void chainableParamBuilder() {
    String queryString = instance.order("followers").highlightsCountFrom(50).getRequest().queryString();
    assertThat(queryString, containsString("order=followers"));
    assertThat(queryString, containsString("highlights_count%5Bfrom%5D=50"));
  }

  @Test
  public void json() throws JSONException, IOException {
    JSONObject user = new JSONObject("{ \"user\": { \"username\": \"christoffer\", id: 1 } }");
    JSONObject json = builderWithStubbedResponseText(user.toString()).fetch();
    assertThat(json.toString(), is(user.toString()));
  }

  @Test
  public void jsonUnwrap() throws JSONException, IOException {
    JSONObject user = new JSONObject("{ \"user\": { \"username\": \"christoffer\", id: 1 } }");
    JSONObject json = builderWithStubbedResponseText(user.toString()).fetch("user");
    assertThat(json.toString(), is(user.optJSONObject("user").toString()));
  }

  @Test
  public void jsonItems() throws JSONException, IOException {
    JSONObject userOne = new JSONObject("{ \"user\": { \"username\": \"christoffer\", id: 1 } }");
    JSONObject userTwo = new JSONObject("{ \"user\": { \"username\": \"niki\", id: 387 } }");

    JSONObject usersWrapped = new JSONObject("{ \"items\": [ " + userOne.toString() + ", " + userTwo.toString() + " ]}");

    JSONArray usersWithoutRoot = new JSONArray();
    usersWithoutRoot.put(userOne);
    usersWithoutRoot.put(userTwo);

    JSONArray json = builderWithStubbedResponseText(usersWrapped.toString()).fetchItems();

    assertThat(json.toString(), is(usersWithoutRoot.toString()));
  }

  @Test
  public void jsonItemsUnwrap() throws JSONException, IOException {
    JSONObject userOne = new JSONObject("{ \"user\": { \"username\": \"christoffer\", id: 1 } }");
    JSONObject userTwo = new JSONObject("{ \"user\": { \"username\": \"niki\", id: 387 } }");

    JSONObject usersWrapped = new JSONObject("{ \"items\": [ " + userOne.toString() + ", " + userTwo.toString() + " ]}");

    JSONArray usersUnwrapped = new JSONArray();
    usersUnwrapped.put(userOne.getJSONObject("user"));
    usersUnwrapped.put(userTwo.getJSONObject("user"));

    JSONArray json = builderWithStubbedResponseText(usersWrapped.toString()).fetchItems("user");

    assertThat(json.toString(), is(usersUnwrapped.toString()));
  }

  /* Private helpers */

  private RequestBuilder builderWithStubbedResponseText(String jsonText) throws JSONException, IOException {
    RequestBuilder builder = Mockito.spy(instance);
    Mockito.doReturn(jsonText).when(builder).getResponseText();
    return builder;
  }
}

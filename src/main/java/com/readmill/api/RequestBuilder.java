package com.readmill.api;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("UnusedDeclaration")

public class RequestBuilder {
  private ReadmillWrapper mWrapper;
  private Request mRequest;
  private Class<? extends HttpRequestBase> mRequestBaseClass;

  public <T extends HttpRequestBase> RequestBuilder(ReadmillWrapper wrapper, Class<T> requestBaseClass, String baseURI) {
    mWrapper = wrapper;
    mRequest = Request.to(baseURI);
    mRequestBaseClass = requestBaseClass;
  }

  public Request getRequest() {
    return mRequest;
  }

  public JSONArray jsonItems() {
    return fetch().optJSONArray("items");
  }

  public JSONArray jsonItems(String unwrapKey) {
    JSONArray items = fetch().optJSONArray("items");
    if(items == null || items.length() == 0) {
      return new JSONArray();
    }

    ArrayList<JSONObject> unwrapped = new ArrayList<JSONObject>(items.length());

    try {
      for(int i = 0; i < items.length(); i++) {
        unwrapped.add(items.getJSONObject(i).getJSONObject(unwrapKey));
      }
    } catch(JSONException e) {
      e.printStackTrace();
      return new JSONArray();
    }
    return new JSONArray(unwrapped);
  }

  /**
   * Performs the request and returns the parsed JSON object.
   */
  public JSONObject fetch() {
    try {
      String responseText = getResponseText();
      return new JSONObject(responseText);
    } catch(JSONException e) {
      e.printStackTrace();
      return new JSONObject();
    } catch(IOException e) {
      e.printStackTrace();
      return new JSONObject();
    }
  }

  public JSONObject fetch(String key) {
    return fetch().optJSONObject(key);
  }

  public void send() throws IOException {
    sendRequest();
  }

  public String getResponseText() throws IOException {
    HttpResponse response = sendRequest();
    HttpEntity entity = response.getEntity();
    return EntityUtils.toString(entity);
  }

  public HttpResponse sendRequest() throws IOException {
    return mWrapper.execute(mRequest, mRequestBaseClass);
  }

  /************************/
  /** Parameter builders **/
  /************************/

  /**
   * Orders result before returning it from the server.
   * Affects ranges.
   */
  public RequestBuilder order(String order) {
    mRequest.withParams("order", order);
    return this;
  }

  /**
   * Filter on maximum highlight count
   */
  public RequestBuilder highlightsCountTo(int maxCount) {
    mRequest.withParams("highlights_count[to]", maxCount);
    return this;
  }

  /**
   * Filter on minimum highlight count
   */
  public RequestBuilder highlightsCountFrom(int minCount) {
    mRequest.withParams("highlights_count[from]", minCount);
    return this;
  }


}

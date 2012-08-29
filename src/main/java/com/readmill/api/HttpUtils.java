package com.readmill.api;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class HttpUtils {
  /**
   * Parses a http response as JSON.
   *
   * @param response Response to parse.
   * @return The parsed JSONObject or null
   */
  public static JSONObject optJson(HttpResponse response) {
    try {
      return getJson(response);
    } catch(IOException e) {
      e.printStackTrace();
      return null;
    } catch(JSONException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Returns a http response as JSON.
   *
   * @param response Response to parse.
   * @return The parsed JSONObject
   * @throws IOException
   * @throws JSONException
   */
  public static JSONObject getJson(HttpResponse response) throws IOException, JSONException {
    return new JSONObject(getString(response));
  }

  public static String getString(HttpResponse response) throws IOException {
    HttpEntity entity = response.getEntity();
    return EntityUtils.toString(entity);
  }

  public static String optString(HttpResponse response) {
    try {
      return getString(response);
    } catch(IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}

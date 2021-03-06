package com.readmill.api;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

@SuppressWarnings("UnusedDeclaration")

public class RequestBuilder {
  private ReadmillWrapper mWrapper;
  private Request mRequest;

  private Class<? extends HttpRequestBase> mRequestBaseClass;

  private final String ISO8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  private final SimpleDateFormat iso8601Format = new SimpleDateFormat(ISO8601);

  public <T extends HttpRequestBase> RequestBuilder(ReadmillWrapper wrapper,
                                                    Class<T> requestBaseClass,
                                                    String baseURI) {
    iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
    mWrapper = wrapper;
    mRequest = Request.to(baseURI);
    mRequestBaseClass = requestBaseClass;
  }

  public Request getRequest() {
    return mRequest;
  }

  /**
   * Gets the base request class for this request builder.
   *
   * @return The current request base class
   */
  public Class<? extends HttpRequestBase> getRequestBaseClass() {
    return mRequestBaseClass;
  }

  /**
   * Executes the built request and parses the result as JSON.
   *
   * @return The parsed JSONObject or null if the request failed or was not
   *         valid JSON.
   */
  public JSONObject fetch() {
    try {
      return fetchOrThrow();
    } catch(JSONException e) {
      e.printStackTrace();
    } catch(IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Executes the built request and parses the result as JSON. Also unwraps the
   * given top level object.
   *
   * @param key top level object to unwrap.
   * @return The parsed JSONObject or null if the the request failed or the
   *         response was not properly formatted.
   */
  public JSONObject fetch(String key) {
    JSONObject fetched = fetch();
    return fetched == null ? null : fetched.optJSONObject(key);
  }

  /**
   * Executes the built request and parses the result as JSON.
   *
   * @return The parsed JSON object.
   * @throws IOException   if the request failed
   * @throws JSONException if the response was not valid JSON
   */
  public JSONObject fetchOrThrow() throws IOException, JSONException {
    return new JSONObject(getResponseText());
  }

  /**
   * Executes the built request and parses the result as JSON. Also unwraps the
   * given top level object.
   *
   * @param key The top level object to unwrap
   * @return The parsed JSON object.
   * @throws IOException   if the request failed
   * @throws JSONException if the response was not valid JSON
   */
  public JSONObject fetchOrThrow(String key) throws IOException, JSONException {
    JSONObject fetched = new JSONObject(getResponseText());
    return fetched.getJSONObject(key);
  }

  /**
   * Executes the built request and parses the result as JSON. Also unwraps
   * the top level object "items".
   *
   * @return The parsed JSONArray or null if the request failed, or the response
   *         was not properly formatted.
   * @see #fetchItemsOrThrow()
   */
  public JSONArray fetchItems() {
    JSONObject fetched = fetch();
    return fetched == null ? null : fetched.optJSONArray("items");
  }

  /**
   * Executes the built request and parses the result as JSON. Also unwraps the
   * top level object "items", and each given top level object inside of the
   * collection.
   *
   * @param key Top level key to unwrap
   * @return The parsed JSONArray or null if the request failed or the response
   *         was no properly formatted.
   * @see #fetchItemsOrThrow(String)
   */
  public JSONArray fetchItems(String key) {
    try {
      return fetchItemsOrThrow(key);
    } catch(IOException e) {
      e.printStackTrace();
    } catch(JSONException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Executes the built request and parses the result as JSON. Also unwraps the
   * top level object "items".
   *
   * <p/>
   * Readmill collection objects are objects with a top level key "items" that
   * contains an array of the objects in the collection.
   * <p/>
   * Readmill response example:
   * <pre>
   * <code>
   * {
   * "items": [
   * { "book": { id: 1 } },
   * { "book": { id: 2 } }
   * ]
   * }
   * </code>
   * </pre>
   *
   * @return The parsed JSONArray.
   * @throws java.io.IOException    If the request failed
   * @throws org.json.JSONException If the response was not properly formatted
   */
  public JSONArray fetchItemsOrThrow() throws IOException, JSONException {
    JSONObject fetched = fetchOrThrow();
    return fetched.getJSONArray("items");
  }

  /**
   * Executes the built request and parses the result as JSON. Also unwraps the
   * top level object "items", and each given top level object inside of the
   * collection.
   * <p/>
   * Readmill collection objects are objects with a top level key "items" that
   * contains an array of the objects in the collection. Each object is
   * individually wrapped by a key which indicates it's type.
   * <p/>
   * The method will first unwrap the top level object "items", and then unwrap
   * each item in the collection. This only works if each object in the
   * collection have the same type.
   * <p/>
   * For example, if calling fetchItems("book"), then this will parse correctly:
   * <pre>
   * <code>
   * {
   * "items": [
   * { "book": { id: 1 } },
   * { "book": { "id": 2 } }
   * ]
   * }
   * # => JSONArray with {id: 1} and {id: 2}
   * </code>
   * </pre>
   * but this will fail since the top level objecst are not the same:
   * <pre>
   * <code>
   * {
   *   "items": [
   *     { "book": { id: 1 } },
   *     { "reading": { "id": 1 } }
   *   ]
   * }
   * # => null
   * </code>
   * </pre>
   * @param key Top level key of objects to unwrap
   * @return The parsed JSONArray or null if the request failed, or the response
   *         was not properly formatted.
   * @throws java.io.IOException    If the request was not successful
   * @throws org.json.JSONException If the response was not properly formatted
   */
  public JSONArray fetchItemsOrThrow(String key) throws IOException, JSONException {
    JSONArray items = fetchItemsOrThrow();
    ArrayList<JSONObject> unwrapped = new ArrayList<JSONObject>(items.length());

    for(int i = 0; i < items.length(); i++) {
      unwrapped.add(items.getJSONObject(i).getJSONObject(key));
    }

    return new JSONArray(unwrapped);
  }

  /**
   * Alias for #fetch() that does not return a value.
   */
  public void send() {
    fetch();
  }

  /**
   * Alias for fetchOrThrow() that does not return a value.
   *
   * @throws IOException when the request was not successful.
   * @throws JSONException if the server did not provide an expected response
   */
  public void sendOrThrow() throws IOException, JSONException {
    fetchItemsOrThrow();
  }


  /**
   * Executes the built request and returns the text body of the response.
   *
   * @return The text body of the response.
   * @throws IOException if the request fails
   */
  public String getResponseText() throws IOException {
    HttpResponse response = sendRequest();
    HttpEntity entity = response.getEntity();
    return EntityUtils.toString(entity);
  }

  // ==================
  // ARGUMENT BUILDERS
  // ==================

  // The name of the author.
  // Example value: Franz Kafka
  public RequestBuilder author(String value) {
    return args("author", value);
  }

  // The name of the author. Multiple authors should be separated with comma.
  // Example value: Franz Kafka
  public RequestBuilder bookAuthor(String value) {
    return args("book[author]", value);
  }

  // A unique identifier of the book. It is currently validated as an ISBN,
  // though were are looking into opening up to more generic identifiers like
  // URLs or any arbitrary string.
  // Example value: 0070162816
  public RequestBuilder bookIdentifier(String value) {
    return args("book[identifier]", value);
  }

  // The title of the book you want to create.
  // Example value: Metamorphosis
  public RequestBuilder bookTitle(String value) {
    return args("book[title]", value);
  }

  // The comment text.
  // Example value: This is a comment
  public RequestBuilder commentContent(String value) {
    return args("comment[content]", value);
  }

  // The time the comment was created. This is used to create comments after
  // they were posted.
  // Example value: 2012-02-27T12:45:02Z
  public RequestBuilder commentPostedAt(String value) {
    return args("comment[posted_at]", value);
  }

  public RequestBuilder commentPostedAt(Date value) {
    return commentPostedAt(toISO8601(value));
  }


  // The number of results to return. Default is 20, max 100.
  // Example value: 75
  public RequestBuilder count(long value) {
    return args("count", value);
  }

  // Filter a set response.
  public RequestBuilder filter(String value) {
    return args("filter", value);
  }

  // Return results using a date to select a range.
  // Example value: 2012-02-27T12:45:02Z
  public RequestBuilder from(String value) {
    return args("from", value);
  }

  public RequestBuilder from(Date value) {
    return from(toISO8601(value));
  }


  // The content of the highlight.
  // Example value: A great highlight
  public RequestBuilder highlightContent(String value) {
    return args("highlight[content]", value);
  }

  // The time the highlight was created by the user.
  // Example value: 2012-02-27T12:45:02Z
  public RequestBuilder highlightHighlightedAt(String value) {
    return args("highlight[highlighted_at]", value);
  }

  public RequestBuilder highlightHighlightedAt(Date value) {
    return highlightHighlightedAt(toISO8601(value));
  }


  // Locators are used to determine the exact location of the highlight in a
  // larger piece of text. They are defined with a custom JSON structure that
  // contains a few different data points.
  //
  // <a href="/api/docs/v2/locators.html">More on locators here</a>.
  //
  // Example value: {}
  public RequestBuilder highlightLocators(JSONObject value) {
    return args("highlight[locators]", value.toString());
  }

  public RequestBuilder highlightLocators(String value) {
    return args("highlight[locators]", value);
  }

  // The position in the book where this highlight was made. Percent as a double.
  // Example value: 0.8723
  public RequestBuilder highlightPosition(double value) {
    return args("highlight[position]", value);
  }

  // Only include readings which have equal or more highlights.
  // Example value: 8
  public RequestBuilder highlightsCountFrom(long value) {
    return args("highlights_count[from]", value);
  }

  // Only include readings which have less or equal highlights.
  // Example value: 10
  public RequestBuilder highlightsCountTo(long value) {
    return args("highlights_count[to]", value);
  }

  // A unique identifier of the book. It is currently validated as an ISBN,
  // though were are looking into opening up to more generic identifiers like
  // URLs or any arbitrary string.
  // Example value: 0070162816
  public RequestBuilder identifier(String value) {
    return args("identifier", value);
  }

  // The sort order of the collection. Always descending, default is created_at.
  // Valid values (depending on endpoint): touched_at, created_at, popular
  public RequestBuilder order(String value) {
    return args("order", value);
  }

  // The duration of the reading session. In seconds.
  // Example value: 500
  public RequestBuilder pingDuration(long value) {
    return args("ping[duration]", value);
  }

  // A unique identifier that is used to group pings together to the same period
  // when processed. In Readmill for iPad we define this identifier to change
  // every time the user has been idle for more than 1 hour. That is when there
  // has been more than 1 hour since the last ping.
  // Example value: ae45ba88ec
  public RequestBuilder pingIdentifier(String value) {
    return args("ping[identifier]", value);
  }

  // The latitude coordinates of the position when reading.
  // Example value: 59.3085
  public RequestBuilder pingLat(double value) {
    return args("ping[lat]", value);
  }

  // The longitude coordinates of the position when reading.
  // Example value: 18.1995
  public RequestBuilder pingLng(double value) {
    return args("ping[lng]", value);
  }

  // When the session occurred.
  // Example value: 2012-02-27T12:45:02Z
  public RequestBuilder pingOccurredAt(String value) {
    return args("ping[occurred_at]", value);
  }

  public RequestBuilder pingOccurredAt(Date value) {
    return pingOccurredAt(toISO8601(value));
  }

  // The progress of the reading session. In percent, between <code>0.0</code>
  // and <code>1.0</code>.
  // Example value: 0.25
  public RequestBuilder pingProgress(double value) {
    return args("ping[progress]", value);
  }

  // This parameter is used for sharing to other networks.
  //
  // Through <a href="/api/docs/v2/get/me/connections.html">/me/connections</a>
  // you can find id's of the authenticated user's connection to sites like
  // Twitter and Facebook. The response from that endpoint also includes the
  // users default state (share or not share).
  //
  // To enable sharing to one or more connection, pass an array of JSON objects
  // that include a single key: "id" with the id for the given connection.
  //
  // If omitted sharing will happen according to the authenticated user
  // default settings.
  public RequestBuilder readingPostTo(JSONArray connections) {
    return args("reading[post_to]", connections.toString());
  }

  // This parameter is used for sharing to other networks.
  //
  // Through <a href="/api/docs/v2/get/me/connections.html">/me/connections</a>
  // you can find id's of the authenticated user's connection to sites like
  // Twitter and Facebook. The response from that endpoint also includes the
  // users default state (share or not share).
  //
  // To enable sharing to one or more connection, pass an array of JSON objects
  // that include a single key: "id" with the id for the given connection.
  //
  // If omitted sharing will happen according to the authenticated user
  // default settings.
  public RequestBuilder highlightPostTo(JSONArray connections) {
    return args("highlight[post_to]", connections.toString());
  }

  // A string with search terms.
  public RequestBuilder query(String value) {
    return args("query", value);
  }

  // Date which says when this reading was abandoned. Mainly for use when
  // readings are added after they happened. This date can only be set if the
  // state of the reading is abandoned.
  // Example value: 2012-02-27T12:45:02Z
  public RequestBuilder readingAbandonedAt(String value) {
    return args("reading[abandoned_at]", value);
  }

  public RequestBuilder readingAbandonedAt(Date value) {
    return readingAbandonedAt(toISO8601(value));
  }


  // A closing remark of the book. Only visible if book is finished or
  // abandoned.
  // Example value: This was truly a great read
  public RequestBuilder readingClosingRemark(String value) {
    return args("reading[closing_remark]", value);
  }

  // Date which says when this reading was finished. Mainly for use when
  // readings are added after they happened. This parameter can only be used if
  // the state of the reading is finished.
  // Example value: 2012-02-27T12:45:02Z
  public RequestBuilder readingFinishedAt(String value) {
    return args("reading[finished_at]", value);
  }

  public RequestBuilder readingFinishedAt(Date value) {
    return readingFinishedAt(toISO8601(value));
  }

  // Flag to indicate if the reading is private or public.
  // Valid values: true, false
  public RequestBuilder readingPrivate(boolean value) {
    return args("reading[private]", value);
  }

  // Flag to indicate if the reader recommends the book.
  // Valid values: true, false
  public RequestBuilder readingRecommended(boolean value) {
    return args("reading[recommended]", value);
  }

  // Date which says when this reading was started. Mainly for use when readings
  // are added after they happened. This parameter can only be used if the state
  // of the reading is reading.
  // Example value: 2012-02-27T12:45:02Z
  public RequestBuilder readingStartedAt(String value) {
    return args("reading[started_at]", value);
  }

  public RequestBuilder readingStartedAt(Date value) {
    return readingStartedAt(toISO8601(value));
  }


  // If the reading was recommended by another user you can credit them by
  // including their user id.
  // Example value: 9
  public RequestBuilder readingViaId(long value) {
    return args("reading[via_id]", value);
  }

  // The size of image to be redirected to.
  // Valid values: small_square (30x30), medium (50x50), large (280x280)
  public RequestBuilder size(String value) {
    return args("size", value);
  }

  // Only return readings that are in certain states. Accepts a comma separated
  // list.
  // Valid values: interesting, reading, finished, abandoned
  public RequestBuilder states(String value) {
    return args("states", value);
  }

  // The state of the reading.
  // Valid values: interesting, reading, finished, abandoned
  public RequestBuilder readingState(String value) {
    return args("reading[state]", value);
  }

  // The title of the book.
  // Example value: Metamorphosis
  public RequestBuilder title(String value) {
    return args("title", value);
  }

  // Return results using a date to select a range. This parameter is
  // non-inclusive.
  // Example value: 2012-02-29T12:45:02Z
  public RequestBuilder to(String value) {
    return args("to", value);
  }


  // Helpers

  private HttpResponse sendRequest() throws IOException {
    return mWrapper.execute(mRequest, mRequestBaseClass);
  }

  private RequestBuilder args(String key, String value) {
    mRequest.withParams(key, value);
    return this;
  }

  private RequestBuilder args(String key, int value) {
    mRequest.withParams(key, value);
    return this;
  }

  private RequestBuilder args(String key, double value) {
    mRequest.withParams(key, value);
    return this;
  }

  private RequestBuilder args(String key, long value) {
    mRequest.withParams(key, value);
    return this;
  }

  private RequestBuilder args(String key, boolean value) {
    mRequest.withParams(key, value ? "true" : "false");
    return this;
  }

  private String toISO8601(Date date) {
    return date == null ? "" : iso8601Format.format(date);
  }
}

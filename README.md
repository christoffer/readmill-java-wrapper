# DISCLAIMER: This is a work in progress.

Not all functionality mentioned below is necessarily implemented or working correctly.

This note will be removed when the first stable version is done.

# Readmill API V2 Wrapper

Wrapper around the Readmill API version 2.

Code is *heavily* inspired by the excellent [Java wrapper for SoundCloud](https://github.com/soundcloud/java-api-wrapper).

Example usage:

```java
  ReadmillWrapper wrapper = new ReadmillWrapper("my_client_id", "my_client_secret", Environment.LIVE);

  JSONObject user = wrapper.get("/users/1").json("user");
  user.optString("username"); // => "christoffer"

  JSONArray readings = wrapper.get("/users/1/readings").
               order("created_at").from("2012-08-13:37:00Z").
               count(50).
               jsonItems("reading") // returns an array of readings
```

For authenticated requests you need to obtain an access token.

```java
  ReadmillWrapper wrapper = new ReadmillWrapper("my_client_id", "my_client_secret", Environment.LIVE);

  Token myAccessToken = getAnOAuthTokenSomehow();
  wrapper.setToken(myAccessToken);

  // Create
  reading = wrapper.post("/books/20461/readings").readingState("reading").readingVia(1).
              json("reading"); // => returns the reading

  // Update
  wrapper.put("/readings/" + reading.getInt("id")).readingState("finished").json();

  // Delete
  wrapper.delete("/readings/" + reading.getInt("id")).json();
```

Or if you prefer to build the requests yourself:

```java
  ReadmillWrapper wrapper = new ReadmillWrapper("my_client_id", "my_client_secret", Environment.LIVE);

  Token userToken = currentUserTokenForMyMultiUserApp();

  Request updateReadingState = Request.to("/readings/" + 77).
                                 withParams("state", "finished", "reading[via]", 1).
                                 usingToken(userToken);

  HTTPResponse response = wrapper.put(updateReadingState).request(); // .json() etc are also available here
```

## Breakdown of a complete authenticated request

```java
/*
 * Select which server to send requests. In 99% of the cases you'll want Environment.Live here
 * which is the standard Readmill API server.
 */
Environment env = Environment.Live;

/*
 * Initiate a wrapper with your client credentials that you got when you registered your app.
 * You can register a new all, or manage your current apps here: [https://readmill.com/you/apps](https://readmill.com/you/apps)
 */
ReadmillWrapper wrapper = new ReadmillWrapper("my-client-id", "my-client-secret", env);

/*
 * Obtaining a token requires a browser for the user to log in to Readmill and authorize your application.
 * The wrapper can generate a url where you can send the user, but you need to have a way to caputer the
 * authorization code returned from Readmill once the user allows your app.
 * Once you have the code, then the wrapper can exchange that code for a token.
 * The full Readmill authorization flow is described [here](http://developers.readmill.com/api/docs/v2/authentication.html)
 */
URI myCallback = URI.create("https://my-readmill-hack.com/callback");
URI authorizationUrl = wrapper.getAuthorizationUrl(myCallback);

/*
 * Exchange the authorization code for a token and store in the wrapper
 */
wrapper.exchangeCodeForToken(myCode);

/* Start building a request by calling wrapper.get(<base path>).
 * The base path is the path of the request (without the leading version: /v2 !).
 * You can also use .post(), .delete(), .put() or .head() for endpoints with other HTTP verbs.
 */

RequestBuilder builder = wrapper.get("/me");

/* Finish building a request by calling .json(). This sends the request to the server
 * and parses the result as a JSON object.
 * All responses from Readmill are wrapped in a key that determines the type of the object.
 * For example, the /me endpoint returns a user (the user for the current token), and the json
 * then loooks like this: { "user": { <the actual user data> } }
 * If you pass in a string the the .json() call, the wrapper will unwrap the response and return the
 * actual user data directly.
 */

JSONObject me = builder.json("user");

/* For more fluent request building you can of course skip assigning a RequestBuilder and go
 * directly from the wrapper .get() to the .json() call:
 * JSONObject me = wrapper.get("/me").json("user");

 * RequestBuilders are chainable, so you can construct complex queries by chaining on multiple parameter
 * setters in a row, and then finalize the request by calling .json().
 */

builder = wrapper.get("/users/" + me.getInt("id") + "/readings");

builder = builder.order("created_at").from("2012-08-20T13:37:00Z").count(50);

/* The Readmill API is consistent about wrapping objects under their type, even for collections.
 * All collections are wrapped under the key: "items", and each object in the collection is individually.
 * wrapped by its type.
 * For example:
 * {
 *  "items": [ { "reading" { <reading data> } }, ... ]
 * }
 *
 * The wrapper provdes jsonItems() as a convenience to unwrap the "items" object.
 * .jsonItems() => [ { "reading" { <reading data> } }, ... ]
 *
 * and the jsonItems(<string>) which unwraps each item in a collection (which, of course, only works
 * when all the items are the same type.
 *
 * .jsonItems("reading") => [ { <reading data> }, ... ]
 */

JSONArray readings = builder.jsonItems("reading");

JSONObject firstReading = readings.getJSONObject(i);

/*
 * You can use the request builder to build all types of requests.
 * So we conclude this breakdown by finishing the book and submit a closing remark.
 */

firstReading = wrapper.put("/readings/" + firstReading.getInt("id")).
  readingState("finished").
  readingClosingRemark("Amazing book, front to cover").
  json("reading"); // Readmill returns the updated resource

System.out.println(firstReading.getString("closing_remark")); // Prints "Amazing book, front to cover"
```


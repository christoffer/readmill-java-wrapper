# DISCLAIMER: This is a work in progress.

Not all functionality mentioned below is necessarily implemented or working correctly.

This note will be removed when the first stable version is done.

# Readmill API V2 Wrapper

Wrapper around the Readmill API version 2.

Code is *heavily* inspired by the excellent [Java wrapper for SoundCloud](https://github.com/soundcloud/java-api-wrapper).

Example usage:

```java
  ReadmillWrapper wrapper = new ReadmillWrapper("my_client_id", "my_client_secret", Environment.LIVE);

  JSONObject user = wrapper.get("/users/1").fetch("user");
  user.optString("username"); // => "christoffer"

  JSONArray readings = wrapper.get("/users/1/readings").
               order("created_at").from("2012-08-13:37:00Z").
               count(50).
               fetchItems("reading") // returns an array of readings
```

For authenticated requests you need to obtain an access token.

```java
  ReadmillWrapper wrapper = new ReadmillWrapper("my_client_id", "my_client_secret", Environment.LIVE);

  Token myAccessToken = getAnOAuthTokenSomehow();
  wrapper.setToken(myAccessToken);

  // Create
  reading = wrapper.post("/books/20461/readings").readingState("reading").readingVia(1).
              fetch("reading");

  // Update
  wrapper.put("/readings/" + reading.getInt("id")).readingState("finished").
            send(); // or use .fetch() if you also want to capture the updated object

  // Delete
  wrapper.delete("/readings/" + reading.getInt("id")).send();
```

Or if you prefer to build the requests yourself:

```java
  ReadmillWrapper wrapper = new ReadmillWrapper("my_client_id", "my_client_secret", Environment.LIVE);

  Token userToken = currentUserTokenForMyMultiUserApp();

  Request updateReadingState = Request.to("/readings/" + 77).
                                 withParams("state", "finished", "reading[via]", 1).
                                 usingToken(userToken);

  HTTPResponse response = wrapper.put(updateReadingState).send(); // or fetch()
```

## Tutorial: Finish my latest reading

This tutorial will go through the following steps:

* Setup the wrapper
* Get a token from an authorize code
* Get the current user
* Get the current users readings
* Update the latest of the current user's readings


```java
Environment env = Environment.Live;
```

Selects which server to send requests. In 99% of the cases you'll want to use
`Environment.Live` which is the standard Readmill API server.

```java
ReadmillWrapper wrapper = new ReadmillWrapper("my-client-id", "my-client-secret", env);
```

Creates a wrapper with your client credentials that you got when you
registered your app. You can register a new app or manage your current ones
here: [https://readmill.com/you/apps](https://readmill.com/you/apps)

If you're accessing authenticated endpoints, then you'll need to obtain an
access token. This requires a browser for the user to log in to Readmill and
authorize your application.

The wrapper can generate a url where you can send the user, but you need to
have a way to caputer the authorization code returned from Readmill once the
user allows your app. Once you have the code, then the wrapper can exchange
that code for a token.

The full Readmill authorization process is described [here](http://developers.readmill.com/api/docs/v2/authentication.html)

```java
URI myCallbackURL = URI.create("https://my-readmill-hack.com/callback");
URI authorizationUrl = wrapper.getAuthorizationUrl(myCallbackURL);

String code = obtainAuthorizationCodeSomehow();

// Exchange the code for a token and store it in the wrapper
wrapper.getTokenForAuthorizationCode(code);
```

The wrapper allows you to construct complex requests using a `RequestBuilder`
interface that constructs a request by chaining multiple calls together.

```java
RequestBuilder builder = wrapper.get("/me");
```

The methods `.post(path)`, `.put(path)`, `.delete(path)` or `.head(path)`
are also available for endpoints with other verbs.

Finish building a request by calling `.fetch()`. This sends the request to the
server and parses the result as a JSON object. If you don't care about the
response then you can use `send()` which performs the request but does not
return anything.

All responses from Readmill are wrapped in a key that determines the type of the
object. For example, the `/me` endpoint above returns a user, and the json
response from Readmill then loooks like this:

```json
{ "user": { id: 1, "username": "christoffer", ... } }
```

The Readmill API is consistent in formatting their responses like this, so to
you might find yourself writing a lot of code that looks like this:
`.fetch().getOptJSONObject("user")`. This is so common in fact that the wrapper
provides a shortcut to unwrap a top level object directly: `fetch("user")`.

Putting the things together you can now get the current user directly with:

```java
JSONObject me = wrapper.get("/me").fetch("user");
```

In order to get the 10 latest readings of the current user, we use the `id` from the
fetched user, and construct a little longer request by chaining parameters together.

```java
JSONArray latestReadings;
String myReadingsUrl = String.format("/users/%d/readings", me.getInt("id");

latestReadings = wrapper.get(myReadingsUrl).
                   order("created_at").count(10).
                   fetchItems("reading");
```

The Readmill API is consistent about wrapping objects under a root object when it
comes to collections. All collections are wrapped under the key: "items",
and each object in the collection is individually wrapped by its type (for example: `"reading"`).

For example:

```json
{
 "items": [ { "reading": { "id": 123, "state": "reading", ... } }, ... ]
}
```

The wrapper provides the method `fetchItems()`, which is similar to calling `fetch("items")`, but
it returns an array of objects instead of a single one. The wrapper also provides a shortcut to the
common pattern of unwrapping each object inside of the array: `fetchItem("reading")`. This first
unwraps the top level object (`items`), and then unwraps each object inside of the resulting array
(each `reading`). The end result is a JSONArray of unwrapped reading objects.

For the final step of the tutorial we will get the most recent reading from the array and finish
it with a closing remark.

```java
JSONObject mostRecentReading = latestReadings.getJSONObject(0);
String mostRecentReadingUrl = String.format("/readings/%d", mostRecentReading.getInt("id"));

latestReading = wrapper.put(mostRecentReadingUrl).
  readingState("finished").
  readingClosingRemark("Amazing book, front to cover").
  fetchJSON("reading");
```

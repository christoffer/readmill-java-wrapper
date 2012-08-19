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
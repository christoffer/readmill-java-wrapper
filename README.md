# Readmill API V2 Wrapper

Example usage:

```java
  ReadmillWrapper wrapper = new ReadmillWrapper("my_client_id", "my_client_secret", Environment.LIVE);

  JSONObject user = wrapper.get("/users/1").json("user");
  user.optString("username"); // => "christoffer"

  JSONArray readings = wrapper.get("/users/1/readings").
               order("created_at").from("2012-08-13:37:00Z").
               count(50).
               jsonItems("reading") // array of readings
```

For authenticated requests you need to obtain an access token.

```java
  ReadmillWrapper wrapper = new ReadmillWrapper("my_client_id", "my_client_secret", Environment.LIVE);

  Token myAccessToken = getAnOAuthTokenSomehow();
  wrapper.setToken(myAccessToken);

  // Create
  reading = wrapper.post("/books/20461/readings").
              readingState("reading").readingVia(1).
              json("reading"); // => the created reading

  // Update
  wrapper.put("/readings/" + reading.getInt("id")).readingState("finished").json();

  // Delete
  wrapper.delete("/readings/" + reading.getInt("id")).json();
```

For more detailed control you can use manual request building:

```java
  ReadmillWrapper wrapper = new ReadmillWrapper("my_client_id", "my_client_secret", Environment.LIVE);

  Token myAccessToken = getAnOAuthTokenSomehow();

  Request updateReadingState = Request.to("/readings/" + 77).withParams("state", "finished").usingToken(myAccessToken);
  HTTPResponse response = wrapper.put(request);
```
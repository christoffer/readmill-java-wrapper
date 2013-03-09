# Readmill API V2 Wrapper

Low level wrapper around the [Readmill API](http://developers.readmill.com/).

Code is *heavily* inspired by the excellent [Java wrapper for SoundCloud](https://github.com/soundcloud/java-api-wrapper).

A short tutorial is available [here](https://github.com/christoffer/readmill-java-wrapper/wiki/Short-tutorial). Project page on [GitHub](https://github.com/christoffer/readmill-java-wrapper).

Example basic usage:

```java
  ReadmillWrapper wrapper = new ReadmillWrapper("my_client_id", "my_client_secret", Environment.LIVE);

  JSONObject user = wrapper.get("/users/1").fetch("user");
  user.optString("username"); // => "christoffer"

  JSONArray readings = wrapper.get("/users/1/readings")
                         .order("created_at").from("2012-08-01T13:37:00Z")
                         .count(50)
                         .fetchItems("reading") // => an array of readings
```

Authenticated requests that uses a token:

```java
  ReadmillWrapper wrapper = new ReadmillWrapper("my_client_id", "my_client_secret", Environment.LIVE);

  Token myAccessToken = getAnOAuthTokenSomehow();
  wrapper.setToken(myAccessToken);

  // Create
  reading = wrapper.post("/books/20461/readings").readingState("reading").fetch("reading");

  // Update
  String readingEndpoint = String.format("/readings/%d", reading.getInt("id"));
  wrapper.put(readingEndpoint).readingState("finished").fetch();

  // Delete
  wrapper.delete(readingEndpoint).send(); // .send() is just .fetch() without a return value
```

You can also drop down to a more manual level with finer control of requets building:

```java
  ReadmillWrapper wrapper = new ReadmillWrapper("my_client_id", "my_client_secret", Environment.LIVE);

  Token userToken = currentUserTokenForMyMultiUserApp();

  Request updateReadingState = Request.to("/readings/" + 77)
                                 .withParams("state", "finished", "reading[via]", 1)
                                 .usingToken(userToken);

  HTTPResponse response = wrapper.put(updateReadingState).fetch();
```

## Maven

The wrapper is published as a Maven repository available from the [GitHub repo](https://github.com/christoffer/readmill-java-wrapper).

To use it add this to your `pom.xml` file:

```xml
<repositories>
  <repository>
    <id>readmill-java-wrapper-snapshots</id>
    <url>https://github.com/christoffer/readmill-java-wrapper/raw/master/snapshots</url>
  </repository>
</repositories>
```

And then list the dependency like this:

```xml
<dependency>
  <groupId>com.christofferklang</groupId>
  <artifactId>readmill-java-wrapper</artifactId>
  <version>0.1</version>
</dependency>
```

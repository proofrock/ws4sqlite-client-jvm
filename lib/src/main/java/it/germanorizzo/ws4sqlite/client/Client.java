/*
  Copyright (c) 2022-, Germano Rizzo <oss@germanorizzo.it>

  Permission to use, copy, modify, and/or distribute this software for any
  purpose with or without fee is hereby granted, provided that the above
  copyright notice and this permission notice appear in all copies.

  THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/
package it.germanorizzo.ws4sqlite.client;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static it.germanorizzo.ws4sqlite.client.Utils.check;

/**
 * <p>This class represent a client for ws4sqlite. Once configured with the URL to
 * contact and the authorization (if any), it can be used to send Requests to
 * the server.</p>
 *
 * <p>The class is thread-safe and, once built, can be safely stored in a constant.</p>
 *
 * <p>Example:</p>
 * <pre>
 * Client cli = Client.make()
 *                 .withURLComponents(Client.Protocol.HTTP, "localhost", 12321, "mydb")
 *                 .withHTTPAuth("myUser1", "myHotPassword");
 *
 * Response r = cli.send(...);
 * </pre>
 */
@SuppressWarnings("unused")
public final class Client {
    private static final ObjectMapper OM = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder().build();
    private final String url, user, pass;
    private final AuthMode authMode;

    private Client() {
        this.url = this.user = this.pass = null;
        this.authMode = AuthMode.NONE;
    }

    private Client(Client old, String url) {
        this.url = url;
        this.user = old.user;
        this.pass = old.pass;
        this.authMode = old.authMode;
    }

    private Client(Client old, String user, String pass, AuthMode authMode) {
        this.url = old.url;
        this.user = user;
        this.pass = pass;
        this.authMode = authMode;
    }

    /**
     * First step when building. Generates a new Client instance.
     *
     * @return A new (empty) Client instance
     */
    public static Client make() {
        return new Client();
    }

    /**
     * Builder methods that adds a "raw" URL for contacting the ws4sqlite remote.
     *
     * @param url The URL/endpoint/remote
     * @return The Client, for chaining
     */
    public Client withURL(String url) {
        check(url != null, "Cannot specify a null argument");
        return new Client(this, url);
    }

    /**
     * Builder methods that adds an URL for contacting the ws4sqlite remote, given its components.
     *
     * @param protocol   The protocol (HTTP/S)
     * @param host       The remote host
     * @param port       Remote port
     * @param databaseId ID of the database
     * @return The Client, for chaining
     */
    public Client withURLComponents(Protocol protocol, String host, int port, String databaseId) {
        check(protocol != null, "Cannot specify a null protocol");
        check(host != null, "Cannot specify a null host");
        check(port > 0 && port <= 65536, "Cannot specify an invalid port");
        check(databaseId != null, "Cannot specify a null database ID");
        String url = String.format("%s://%s:%d/%s", protocol.name(), host, port, databaseId);
        return new Client(this, url);
    }

    /**
     * Builder methods that adds an URL for contacting the ws4sqlite remote, given its components.
     *
     * @param protocol   The protocol (HTTP/S)
     * @param host       The remote host
     * @param databaseId ID of the database
     * @return The Client, for chaining
     */
    public Client withURLComponents(Protocol protocol, String host, String databaseId) {
        check(protocol != null, "Cannot specify a null protocol");
        check(host != null, "Cannot specify a null host");
        check(databaseId != null, "Cannot specify a null database ID");
        String url = String.format("%s://%s/%s", protocol.name(), host, databaseId);
        return new Client(this, url);
    }

    /**
     * Builder methods that configures inline authentication; it must be configured so in the remote.
     *
     * @param user The username
     * @param pass The password
     * @return The Client, for chaining
     */
    public Client withInlineAuth(String user, String pass) {
        check(user != null, "Cannot specify a null user");
        check(pass != null, "Cannot specify a null password");
        return new Client(this, user, pass, AuthMode.INLINE);
    }

    /**
     * Builder methods that configures HTTP Basic Authentication; it must be configured so in the remote.
     *
     * @param user The username
     * @param pass The password
     * @return The Client, for chaining
     */
    public Client withHTTPAuth(String user, String pass) {
        check(user != null, "Cannot specify a null user");
        check(pass != null, "Cannot specify a null password");
        return new Client(this, user, pass, AuthMode.HTTP);
    }

    /**
     * Sends a set of requests to the remote, wrapped in a Request object. Returns
     * a matching set of Responses, wrapped in a Response object.
     * <p>
     * Throws a WS4SClientException if the remote service returns an error.
     *
     * @param r The Request(s) wrapper
     * @return The Response(s) wrapper
     * @throws IOException Network failure or WS4SClientException if ws4sqlite
     *                     answers with an error. The fields of the exception are the error's details.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Response send(RequestBuilder.Request r) throws IOException {
        assert user != null;
        assert pass != null;
        assert url != null;

        final Map<String, Object> map = r.request;

        if (authMode == AuthMode.INLINE)
            map.put("credentials", new MapBuilder().add("user", user).add("pass", pass).getMap());

        final String json = OM.writeValueAsString(map);

        final RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder();
        if (authMode == AuthMode.HTTP) {
            requestBuilder = requestBuilder.addHeader("Authorization", Credentials.basic(user, pass));
        }

        final okhttp3.Request request = requestBuilder.url(url).post(body).build();
        final Call call = CLIENT.newCall(request);
        final okhttp3.Response response = call.execute();

        final byte[] jsonRes = Objects.requireNonNull(response.body()).bytes();
        final int code = response.code();

        if (code != 200) {
            if (authMode == AuthMode.HTTP && code == 401) {
                throw new ClientException("Unauthorized", -1, 401);
            }
            final Err err = OM.readValue(jsonRes, Err.class);
            throw new ClientException(err.error, err.qryIdx, err.code);
        }

        final Map m = OM.readValue(jsonRes, Map.class);

        final Response ret = new Response();
        for (int i = 0; i < ((List<Map>) m.get("results")).size(); i++) {
            final Map ri = ((List<Map>) m.get("results")).get(i);
            ret.getResults().add(new Response.Item(
                    (Boolean) ri.get("success"),
                    (Integer) ri.get("rowsUpdated"),
                    (List<Integer>) ri.get("rowsUpdatedBatch"),
                    (List<Map<String, Object>>) ri.get("resultSet"),
                    (String) ri.get("error")));
        }

        return ret;
    }

    /**
     * Authentication mode for the database remote.
     */
    public enum AuthMode {
        HTTP, INLINE, NONE
    }

    /**
     * Used in URL composition
     */
    public enum Protocol {
        HTTP, HTTPS
    }

    private static class Err {
        public int qryIdx, code;
        public String error;
    }
}

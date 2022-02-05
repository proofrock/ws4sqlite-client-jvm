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

/**
 * <p>This class represent a client for ws4sqlite. It can be constructed using the
 * {@link ClientBuilder} class, that configures it with the URL to contact and the authorization
 * (if any). Once instantiated, it can be used to send Requests to the server.</p>
 *
 * <p>The class is thread-safe and, once built, can be safely stored in a constant.</p>
 *
 * <p>Example:</p>
 * <pre>
 * Client cli = new ClientBuilder()
 *                 .withURLComponents(Client.Protocol.HTTP, "localhost", 12321, "mydb")
 *                 .withHTTPAuth("myUser1", "myHotPassword")
 *                 .build();
 *
 * Response r = cli.send(...);
 * </pre>
 */
@SuppressWarnings("unused")
public final class Client {
    private static final ObjectMapper OM = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder().build();
    private final String url, user, password;
    private final AuthMode authMode;
    Client(String url, String user, String password, AuthMode authMode) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.authMode = authMode;
    }

    /**
     * <p>Sends a set of requests to the remote, wrapped in a {@link Request} object. Returns
     * a matching set of responses, wrapped in a {@link Response} object.</p>
     * <p>Throws a {@link ClientException} if the remote service returns an error.</p>
     *
     * @param request The request(s) wrapper
     * @return The response(s) wrapper
     * @throws IOException Network failure or {@link ClientException} if ws4sqlite answers with an error.
     *                     The fields of the exception are the error's details.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Response send(Request request) throws IOException {
        assert user != null;
        assert password != null;
        assert url != null;

        final Map<String, Object> map = request.map;

        if (authMode == AuthMode.INLINE)
            map.put("credentials", new MapBuilder().add("user", user).add("password", password).build());

        final String json = OM.writeValueAsString(map);

        final RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder();
        if (authMode == AuthMode.HTTP) {
            requestBuilder = requestBuilder.addHeader("Authorization", Credentials.basic(user, password));
        }

        final okhttp3.Request req = requestBuilder.url(url).post(body).build();
        final Call call = CLIENT.newCall(req);
        final okhttp3.Response response = call.execute();

        final byte[] jsonRes = Objects.requireNonNull(response.body()).bytes();
        final int code = response.code();

        if (code != 200) {
            if (authMode == AuthMode.HTTP && code == 401) {
                throw new ClientException("Unauthorized", -1, 401);
            }
            final Err err = OM.readValue(jsonRes, Err.class);
            throw new ClientException(err.error, err.reqIdx, err.code);
        }

        final Map m = OM.readValue(jsonRes, Map.class);

        final Response ret = new Response(code);
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
        public int reqIdx, code;
        public String error;
    }
}

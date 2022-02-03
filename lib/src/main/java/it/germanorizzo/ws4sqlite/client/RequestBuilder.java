package it.germanorizzo.ws4sqlite.client;

import java.util.*;

import static it.germanorizzo.ws4sqlite.client.Utils.check;
import static it.germanorizzo.ws4sqlite.client.Utils.require;

/**
 * A builder class to build a {@link Request} to send to the ws4sqlite server with the {@link Client#send(Request)}
 * method.
 */
@SuppressWarnings("unused")
public final class RequestBuilder {
    private static final String K_TRX = "transaction";
    private static final String K_QUERY = "query";
    private static final String K_STATEMENT = "statement";
    private static final String K_NO_FAIL = "noFail";
    private static final String K_VALUES = "values";
    private static final String K_BATCH = "valuesBatch";
    private static final String K_ENCODER = "encoder";
    private static final String K_DECODER = "decoder";
    private static final String K_PASSWORD = "pwd";
    private static final String K_Z_LEVEL = "compressionLevel";
    private static final String K_COLUMNS = "columns";
    private final List<Map<String, Object>> request = new ArrayList<>();
    private Map<String, Object> temp = null;

    /**
     * First step when building. Generates a new {@link RequestBuilder} instance.
     */
    public RequestBuilder() {
    }

    /**
     * Returns the {@link Request} that was built.
     *
     * @return The underlying {@link Request}
     */
    public Request build() {
        require(temp != null, "There are no requests");
        request.add(temp);
        temp = null;
        Request ret = new Request();
        Map<String, Object> req = new HashMap<>();
        req.put(K_TRX, request);
        ret.map = req;
        return ret;
    }

    /**
     * Adds a new request to the list, for a query. It must be configured later on with the
     * proper methods.
     *
     * @param query The query
     * @return The {@link RequestBuilder}, for chaining
     */
    public RequestBuilder addQuery(String query) {
        check(query != null, "Cannot specify a null query");
        if (temp != null) {
            request.add(temp);
        }
        temp = new HashMap<>();
        temp.put(K_QUERY, query);
        return this;
    }

    /**
     * Adds a new request to the list, for a statement. It must be configured later on with the
     * proper methods.
     *
     * @param statement The statement
     * @return The {@link RequestBuilder}, for chaining
     */
    public RequestBuilder addStatement(String statement) {
        check(statement != null, "Cannot specify a null statement");
        if (temp != null) {
            request.add(temp);
        }
        temp = new HashMap<>();
        temp.put(K_STATEMENT, statement);
        return this;
    }

    /**
     * Specify that the request must not cause a general failure.
     *
     * @return The {@link RequestBuilder}, for chaining
     */
    public RequestBuilder withNoFail() {
        temp.put(K_NO_FAIL, Boolean.TRUE);
        return this;
    }

    /**
     * Adds a list of values (ok, amap) for the request. If there's already one,
     * it creates a batch.
     *
     * @param values The values map to add
     * @return The {@link RequestBuilder}, for chaining
     */
    @SuppressWarnings("unchecked")
    public RequestBuilder withValues(Map<String, Object> values) {
        check(values != null, "Cannot specify a null argument");
        check(!temp.containsKey(K_QUERY) || !(temp.containsKey(K_VALUES) || temp.containsKey(K_BATCH)),
                "Cannot specify a batch for a query");
        if (temp.containsKey(K_BATCH)) {
            List<Map<String, Object>> batch = (List<Map<String, Object>>) temp.get(K_BATCH);
            batch.add(values);
        } else if (temp.containsKey(K_VALUES)) {
            Map<String, Object> curValues = (Map<String, Object>) temp.get(K_VALUES);
            List<Map<String, Object>> batch = new ArrayList<>();
            batch.add(curValues);
            batch.add(values);
            temp.remove(K_VALUES);
            temp.put(K_BATCH, batch);
        } else {
            temp.put(K_VALUES, values);
        }
        return this;
    }

    /**
     * Adds a list of values (well, a map) for the request. If there's already one,
     * it creates a batch.
     *
     * @param values The values map to add
     * @return The {@link RequestBuilder}, for chaining
     */
    public RequestBuilder withValues(MapBuilder values) {
        check(values != null, "Cannot specify a null argument");
        return withValues(values.build());
    }

    /**
     * Add an encoder to the request, with compression. Allowed only for statements.
     *
     * @param password         The password for the encryption
     * @param compressionLevel The ZStd compression level, in the range 1-19
     * @param columns          The columns to encrypt
     * @return The {@link RequestBuilder}, for chaining
     */
    public RequestBuilder withEncoderAndCompression(String password, int compressionLevel, String... columns) {
        check(password != null, "Cannot specify a null password");
        check(compressionLevel >= 1 && compressionLevel <= 19, "CompressionLevel must be between 1 and 19");
        check(columns.length > 0, "Cannot specify an empty column list");
        check(!temp.containsKey(K_QUERY), "Cannot specify an encoder for a query");
        Map<String, Object> encoder = new HashMap<>();
        encoder.put(K_PASSWORD, password);
        encoder.put(K_Z_LEVEL, compressionLevel);
        encoder.put(K_COLUMNS, Arrays.asList(columns));
        temp.put(K_ENCODER, encoder);
        return this;
    }

    /**
     * Add an encoder to the request. Allowed only for statements.
     *
     * @param password The password for the encryption
     * @param columns  The columns to encrypt
     * @return The {@link RequestBuilder}, for chaining
     */
    public RequestBuilder withEncoder(String password, String... columns) {
        check(password != null, "Cannot specify a null password");
        check(columns.length > 0, "Cannot specify an empty column list");
        check(!temp.containsKey(K_QUERY), "Cannot specify an encoder for a query");
        Map<String, Object> encoder = new HashMap<>();
        encoder.put(K_PASSWORD, password);
        encoder.put(K_COLUMNS, Arrays.asList(columns));
        temp.put(K_ENCODER, encoder);
        return this;
    }

    /**
     * Add a decoder to the request. Allowed only for queries.
     *
     * @param password The password for the decryption
     * @param columns  The columns to decrypt
     * @return The {@link RequestBuilder}, for chaining
     */
    public RequestBuilder withDecoder(String password, String... columns) {
        check(password != null, "Cannot specify a null password");
        check(columns.length > 0, "Cannot specify an empty column list");
        check(!temp.containsKey(K_STATEMENT), "Cannot specify a decoder for a statement");
        Map<String, Object> decoder = new HashMap<>();
        decoder.put(K_PASSWORD, password);
        decoder.put(K_COLUMNS, Arrays.asList(columns));
        temp.put(K_ENCODER, decoder);
        return this;
    }
}

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

import static it.germanorizzo.ws4sqlite.client.Utils.check;

/**
 * <p>This class is a builder for {@link Client} instances. Once configured with the URL to
 * contact and the authorization (if any), it can be used to instantiate a {@link Client}.</p>
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
public final class ClientBuilder {
    private final String url, user, password;
    private final Client.AuthMode authMode;

    /**
     * First step when building. Generates a new {@link ClientBuilder} instance.
     */
    public ClientBuilder() {
        this.url = this.user = this.password = null;
        this.authMode = Client.AuthMode.NONE;
    }

    private ClientBuilder(ClientBuilder old, String url) {
        this.url = url;
        this.user = old.user;
        this.password = old.password;
        this.authMode = old.authMode;
    }

    private ClientBuilder(ClientBuilder old, String user, String password, Client.AuthMode authMode) {
        this.url = old.url;
        this.user = user;
        this.password = password;
        this.authMode = authMode;
    }

    /**
     * Builder methods that adds a "raw" URL for contacting the ws4sqlite remote.
     *
     * @param url The URL/endpoint/remote
     * @return The {@link ClientBuilder}, for chaining
     */
    public ClientBuilder withURL(String url) {
        check(url != null, "Cannot specify a null argument");
        return new ClientBuilder(this, url);
    }

    /**
     * Builder methods that adds an URL for contacting the ws4sqlite remote, given its components.
     *
     * @param protocol   The protocol (HTTP/S)
     * @param host       The remote host
     * @param port       Remote port
     * @param databaseId ID of the database
     * @return The {@link ClientBuilder}, for chaining
     */
    public ClientBuilder withURLComponents(Client.Protocol protocol, String host, int port, String databaseId) {
        check(protocol != null, "Cannot specify a null protocol");
        check(host != null, "Cannot specify a null host");
        check(port > 0 && port <= 65536, "Cannot specify an invalid port");
        check(databaseId != null, "Cannot specify a null database ID");
        String url = String.format("%s://%s:%d/%s", protocol.name(), host, port, databaseId);
        return new ClientBuilder(this, url);
    }

    /**
     * Builder methods that adds an URL for contacting the ws4sqlite remote, given its components.
     *
     * @param protocol   The protocol (HTTP/S)
     * @param host       The remote host
     * @param databaseId ID of the database
     * @return The {@link ClientBuilder}, for chaining
     */
    public ClientBuilder withURLComponents(Client.Protocol protocol, String host, String databaseId) {
        check(protocol != null, "Cannot specify a null protocol");
        check(host != null, "Cannot specify a null host");
        check(databaseId != null, "Cannot specify a null database ID");
        String url = String.format("%s://%s/%s", protocol.name(), host, databaseId);
        return new ClientBuilder(this, url);
    }

    /**
     * Builder methods that configures INLINE authentication; the remote must be configured accordingly.
     *
     * @param user     The username
     * @param password The password
     * @return The {@link ClientBuilder}, for chaining
     */
    public ClientBuilder withInlineAuth(String user, String password) {
        check(user != null, "Cannot specify a null user");
        check(password != null, "Cannot specify a null password");
        return new ClientBuilder(this, user, password, Client.AuthMode.INLINE);
    }

    /**
     * Builder methods that configures HTTP Basic Authentication; the remote must be configured accordingly.
     *
     * @param user     The username
     * @param password The password
     * @return The {@link ClientBuilder}, for chaining
     */
    public ClientBuilder withHTTPAuth(String user, String password) {
        check(user != null, "Cannot specify a null user");
        check(password != null, "Cannot specify a null password");
        return new ClientBuilder(this, user, password, Client.AuthMode.HTTP);
    }

    /**
     * Returns the {@link Client} that was built.
     *
     * @return The underlying {@link Client}
     */
    public Client build() {
        return new Client(url, user, password, authMode);
    }
}

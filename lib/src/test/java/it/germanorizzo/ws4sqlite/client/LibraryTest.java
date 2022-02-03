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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LibraryTest {
    Process p;

    @BeforeAll
    void setUp() throws Exception {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        p = Runtime.getRuntime().exec("src/test/resources/ws4sqlite-0.9.0-macos --cfgDir src/test/resources");

        Thread.sleep(1000);
    }

    @AfterAll
    void tearDown() {
        p.destroyForcibly();
    }

    @Test
    void requestWithHTTPAuth() throws IOException {
        Client client = new ClientBuilder()
                .withURLComponents(Client.Protocol.HTTP, "localhost", 12321, "mydb")
                .withHTTPAuth("myUser1", "myHotPassword")
                .build();

        Request req = new RequestBuilder()
                .addQuery("SELECT * FROM TEMP")

                .addQuery("SELECT * FROM TEMP WHERE ID = :id")
                .withValues(new MapBuilder().add("id", 1))

                .addStatement("INSERT INTO TEMP (ID, VAL) VALUES (0, 'ZERO')")

                .addStatement("INSERT INTO TEMP (ID, VAL) VALUES (:id, :val)")
                .withNoFail()

                .withValues(new MapBuilder().add("id", 1).add("val", "a"))

                .addStatement("INSERT INTO TEMP (ID, VAL) VALUES (:id, :val)")
                .withValues(new MapBuilder().add("id", 2).add("val", "b"))
                .withValues(new MapBuilder().add("id", 3).add("val", "c"))

                .build();

        Response res = client.send(req);

        assertEquals(5, res.getResults().size());
        assertTrue(res.getResults().get(0).isSuccess());
        assertEquals(2, res.getResults().get(0).getResultSet().size());
        assertTrue(res.getResults().get(1).isSuccess());
        assertEquals(1, res.getResults().get(1).getResultSet().size());
        assertEquals("ONE", res.getResults().get(1).getResultSet().get(0).get("VAL"));
        assertTrue(res.getResults().get(2).isSuccess());
        assertEquals(1, res.getResults().get(2).getRowsUpdated());
        assertFalse(res.getResults().get(3).isSuccess());
        assertNotNull(res.getResults().get(3).getError());
        assertTrue(res.getResults().get(4).isSuccess());
        assertEquals(2, res.getResults().get(4).getRowsUpdatedBatch().size());
        assertEquals(1, res.getResults().get(4).getRowsUpdatedBatch().get(0));
    }

    @Test
    void requestWithInlineAuth() throws IOException {
        Client client = new ClientBuilder()
                .withURLComponents(Client.Protocol.HTTP, "localhost", 12321, "mydb2")
                .withInlineAuth("myUser1", "myHotPassword")
                .build();

        Request req = new RequestBuilder()
                .addQuery("SELECT * FROM TEMP")

                .addQuery("SELECT * FROM TEMP WHERE ID = :id")
                .withValues(new MapBuilder().add("id", 1))

                .addStatement("INSERT INTO TEMP (ID, VAL) VALUES (0, 'ZERO')")

                .addStatement("INSERT INTO TEMP (ID, VAL) VALUES (:id, :val)")
                .withNoFail()

                .withValues(new MapBuilder().add("id", 1).add("val", "a"))

                .addStatement("INSERT INTO TEMP (ID, VAL) VALUES (:id, :val)")
                .withValues(new MapBuilder().add("id", 2).add("val", "b"))
                .withValues(new MapBuilder().add("id", 3).add("val", "c"))

                .build();

        Response res = client.send(req);

        assertEquals(200, res.getStatusCode());
        assertEquals(5, res.getResults().size());
        assertTrue(res.getResults().get(0).isSuccess());
        assertEquals(2, res.getResults().get(0).getResultSet().size());
        assertTrue(res.getResults().get(1).isSuccess());
        assertEquals(1, res.getResults().get(1).getResultSet().size());
        assertEquals("ONE", res.getResults().get(1).getResultSet().get(0).get("VAL"));
        assertTrue(res.getResults().get(2).isSuccess());
        assertEquals(1, res.getResults().get(2).getRowsUpdated());
        assertFalse(res.getResults().get(3).isSuccess());
        assertNotNull(res.getResults().get(3).getError());
        assertTrue(res.getResults().get(4).isSuccess());
        assertEquals(2, res.getResults().get(4).getRowsUpdatedBatch().size());
        assertEquals(1, res.getResults().get(4).getRowsUpdatedBatch().get(0));
    }
}

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

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Simple builder for maps used in {@link RequestBuilder#withValues(MapBuilder)} or
 * {@link RequestBuilder#withValues(Map)}.</p>
 *
 * <p>Usage:</p>
 * <pre>
 *     .addValues(new MapBuilder().add("key", "value").add("num", 2))
 *     // final call to .build() is optional
 * </pre>
 */
@SuppressWarnings("unused")
public final class MapBuilder {
    private final Map<String, Object> map = new HashMap<>();

    /**
     * Adds an item to the map.
     *
     * @param key   The key
     * @param value The value
     * @return The {@link MapBuilder}, for chaining
     */
    public MapBuilder add(String key, Object value) {
        map.put(key, value);
        return this;
    }

    /**
     * Returns the map that was built.
     * @return The underlying Map&lt;String, Object&gt;
     */
    public Map<String, Object> build() {
        return map;
    }
}

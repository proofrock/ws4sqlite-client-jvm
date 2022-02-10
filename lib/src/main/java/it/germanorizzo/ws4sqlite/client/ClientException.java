/*
  Copyright (c) 2022-, Germano Rizzo <oss /AT/ germanorizzo /DOT/ it>

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

import java.io.IOException;

@SuppressWarnings("unused")
/**
 * <p>This is an exception that wraps the error structure of ws4sqlite. See
 * <a href="https://germ.gitbook.io/ws4sqlite/documentation/errors#global-errors">the docs</a>".</p>
 *
 * <p>It has fields for the index of the node that failed, and for the HTTP code.</p>
 */
public class ClientException extends IOException {
    private final int reqIdx, code;

    ClientException(String message, int reqIdx, int code) {
        super(message);
        this.reqIdx = reqIdx;
        this.code = code;
    }

    /**
     * The 0-based index of the failing sub-request, -1 if it's a general failure.
     *
     * @return Index of the request that failed.
     */
    public int getReqIdx() {
        return reqIdx;
    }

    /**
     * The HTTP code of the error.
     *
     * @return HTTP code of the error (400, 401, 404 or 500)
     */
    public int getCode() {
        return code;
    }
}

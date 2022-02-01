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

import java.io.IOException;

@SuppressWarnings("unused")
public class ClientException extends IOException {
    private final int qryIdx, code;


    ClientException(String message, int qryIdx, int code) {
        super(message);
        this.qryIdx = qryIdx;
        this.code = code;
    }

    /**
     * The 0-based index of the failing query, -1 if it's a general failure.
     */
    public int getQryIdx() {
        return qryIdx;
    }

    /**
     * The HTTP code of the error.
     */
    public int getCode() {
        return code;
    }
}
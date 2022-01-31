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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Response coming from the endpoint, that is a list of single responses
 * matching the list of request that were submitted.
 */
public class Response {
    private final List<Item> results = new ArrayList<>();

    /**
     * List of result items.
     *
     * @return The list
     */
    public List<Item> getResults() {
        return results;
    }

    /**
     * Singular response coming from the endpoint
     */
    public static class Item {
        private final boolean success;
        private final Integer rowsUpdated;
        private final List<Integer> rowsUpdatedBatch;
        private final List<Map<String, Object>> resultSet;
        private final String error;

        Item(boolean success, Integer rowsUpdated, List<Integer> rowsUpdatedBatch,
             List<Map<String, Object>> resultSet, String error) {
            this.success = success;
            this.rowsUpdated = rowsUpdated;
            this.rowsUpdatedBatch = rowsUpdatedBatch;
            this.resultSet = resultSet;
            this.error = error;
        }

        /**
         * Was it a success?
         *
         * @return Success status of this response item.
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * If this was a single-valued statement (no batch), how many rows
         * were updated?
         *
         * @return Null if it wasn't a single-valued statement
         */
        public Integer getRowsUpdated() {
            return rowsUpdated;
        }

        /**
         * If this was a batch statement, how many rows
         * were updated for every item in the batch?
         *
         * @return Null if it wasn't a batch statement
         */
        public List<Integer> getRowsUpdatedBatch() {
            return rowsUpdatedBatch;
        }

        /**
         * If this was a query, what is the result set?
         *
         * @return Null if it wasn't a query
         */
        public List<Map<String, Object>> getResultSet() {
            return resultSet;
        }

        /**
         * If a managed error occurred (success=false), what was the cause?
         *
         * @return Null if it wasn't an error.
         */
        public String getError() {
            return error;
        }
    }
}

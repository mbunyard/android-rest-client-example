package com.mbunyard.rest_client_example.event;

import org.json.JSONObject;

/**
 * Event bus event.
 */
public class Event {

    /**
     * Event indicating network query/request is complete.
     */
    public static class QueryCompleteEvent {}

    /**
     * Event indicating network query/request resulted in error.
     */
    public static class QueryServiceError {
        private JSONObject jsonResponse;
        private String message;

        public QueryServiceError(JSONObject jsonResponse) {
            this.jsonResponse = jsonResponse;
        }

        public QueryServiceError(String message) {
            this.message = message;
        }

        public JSONObject getJsonResponse() {
            return jsonResponse;
        }

        public String getMessage() {
            return message;
        }
    }
}

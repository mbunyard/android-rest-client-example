package com.mbunyard.rest_client_example.event;

import org.json.JSONObject;

public class Event {

    public static class QueryCompleteEvent {}

    public static class QueryServiceError {
        private JSONObject jsonResponse;

        public QueryServiceError(JSONObject jsonResponse) {
            this.jsonResponse = jsonResponse;
        }

        public JSONObject getJsonResponse() {
            return jsonResponse;
        }
    }
}

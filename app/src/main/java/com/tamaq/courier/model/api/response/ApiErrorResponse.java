package com.tamaq.courier.model.api.response;


import android.support.annotation.Nullable;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by ivan on 24.05.17.
 */

public class ApiErrorResponse {

    List<ApiError> data;

    public static ApiErrorResponse parse(String s) throws IOException {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse();

        JsonFactory factory = new JsonFactory();

        ObjectMapper mapper = new ObjectMapper(factory);
        JsonNode rootNode;

        rootNode = mapper.readTree(s);

        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = rootNode.fields();
        apiErrorResponse.data = new ArrayList<>();
        while (fieldsIterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = fieldsIterator.next();
            apiErrorResponse.data.add(new ApiError(entry.getKey(), entry.getValue()));
        }
        return apiErrorResponse;
    }

    @Nullable
    public ApiError firstOrNull() {
        if (data == null)
            return null;
        if (data.size() > 0)
            return data.get(0);
        return null;
    }
}

package org.lunskra.adapter.utils;

import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;

public class MemberRequestGenerator {

    public static Map<String, Object> createLivingMemberRequestWithAllDataAndNoId() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", "Katharina");
        requestBody.put("lastName", "Schneider");
        requestBody.put("initialLastName", "Becker");
        requestBody.put("gender", "F");
        requestBody.put("birthDate", "1987-09-03");
        requestBody.put("birthCity", "Hamburg");
        requestBody.put("birthCountry", "Deutschland");
        requestBody.put("email", "katharina.schneider@example.de");
        requestBody.put("telephone", "+49 40 12345678");
        requestBody.put("streetAndNumber", "Jungfernstieg 12");
        requestBody.put("postcode", "20095");
        requestBody.put("city", "Hamburg");
        return requestBody;
    }

    public static RequestSpecification createMultiPartRequestWithMemberRequestFields(RequestSpecification req, Map<String, Object> requestBody) {
        for (Map.Entry<String, Object> entry : requestBody.entrySet()) {
            Object v = entry.getValue();
            if (v != null) {
                req.multiPart(entry.getKey(), String.valueOf(v));
            }
        }
        return req;
    }
}

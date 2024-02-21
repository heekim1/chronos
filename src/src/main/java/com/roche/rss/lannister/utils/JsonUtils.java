package com.roche.rss.lannister.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

public class JsonUtils {

    /**
     * Convert stringified json to HashMap
     * @param json
     * @return
     * @throws JsonProcessingException
     */
    public static HashMap jsonToMap(String json) throws JsonProcessingException {
        HashMap<String, String> map = new HashMap<String, String>();
        ObjectMapper mapper = new ObjectMapper();
        map = mapper.readValue(json, new TypeReference<HashMap<String, String>>() {});
        System.out.println(map);
        return map;
    }
}

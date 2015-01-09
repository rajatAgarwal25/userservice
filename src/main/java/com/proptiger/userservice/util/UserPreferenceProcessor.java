package com.proptiger.userservice.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.util.JsonLoader;
import com.proptiger.core.exception.ProAPIException;
import com.proptiger.core.util.JsonUtil;

/**
 * Class for various json processing on b2b user preference
 * 
 * @author Azitabh Ajit
 * 
 */

public class UserPreferenceProcessor {
    private static final JsonNode preferenceSahema;
    private static final JsonNode defaultPreference;
    private static final JsonUtil jsonUtil = new JsonUtil();

    static {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL preferenceSahemaUrl = classloader.getResource("b2b/userPreferenceSchema.json");
        URL defaultPreferenceUrl = classloader.getResource("b2b/userPreferenceDefault.json");

        try {
            File preferenceSahemaFile = new File(preferenceSahemaUrl.toURI());
            preferenceSahema = JsonLoader.fromFile(preferenceSahemaFile);

            File defaultPreferenceFile = new File(defaultPreferenceUrl.toURI());
            defaultPreference = JsonLoader.fromFile(defaultPreferenceFile);
        }
        catch (URISyntaxException | IOException e) {
            throw new ProAPIException(e);
        }
    }

    /**
     * checks if a given json is a valid user preference or not
     * 
     * @param JsonNode
     *            preference
     * 
     * @return boolean
     * 
     * @author Azitabh Ajit
     * 
     */
    public static boolean isValidPreference(JsonNode preference) {
        return jsonUtil.isValidSchema(preferenceSahema, preference);
    }

    /**
     * imposes a user preference over default user preference
     * 
     * @param JsonNode
     *            preference
     * 
     * @return JsonNode
     * 
     * @author Azitabh Ajit
     * 
     */
    public static JsonNode mergeDefaultPreference(JsonNode preference) {
        return JsonUtil.mergeJsonNode(defaultPreference, preference);
    }
}
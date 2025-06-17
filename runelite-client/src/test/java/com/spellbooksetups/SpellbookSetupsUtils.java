package com.spellbooksetups;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class SpellbookSetupsUtils
{
    private static final Gson GSON = new Gson();
    private static final Type TYPE = new TypeToken<Map<String, Map<String, SpellLayoutEntry>>>(){}.getType();

    public static Map<String, Map<String, SpellLayoutEntry>> decodeLayouts(String json)
    {
        if (json == null || json.isEmpty())
        {
            return new HashMap<>();
        }
        return GSON.fromJson(json, TYPE);
    }

    public static String encodeLayouts(Map<String, Map<String, SpellLayoutEntry>> layouts)
    {
        return GSON.toJson(layouts);
    }
}

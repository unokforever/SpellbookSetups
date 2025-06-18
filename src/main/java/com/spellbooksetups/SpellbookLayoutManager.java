package com.spellbooksetups;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

import java.lang.reflect.Type;
import java.util.*;

@Slf4j
public class SpellbookLayoutManager
{
    private static final String CONFIG_GROUP = "spellbooksetups";
    private static final String CONFIG_KEY = "savedLayouts";
    
    private final ConfigManager configManager;
    private final Gson gson;
    private Map<String, SpellbookLayout> layouts;

    public SpellbookLayoutManager(ConfigManager configManager)
    {
        this.configManager = configManager;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.layouts = new HashMap<>();
        loadLayouts();
    }

    public void saveLayout(String name, SpellbookLayout layout)
    {
        layouts.put(name, layout);
        persistLayouts();
        log.debug("Saved layout: {}", name);
    }

    public SpellbookLayout getLayout(String name)
    {
        return layouts.get(name);
    }

    public void deleteLayout(String name)
    {
        layouts.remove(name);
        persistLayouts();
        log.debug("Deleted layout: {}", name);
    }

    public boolean hasLayout(String name)
    {
        return layouts.containsKey(name);
    }

    public Set<String> getLayoutNames()
    {
        return new TreeSet<>(layouts.keySet());
    }

    private void loadLayouts()
    {
        try
        {
            String json = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY);
            if (json != null && !json.trim().isEmpty() && !json.equals("{}"))
            {
                Type type = new TypeToken<Map<String, SpellbookLayout>>(){}.getType();
                Map<String, SpellbookLayout> loadedLayouts = gson.fromJson(json, type);
                if (loadedLayouts != null)
                {
                    layouts = loadedLayouts;
                    log.debug("Loaded {} layouts from config", layouts.size());
                }
            }
        }
        catch (Exception e)
        {
            log.error("Failed to load layouts from config", e);
            layouts = new HashMap<>();
        }
    }

    private void persistLayouts()
    {
        try
        {
            String json = gson.toJson(layouts);
            configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY, json);
            log.debug("Persisted {} layouts to config", layouts.size());
        }
        catch (Exception e)
        {
            log.error("Failed to persist layouts to config", e);
        }
    }
}
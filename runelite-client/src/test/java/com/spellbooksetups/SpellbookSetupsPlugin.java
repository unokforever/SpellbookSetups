package com.spellbooksetups;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.GameStateChanged;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.plugins.Plugin;

@Slf4j
public class SpellbookSetupsPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ConfigManager configManager;

    @Inject
    private SpellbookSetupsConfig config;

    @Inject
    private SpellbookSetupsPanel panel;

    private NavigationButton navButton;
    private Map<String, Map<String, SpellLayoutEntry>> layouts = new HashMap<>();

    @Provides
    SpellbookSetupsConfig provideConfig(ConfigManager cm)
    {
        return cm.getConfig(SpellbookSetupsConfig.class);
    }

    @Override
    protected void startUp()
    {
        layouts = SpellbookSetupsUtils.decodeLayouts(config.layouts());
        panel.rebuild(new ArrayList<>(layouts.keySet()));
        navButton = NavigationButton.builder()
            .tooltip("Spellbook Setups")
            .panel(panel)
            .build();
        clientToolbar.addNavigation(navButton);
        String last = config.lastLayout();
        if (last != null && !last.isEmpty())
        {
            applyLayout(last);
        }
    }

    @Override
    protected void shutDown()
    {
        clientToolbar.removeNavigation(navButton);
    }

    @Subscribe
    public void onProfileChanged(ProfileChanged ev)
    {
        panel.rebuild(new ArrayList<>(layouts.keySet()));
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged ev)
    {
        // apply last layout on login
        if (ev.getGameState() == net.runelite.api.GameState.LOGGED_IN)
        {
            String last = config.lastLayout();
            if (last != null && !last.isEmpty())
            {
                applyLayout(last);
            }
        }
    }

    public List<String> getLayoutNames()
    {
        return new ArrayList<>(layouts.keySet());
    }

    public void saveLayout(String name)
    {
        Map<String, SpellLayoutEntry> layout = captureCurrentLayout();
        layouts.put(name, layout);
        persist(name);
        panel.rebuild(new ArrayList<>(layouts.keySet()));
    }

    public void applyLayout(String name)
    {
        Map<String, SpellLayoutEntry> layout = layouts.get(name);
        if (layout == null)
        {
            return;
        }
        clearSpellbookConfig();
        for (Map.Entry<String, SpellLayoutEntry> e : layout.entrySet())
        {
            SpellLayoutEntry entry = e.getValue();
            String key = e.getKey();
            configManager.setConfiguration(SpellbookConfig.GROUP, "spell_pos_book_" + key, entry.getPosition());
            if (entry.isHidden())
            {
                configManager.setConfiguration(SpellbookConfig.GROUP, "spell_hidden_book_" + key, true);
            }
        }
        persist(name);
        redrawSpellbook();
        panel.rebuild(new ArrayList<>(layouts.keySet()));
    }

    private void persist(String last)
    {
        configManager.setConfiguration(SpellbookSetupsConfig.GROUP, "layouts", SpellbookSetupsUtils.encodeLayouts(layouts));
        configManager.setConfiguration(SpellbookSetupsConfig.GROUP, "lastLayout", last);
    }

    private Map<String, SpellLayoutEntry> captureCurrentLayout()
    {
        Map<String, SpellLayoutEntry> layout = new HashMap<>();
        for (String key : configManager.getConfigurationKeys(SpellbookConfig.GROUP + ".spell_"))
        {
            String[] sp = key.split("\\.", 2);
            if (sp.length != 2)
            {
                continue;
            }
            String k = sp[1];
            String val = configManager.getConfiguration(SpellbookConfig.GROUP, k);
            if (val == null)
            {
                continue;
            }
            if (k.startsWith("spell_pos_book_"))
            {
                String id = k.substring("spell_pos_book_".length());
                SpellLayoutEntry e = layout.computeIfAbsent(id, x -> new SpellLayoutEntry());
                e.setPosition(Integer.parseInt(val));
            }
            else if (k.startsWith("spell_hidden_book_"))
            {
                String id = k.substring("spell_hidden_book_".length());
                SpellLayoutEntry e = layout.computeIfAbsent(id, x -> new SpellLayoutEntry());
                e.setHidden(Boolean.parseBoolean(val));
            }
        }
        return layout;
    }

    private void clearSpellbookConfig()
    {
        for (String key : configManager.getConfigurationKeys(SpellbookConfig.GROUP + ".spell_"))
        {
            String[] sp = key.split("\\.", 2);
            if (sp.length == 2)
            {
                configManager.unsetConfiguration(sp[0], sp[1]);
            }
        }
    }

    private void redrawSpellbook()
    {
        clientThread.invokeLater(() ->
        {
            Widget w = client.getWidget(InterfaceID.MagicSpellbook.UNIVERSE);
            if (w != null && w.getOnInvTransmitListener() != null)
            {
                client.createScriptEvent(w.getOnInvTransmitListener())
                    .setSource(w)
                    .run();
            }
        });
    }
}

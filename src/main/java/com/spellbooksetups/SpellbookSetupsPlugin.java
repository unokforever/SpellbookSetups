package com.spellbooksetups;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.*;

@Slf4j
@PluginDescriptor(
    name = "Spellbook Setups",
    description = "Save and load custom spellbook layouts",
    tags = {"spellbook", "magic", "layout", "setup"}
)
public class SpellbookSetupsPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private SpellbookSetupsConfig config;

    @Inject
    private ConfigManager configManager;

    @Inject
    private ClientToolbar clientToolbar;

    private SpellbookSetupsPanel panel;
    private NavigationButton navButton;
    private SpellbookLayoutManager layoutManager;
    private String lastAppliedLayout;

    @Override
    protected void startUp() throws Exception
    {
        layoutManager = new SpellbookLayoutManager(configManager);
        panel = new SpellbookSetupsPanel(this, layoutManager);
        
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/spellbook_icon.png");
        navButton = NavigationButton.builder()
            .tooltip("Spellbook Setups")
            .icon(icon != null ? icon : new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB))
            .priority(5)
            .panel(panel)
            .build();

        clientToolbar.addNavigation(navButton);
        
        lastAppliedLayout = config.lastAppliedLayout();
        
        log.info("Spellbook Setups plugin started");
    }

    @Override
    protected void shutDown() throws Exception
    {
        clientToolbar.removeNavigation(navButton);
        panel = null;
        navButton = null;
        layoutManager = null;
        log.info("Spellbook Setups plugin stopped");
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
        {
            if (lastAppliedLayout != null && !lastAppliedLayout.isEmpty())
            {
                SwingUtilities.invokeLater(() -> {
                    Timer timer = new Timer(2000, e -> {
                        applyLayout(lastAppliedLayout);
                        ((Timer) e.getSource()).stop();
                    });
                    timer.setRepeats(false);
                    timer.start();
                });
            }
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded widgetLoaded)
    {
        if (isSpellbookWidget(widgetLoaded.getGroupId()))
        {
            SwingUtilities.invokeLater(() -> {
                if (panel != null)
                {
                    panel.updateCurrentLayoutDisplay();
                }
            });
        }
    }

    public void saveCurrentLayout(String layoutName)
    {
        if (layoutName == null || layoutName.trim().isEmpty())
        {
            return;
        }

        SpellbookLayout layout = captureCurrentLayout();
        if (layout != null)
        {
            layoutManager.saveLayout(layoutName, layout);
            panel.refreshLayoutList();
            log.info("Saved spellbook layout: {}", layoutName);
        }
        else
        {
            log.warn("Failed to capture current spellbook layout");
        }
    }

    public void applyLayout(String layoutName)
    {
        SpellbookLayout layout = layoutManager.getLayout(layoutName);
        if (layout == null)
        {
            log.warn("Layout not found: {}", layoutName);
            return;
        }

        if (applyLayoutToSpellbook(layout))
        {
            lastAppliedLayout = layoutName;
            configManager.setConfiguration("spellbooksetups", "lastAppliedLayout", layoutName);
            panel.updateCurrentLayoutDisplay();
            log.info("Applied spellbook layout: {}", layoutName);
        }
        else
        {
            log.warn("Failed to apply spellbook layout: {}", layoutName);
        }
    }

    public void deleteLayout(String layoutName)
    {
        layoutManager.deleteLayout(layoutName);
        panel.refreshLayoutList();
        
        if (layoutName.equals(lastAppliedLayout))
        {
            lastAppliedLayout = null;
            configManager.setConfiguration("spellbooksetups", "lastAppliedLayout", "");
        }
        
        log.info("Deleted spellbook layout: {}", layoutName);
    }

    private SpellbookLayout captureCurrentLayout()
    {
        Widget spellbookWidget = getSpellbookWidget();
        if (spellbookWidget == null)
        {
            return null;
        }

        SpellbookLayout layout = new SpellbookLayout();
        Widget[] spellWidgets = spellbookWidget.getChildren();
        
        if (spellWidgets != null)
        {
            for (Widget spell : spellWidgets)
            {
                if (spell != null && isSpellWidget(spell))
                {
                    SpellData spellData = new SpellData();
                    spellData.setSpellId(spell.getId());
                    spellData.setRelativeX(spell.getRelativeX());
                    spellData.setRelativeY(spell.getRelativeY());
                    spellData.setHidden(spell.isHidden());
                    spellData.setOriginalX(spell.getOriginalX());
                    spellData.setOriginalY(spell.getOriginalY());
                    
                    layout.getSpells().add(spellData);
                }
            }
        }

        return layout.getSpells().isEmpty() ? null : layout;
    }

    private boolean applyLayoutToSpellbook(SpellbookLayout layout)
    {
        Widget spellbookWidget = getSpellbookWidget();
        if (spellbookWidget == null)
        {
            return false;
        }

        Widget[] spellWidgets = spellbookWidget.getChildren();
        if (spellWidgets == null)
        {
            return false;
        }

        Map<Integer, SpellData> spellDataMap = new HashMap<>();
        for (SpellData spellData : layout.getSpells())
        {
            spellDataMap.put(spellData.getSpellId(), spellData);
        }

        for (Widget spell : spellWidgets)
        {
            if (spell != null && isSpellWidget(spell))
            {
                SpellData spellData = spellDataMap.get(spell.getId());
                if (spellData != null)
                {
                    spell.setRelativeX(spellData.getRelativeX());
                    spell.setRelativeY(spellData.getRelativeY());
                    spell.setHidden(spellData.isHidden());
                }
            }
        }

        return true;
    }

    private Widget getSpellbookWidget()
    {
        Widget widget = client.getWidget(WidgetInfo.SPELLBOOK);
        if (widget != null)
        {
            return widget;
        }

        int[] spellbookGroups = {218, 201, 192};
        for (int groupId : spellbookGroups)
        {
            widget = client.getWidget(groupId, 0);
            if (widget != null && widget.getChildren() != null)
            {
                return widget;
            }
        }

        return null;
    }

    private boolean isSpellbookWidget(int groupId)
    {
        int[] spellbookGroups = {218, 201, 192};
        for (int id : spellbookGroups)
        {
            if (id == groupId)
            {
                return true;
            }
        }
        return false;
    }

    private boolean isSpellWidget(Widget widget)
    {
        return widget.getType() == 5 && 
               widget.getWidth() > 0 && 
               widget.getHeight() > 0;
    }

    public String getLastAppliedLayout()
    {
        return lastAppliedLayout;
    }

    @Provides
    SpellbookSetupsConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(SpellbookSetupsConfig.class);
    }
}
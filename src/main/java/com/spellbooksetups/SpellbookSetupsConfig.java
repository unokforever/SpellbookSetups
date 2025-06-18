package com.spellbooksetups;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("spellbooksetups")
public interface SpellbookSetupsConfig extends Config
{
    @ConfigItem(
        keyName = "savedLayouts",
        name = "Saved Layouts",
        description = "JSON data of saved spellbook layouts"
    )
    default String savedLayouts()
    {
        return "{}";
    }

    @ConfigItem(
        keyName = "lastAppliedLayout",
        name = "Last Applied Layout",
        description = "Name of the last applied layout"
    )
    default String lastAppliedLayout()
    {
        return "";
    }

    @ConfigItem(
        keyName = "autoApplyOnLogin",
        name = "Auto-apply on login",
        description = "Automatically apply the last used layout when logging in"
    )
    default boolean autoApplyOnLogin()
    {
        return true;
    }
}
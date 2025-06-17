package com.spellbooksetups;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(SpellbookSetupsConfig.GROUP)
public interface SpellbookSetupsConfig extends Config
{
    String GROUP = "spellbooksetups";

    @ConfigItem(
        keyName = "layouts",
        name = "Layouts",
        description = "Stored spellbook layouts",
        hidden = true
    )
    default String layouts()
    {
        return "";
    }

    @ConfigItem(
        keyName = "lastLayout",
        name = "Last Layout",
        description = "Last applied layout",
        hidden = true
    )
    default String lastLayout()
    {
        return "";
    }
}

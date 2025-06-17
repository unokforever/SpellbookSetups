package com.spellbooksetups;

import net.runelite.client.externalplugins.ExternalPluginManager;
import net.runelite.client.RuneLite;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(name = "Spellbook Setups")
public class SpellbookSetupsTestPlugin extends SpellbookSetupsPlugin
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(SpellbookSetupsTestPlugin.class);
        RuneLite.main(args);
    }
}

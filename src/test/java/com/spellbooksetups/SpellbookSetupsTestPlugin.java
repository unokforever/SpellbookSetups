package com.spellbooksetups;

import net.runelite.client.plugins.PluginDescriptor;

/**
 * Test wrapper for the Spellbook Setups plugin.
 * This allows the plugin to be loaded for testing without requiring registration.
 */
@PluginDescriptor(
    name = "Spellbook Setups Test",
    description = "Test version of Spellbook Setups plugin for development",
    tags = {"spellbook", "magic", "layout", "setup", "test"},
    enabledByDefault = false
)
public class SpellbookSetupsTestPlugin extends SpellbookSetupsPlugin
{
    // This class inherits all functionality from SpellbookSetupsPlugin
    // It exists solely to provide a separate PluginDescriptor for testing
}
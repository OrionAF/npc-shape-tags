package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
    name = "Combat State Utility",
    description = "Tracks NPC tags and player combat state",
    tags = {"combat", "overlay", "pvm", "utility"}
)
public class CombatStatePlugin extends Plugin
{
    @Inject private OverlayManager overlayManager;
    @Inject private ShapeOverlay shapeOverlay;
    @Inject private StatusOverlay statusOverlay;
    @Inject private CombatStateConfig config;

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(shapeOverlay);
        overlayManager.add(statusOverlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(shapeOverlay);
        overlayManager.remove(statusOverlay);
    }

    @Provides
    CombatStateConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(CombatStateConfig.class);
    }
}

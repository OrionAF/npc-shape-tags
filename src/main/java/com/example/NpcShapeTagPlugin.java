package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
    name = "NPC Shape Tags",
    description = "Tags NPCs with custom shapes and tracks status",
    tags = {"npc", "highlight", "tag", "shape", "status"}
)
public class NpcShapeTagPlugin extends Plugin
{
    @Inject
    private OverlayManager overlayManager;

    @Inject
    private NpcShapeTagOverlay shapeOverlay;

    // ADD THIS LINE
    @Inject
    private NpcStatusOverlay statusOverlay;

    @Inject
    private NpcShapeTagConfig config;

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(shapeOverlay);
        // ADD THIS LINE
        overlayManager.add(statusOverlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(shapeOverlay);
        // ADD THIS LINE
        overlayManager.remove(statusOverlay);
    }

    @Provides
    NpcShapeTagConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(NpcShapeTagConfig.class);
    }
}

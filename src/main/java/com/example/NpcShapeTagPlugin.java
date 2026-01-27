package net.runelite.client.plugins.npcshapetags;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
    name = "NPC Shape Tags",
    description = "Tags NPCs with custom shapes (Square, Circle, Star)",
    tags = {"npc", "highlight", "tag", "shape"}
)
public class NpcShapeTagPlugin extends Plugin
{
    @Inject
    private OverlayManager overlayManager;

    @Inject
    private NpcShapeTagOverlay overlay;

    @Inject
    private NpcShapeTagConfig config;

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(overlay);
    }

    @Provides
    NpcShapeTagConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(NpcShapeTagConfig.class);
    }
}

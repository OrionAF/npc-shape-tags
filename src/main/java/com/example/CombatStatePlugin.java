package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
    name = "Combat State Utility",
    description = "Tracks NPC tags, object tags, and player combat state",
    tags = {"combat", "overlay", "pvm", "utility"}
)
public class CombatStatePlugin extends Plugin
{
    @Inject private Client client;
    @Inject private OverlayManager overlayManager;
    @Inject private ShapeOverlay shapeOverlay;
    @Inject private StatusOverlay statusOverlay;
    @Inject private CombatStateConfig config;

    // Tracker Variables
    private int xpDropCount = 0;
    private LocalPoint lastObjectClickPos = null;
    private int lastObjectClickId = -1;

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

    @Subscribe
    public void onStatChanged(StatChanged event)
    {
        // Increment counter on any XP drop
        xpDropCount++;
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        // Logic to track if we clicked a Game Object
        MenuAction action = event.getMenuAction();
        
        if (action == MenuAction.GAME_OBJECT_FIRST_OPTION ||
            action == MenuAction.GAME_OBJECT_SECOND_OPTION ||
            action == MenuAction.GAME_OBJECT_THIRD_OPTION ||
            action == MenuAction.GAME_OBJECT_FOURTH_OPTION ||
            action == MenuAction.GAME_OBJECT_FIFTH_OPTION)
        {
            // Store the ID and Location of the object we just clicked
            lastObjectClickId = event.getId();
            
            // Convert scene X/Y (param0/param1) to LocalPoint
            int sceneX = event.getParam0();
            int sceneY = event.getParam1();
            lastObjectClickPos = LocalPoint.fromScene(sceneX, sceneY, client.getScene());
        }
        else if (action == MenuAction.WALK || action == MenuAction.WIDGET_TARGET_ON_WIDGET)
        {
            // If we walk somewhere else or click UI, clear the hidden object
            lastObjectClickPos = null;
            lastObjectClickId = -1;
        }
    }

    // Getters for the Overlays to use
    public int getXpDropCount()
    {
        return xpDropCount;
    }

    public boolean isObjectHidden(TileObject obj)
    {
        if (lastObjectClickPos == null || obj == null) return false;
        
        // If IDs match AND locations match, it's the one we clicked
        return obj.getId() == lastObjectClickId && 
               obj.getLocalLocation().distanceTo(lastObjectClickPos) < 1; // Strict match
    }

    @Provides
    CombatStateConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(CombatStateConfig.class);
    }
}

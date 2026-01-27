package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import java.awt.Color;

@ConfigGroup("combatstate")
public interface CombatStateConfig extends Config
{
    enum Shape
    {
        SQUARE, CIRCLE, STAR
    }

    // --- Shape Tag Settings ---
    @ConfigItem(keyName = "npcNames", name = "NPC Names", description = "NPCs to tag", position = 1)
    default String npcNames() { return ""; }

    @ConfigItem(keyName = "shape", name = "Shape", description = "Shape to render", position = 2)
    default Shape shape() { return Shape.STAR; }

    @ConfigItem(keyName = "color", name = "Color", description = "Shape color", position = 3)
    default Color color() { return Color.CYAN; }

    @ConfigItem(keyName = "size", name = "Size", description = "Shape size", position = 4)
    default int size() { return 20; }

    @ConfigItem(keyName = "filled", name = "Fill Shape", description = "Fill the shape?", position = 5)
    default boolean filled() { return false; }

    // --- Status Window Settings ---
    @ConfigItem(keyName = "foodNames", name = "Food Names", description = "Food to check for", position = 6)
    default String foodNames() { return "Shark,Manta ray,Cooked karambwan"; }

    @ConfigItem(keyName = "groundItemNames", name = "Ground Items", description = "Ground items to check for", position = 7)
    default String groundItemNames() { return "Coins,Bones"; }

    @ConfigItem(keyName = "overlayScale", name = "Overlay Scale", description = "Adjust the size of the status window (50% to 200%)", position = 8)
    default int overlayScale() { return 100; }
}

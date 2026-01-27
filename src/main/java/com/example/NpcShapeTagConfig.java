package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import java.awt.Color;

@ConfigGroup("npcshapetags")
public interface NpcShapeTagConfig extends Config
{
    enum Shape
    {
        SQUARE,
        CIRCLE,
        STAR
    }

    @ConfigItem(
        keyName = "npcNames",
        name = "NPC Names",
        description = "List of NPC names to tag, separated by commas (e.g. Goblin, Man)",
        position = 1
    )
    default String npcNames()
    {
        return "";
    }

    @ConfigItem(
        keyName = "shape",
        name = "Shape",
        description = "The shape to render on the NPC",
        position = 2
    )
    default Shape shape()
    {
        return Shape.STAR;
    }

    @ConfigItem(
        keyName = "color",
        name = "Color",
        description = "Color of the shape",
        position = 3
    )
    default Color color()
    {
        return Color.CYAN;
    }

    @ConfigItem(
        keyName = "size",
        name = "Shape Size",
        description = "The size/radius of the shape",
        position = 4
    )
    default int size()
    {
        return 20;
    }

    @ConfigItem(
        keyName = "filled",
        name = "Fill Shape",
        description = "Check to fill the shape with color, uncheck for outline only",
        position = 5
    )
    default boolean filled()
    {
        return false;
    }

    @ConfigItem(
        keyName = "foodNames",
        name = "Food Names",
        description = "Comma separated list of food names to check for (e.g. Shark, Manta ray)",
        position = 6
    )
    default String foodNames()
    {
        return "Shark,Manta ray,Cooked karambwan";
    }

    @ConfigItem(
        keyName = "groundItemNames",
        name = "Ground Items",
        description = "Comma separated list of ground items to check for",
        position = 7
    )
    default String groundItemNames()
    {
        return "Coins,Bones";
    }
}

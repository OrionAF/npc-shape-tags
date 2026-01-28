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

    @ConfigItem(keyName = "foodNames", name = "Food Names", description = "Food to check for", position = 6)
    default String foodNames() { return "Cooked Karambwan, Lobster, Swordfish, Corrupted shark"; }

    @ConfigItem(keyName = "groundItemNames", name = "Ground Items", description = "Ground items to check for", position = 7)
    default String groundItemNames() { return "Coins,Bones"; }

    @ConfigItem(keyName = "overlayScale", name = "Overlay Scale", description = "Adjust the size of the status window (50% to 200%)", position = 8)
    default int overlayScale() { return 100; }

    @ConfigItem(keyName = "hideInCombat", name = "Hide NPCs in Combat", description = "Do not tag NPCs that are currently interacting or have a health bar visible", position = 9)
    default boolean hideInCombat() { return false; }

    @ConfigItem(keyName = "groundItemTags", name = "Tag Ground Items", description = "List of ground items to visually tag with a shape", position = 10)
    default String groundItemTags() { return ""; }

    @ConfigItem(keyName = "objectTags", name = "Tag Objects", description = "List of game objects to visually tag (e.g. Altar, Tree)", position = 11)
    default String objectTags() { return ""; }

    @ConfigItem(keyName = "showInCombat", name = "Show 'In Combat'", description = "Toggle the In Combat status", position = 20)
    default boolean showInCombat() { return true; }

    @ConfigItem(keyName = "showTargeting", name = "Show 'Targeting?'", description = "Toggle the Targeting status", position = 21)
    default boolean showTargeting() { return true; }

    @ConfigItem(keyName = "showCorrectTarget", name = "Show 'Correct Target'", description = "Toggle the Correct Target status", position = 22)
    default boolean showCorrectTarget() { return true; }

    @ConfigItem(keyName = "showGroundItems", name = "Show 'Items on Ground'", description = "Toggle the Items on Ground status", position = 23)
    default boolean showGroundItems() { return true; }

    @ConfigItem(keyName = "showHp", name = "Show 'HP > 50%'", description = "Toggle the HP status", position = 24)
    default boolean showHp() { return true; }

    @ConfigItem(keyName = "showFood", name = "Show 'Has Food'", description = "Toggle the Food status", position = 25)
    default boolean showFood() { return true; }

    @ConfigItem(keyName = "showPrayer", name = "Show 'Prayer > 50%'", description = "Toggle the Prayer > 50% status", position = 26)
    default boolean showPrayer() { return true; }

    @ConfigItem(keyName = "showOutOfPrayer", name = "Show 'Out of Prayer'", description = "Toggle the Out of Prayer status", position = 27)
    default boolean showOutOfPrayer() { return true; }

    @ConfigItem(keyName = "showIdle", name = "Show 'Idle'", description = "Toggle the Idle status", position = 28)
    default boolean showIdle() { return true; }

    @ConfigItem(keyName = "showInvFull", name = "Show 'Inv Full'", description = "Toggle the Inventory Full status", position = 29)
    default boolean showInvFull() { return true; }
}

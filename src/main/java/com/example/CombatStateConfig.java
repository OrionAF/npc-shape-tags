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

    // ------------------------------------------------------------
    // NPC Visual Settings
    // ------------------------------------------------------------

    @ConfigItem(keyName = "npcNames", name = "NPC Names", description = "NPCs to tag (logic also uses this)", position = 1)
    default String npcNames() { return ""; }

    @ConfigItem(keyName = "npcShape", name = "NPC Shape", description = "Shape for NPCs", position = 2)
    default Shape npcShape() { return Shape.STAR; }

    @ConfigItem(keyName = "npcColor", name = "NPC Color", description = "Color for NPCs", position = 3)
    default Color npcColor() { return Color.CYAN; }

    @ConfigItem(keyName = "npcSize", name = "NPC Size", description = "Size for NPC shapes", position = 4)
    default int npcSize() { return 20; }

    @ConfigItem(keyName = "npcFilled", name = "NPC Fill", description = "Fill the NPC shape?", position = 5)
    default boolean npcFilled() { return false; }

    @ConfigItem(keyName = "hideInCombat", name = "Hide NPCs in Combat", description = "Do not tag NPCs that are currently interacting", position = 6)
    default boolean hideInCombat() { return false; }


    // ------------------------------------------------------------
    // Ground Item Visual Settings
    // ------------------------------------------------------------

    @ConfigItem(keyName = "groundItemTags", name = "Ground Item Tags", description = "List of ground items to visually tag", position = 10)
    default String groundItemTags() { return ""; }

    @ConfigItem(keyName = "groundItemShape", name = "Item Shape", description = "Shape for Ground Items", position = 11)
    default Shape groundItemShape() { return Shape.SQUARE; }

    @ConfigItem(keyName = "groundItemColor", name = "Item Color", description = "Color for Ground Items", position = 12)
    default Color groundItemColor() { return Color.ORANGE; }

    @ConfigItem(keyName = "groundItemSize", name = "Item Size", description = "Size for Ground Item shapes", position = 13)
    default int groundItemSize() { return 15; }

    @ConfigItem(keyName = "groundItemFilled", name = "Item Fill", description = "Fill the Ground Item shape?", position = 14)
    default boolean groundItemFilled() { return false; }


    // ------------------------------------------------------------
    // Object Visual Settings
    // ------------------------------------------------------------

    @ConfigItem(keyName = "objectTags", name = "Object Tags", description = "List of objects to visually tag", position = 20)
    default String objectTags() { return ""; }

    @ConfigItem(keyName = "objectShape", name = "Object Shape", description = "Shape for Objects", position = 21)
    default Shape objectShape() { return Shape.CIRCLE; }

    @ConfigItem(keyName = "objectColor", name = "Object Color", description = "Color for Objects", position = 22)
    default Color objectColor() { return Color.GREEN; }

    @ConfigItem(keyName = "objectSize", name = "Object Size", description = "Size for Object shapes", position = 23)
    default int objectSize() { return 30; }

    @ConfigItem(keyName = "objectFilled", name = "Object Fill", description = "Fill the Object shape?", position = 24)
    default boolean objectFilled() { return false; }


    // ------------------------------------------------------------
    // Status Window Logic
    // ------------------------------------------------------------

    @ConfigItem(keyName = "foodNames", name = "Food Names (Logic)", description = "Food to check for status box", position = 30)
    default String foodNames() { return "Cooked Karambwan, Lobster, Swordfish, Corrupted shark"; }

    @ConfigItem(keyName = "groundItemNames", name = "Ground Items (Logic)", description = "Ground items to check for status box", position = 31)
    default String groundItemNames() { return "Coins,Bones"; }

    @ConfigItem(keyName = "overlayScale", name = "Overlay Scale", description = "Adjust the size of the status window", position = 32)
    default int overlayScale() { return 100; }

    @ConfigItem(keyName = "showInCombat", name = "Show 'In Combat'", description = "Toggle status", position = 40)
    default boolean showInCombat() { return true; }

    @ConfigItem(keyName = "showTargeting", name = "Show 'Targeting?'", description = "Toggle status", position = 41)
    default boolean showTargeting() { return true; }

    @ConfigItem(keyName = "showCorrectTarget", name = "Show 'Correct Target'", description = "Toggle status", position = 42)
    default boolean showCorrectTarget() { return true; }

    @ConfigItem(keyName = "showGroundItems", name = "Show 'Items on Ground'", description = "Toggle status", position = 43)
    default boolean showGroundItems() { return true; }

    @ConfigItem(keyName = "showHp", name = "Show 'HP > 50%'", description = "Toggle status", position = 44)
    default boolean showHp() { return true; }

    @ConfigItem(keyName = "showFood", name = "Show 'Has Food'", description = "Toggle status", position = 45)
    default boolean showFood() { return true; }

    @ConfigItem(keyName = "showPrayer", name = "Show 'Prayer > 50%'", description = "Toggle status", position = 46)
    default boolean showPrayer() { return true; }

    @ConfigItem(keyName = "showOutOfPrayer", name = "Show 'Out of Prayer'", description = "Toggle status", position = 47)
    default boolean showOutOfPrayer() { return true; }

    @ConfigItem(keyName = "showIdle", name = "Show 'Idle'", description = "Toggle status", position = 48)
    default boolean showIdle() { return true; }

    @ConfigItem(keyName = "showInvFull", name = "Show 'Inv Full'", description = "Toggle status", position = 49)
    default boolean showInvFull() { return true; }
}

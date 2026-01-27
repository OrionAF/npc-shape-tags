package com.example;

import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class NpcStatusOverlay extends Overlay
{
    private final Client client;
    private final NpcShapeTagConfig config;

    // Visual Settings
    private static final int CELL_WIDTH = 100;
    private static final int CELL_HEIGHT = 45;
    private static final int BOX_SIZE = 15;
    private static final Color COLOR_TRUE = Color.WHITE;
    private static final Color COLOR_FALSE = Color.RED;
    private static final Color BG_COLOR = new Color(0, 0, 0, 150);

    @Inject
    private NpcStatusOverlay(Client client, NpcShapeTagConfig config)
    {
        this.client = client;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        // 1. Calculate all states
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null) return null;

        boolean inCombat = localPlayer.getInteracting() != null;
        boolean isAttacking = localPlayer.getAnimation() != -1 && localPlayer.getAnimation() != 808; // 808 is generic idle
        boolean correctTarget = isAttackingCorrectTarget(localPlayer);
        boolean itemsOnGround = areItemsOnGround();
        boolean healthAbove50 = client.getBoostedSkillLevel(Skill.HITPOINTS) > (client.getRealSkillLevel(Skill.HITPOINTS) / 2);
        boolean hasFood = checkInventoryFor(config.foodNames());
        boolean prayerAbove50 = client.getBoostedSkillLevel(Skill.PRAYER) > (client.getRealSkillLevel(Skill.PRAYER) / 2);
        boolean outOfPrayer = client.getBoostedSkillLevel(Skill.PRAYER) == 0;
        boolean isIdle = localPlayer.getAnimation() == -1 || localPlayer.getAnimation() == 808; // 808 is generic idle
        boolean invFull = isInventoryFull();

        // 2. Prepare list of cells to draw
        List<StatusCell> cells = new ArrayList<>();
        cells.add(new StatusCell("In Combat?", inCombat));
        cells.add(new StatusCell("Attacking?", isAttacking));
        cells.add(new StatusCell("Correct Target?", correctTarget));
        cells.add(new StatusCell("Items on Ground?", itemsOnGround));
        cells.add(new StatusCell("HP > 50%?", healthAbove50));
        cells.add(new StatusCell("Has Food?", hasFood));
        cells.add(new StatusCell("Prayer > 50%?", prayerAbove50));
        cells.add(new StatusCell("Out of Prayer?", outOfPrayer));
        cells.add(new StatusCell("Idle?", isIdle));
        cells.add(new StatusCell("Inv Full?", invFull));

        // 3. Render the Grid
        int totalWidth = cells.size() * CELL_WIDTH;
        
        // Draw Background
        graphics.setColor(BG_COLOR);
        graphics.fillRect(0, 0, totalWidth, CELL_HEIGHT);
        graphics.drawRect(0, 0, totalWidth, CELL_HEIGHT);

        for (int i = 0; i < cells.size(); i++)
        {
            StatusCell cell = cells.get(i);
            int xPos = i * CELL_WIDTH;

            // Draw status box (Centered horizontally in the cell)
            int boxX = xPos + (CELL_WIDTH / 2) - (BOX_SIZE / 2);
            int boxY = 5;
            
            graphics.setColor(cell.isActive ? COLOR_TRUE : COLOR_FALSE);
            graphics.fillRect(boxX, boxY, BOX_SIZE, BOX_SIZE);
            graphics.setColor(Color.BLACK); // Border for the box
            graphics.drawRect(boxX, boxY, BOX_SIZE, BOX_SIZE);

            // Draw Text (Centered below box)
            graphics.setColor(Color.WHITE);
            drawCenteredString(graphics, cell.label, xPos, 35, CELL_WIDTH);

            // Draw vertical divider line
            graphics.setColor(Color.GRAY);
            graphics.drawLine(xPos + CELL_WIDTH, 0, xPos + CELL_WIDTH, CELL_HEIGHT);
        }

        return new Dimension(totalWidth, CELL_HEIGHT);
    }

    // --- Helper Methods ---

    private boolean isAttackingCorrectTarget(Player player)
    {
        if (player.getInteracting() == null) return false;
        String targetName = Text.removeTags(player.getInteracting().getName()).toUpperCase();
        List<String> configNames = Text.fromCSV(config.npcNames());
        
        return configNames.stream().anyMatch(n -> WildcardMatcher.matches(n.toUpperCase(), targetName));
    }

    private boolean isInventoryFull()
    {
        ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
        return container != null && container.count() == 28;
    }

    private boolean checkInventoryFor(String configList)
    {
        ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
        if (container == null) return false;
        
        List<String> namesToCheck = Text.fromCSV(configList.toUpperCase());
        
        for (Item item : container.getItems())
        {
            if (item.getId() == -1) continue;
            // Note: In an RSPS/External setup, getting item name by ID might fail if cache isn't loaded.
            // But we try best effort.
            String itemName = client.getItemDefinition(item.getId()).getName(); 
            if (itemName == null) continue;

            if (namesToCheck.stream().anyMatch(n -> WildcardMatcher.matches(n, itemName.toUpperCase())))
            {
                return true;
            }
        }
        return false;
    }

    private boolean areItemsOnGround()
    {
        List<String> namesToCheck = Text.fromCSV(config.groundItemNames().toUpperCase());
        int range = 10; // Check 10 tile radius
        LocalPoint lp = client.getLocalPlayer().getLocalLocation();

        for (int x = -range; x <= range; x++)
        {
            for (int y = -range; y <= range; y++)
            {
                Tile tile = client.getScene().getTiles()[client.getPlane()][client.getBaseX() + lp.getSceneX() + x][client.getBaseY() + lp.getSceneY() + y];
                if (tile == null || tile.getGroundItems() == null) continue;

                for (TileItem ti : tile.getGroundItems())
                {
                    String itemName = client.getItemDefinition(ti.getId()).getName();
                    if (itemName != null && namesToCheck.stream().anyMatch(n -> WildcardMatcher.matches(n, itemName.toUpperCase())))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void drawCenteredString(Graphics2D g, String text, int x, int y, int width)
    {
        FontMetrics metrics = g.getFontMetrics();
        int textX = x + (width - metrics.stringWidth(text)) / 2;
        g.drawString(text, textX, y);
    }

    // Helper class to hold data
    private static class StatusCell
    {
        String label;
        boolean isActive;

        StatusCell(String label, boolean isActive)
        {
            this.label = label;
            this.isActive = isActive;
        }
    }
}

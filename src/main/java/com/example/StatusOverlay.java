package com.example;

import net.runelite.api.*;
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
import java.util.ArrayList;
import java.util.List;

public class StatusOverlay extends Overlay
{
    private final Client client;
    private final CombatStateConfig config;

    private static final int CELL_WIDTH = 100;
    private static final int CELL_HEIGHT = 45;
    private static final int BOX_SIZE = 15;
    private static final Color COLOR_TRUE = Color.WHITE;
    private static final Color COLOR_FALSE = Color.RED;
    private static final Color BG_COLOR = new Color(0, 0, 0, 150);

    @Inject
    private StatusOverlay(Client client, CombatStateConfig config)
    {
        this.client = client;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null) return null;

        boolean inCombat = localPlayer.getInteracting() != null;
        boolean isAttacking = localPlayer.getAnimation() != -1 && localPlayer.getAnimation() != 808;
        boolean correctTarget = isAttackingCorrectTarget(localPlayer);
        boolean itemsOnGround = areItemsOnGround();
        boolean healthAbove50 = client.getBoostedSkillLevel(Skill.HITPOINTS) > (client.getRealSkillLevel(Skill.HITPOINTS) / 2);
        boolean hasFood = checkInventoryFor(config.foodNames());
        boolean prayerAbove50 = client.getBoostedSkillLevel(Skill.PRAYER) > (client.getRealSkillLevel(Skill.PRAYER) / 2);
        boolean outOfPrayer = client.getBoostedSkillLevel(Skill.PRAYER) == 0;
        boolean isIdle = localPlayer.getAnimation() == -1 || localPlayer.getAnimation() == 808;
        boolean invFull = isInventoryFull();

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

        int totalWidth = cells.size() * CELL_WIDTH;
        graphics.setColor(BG_COLOR);
        graphics.fillRect(0, 0, totalWidth, CELL_HEIGHT);
        graphics.drawRect(0, 0, totalWidth, CELL_HEIGHT);

        for (int i = 0; i < cells.size(); i++)
        {
            StatusCell cell = cells.get(i);
            int xPos = i * CELL_WIDTH;
            int boxX = xPos + (CELL_WIDTH / 2) - (BOX_SIZE / 2);
            int boxY = 5;

            graphics.setColor(cell.isActive ? COLOR_TRUE : COLOR_FALSE);
            graphics.fillRect(boxX, boxY, BOX_SIZE, BOX_SIZE);
            graphics.setColor(Color.BLACK);
            graphics.drawRect(boxX, boxY, BOX_SIZE, BOX_SIZE);

            graphics.setColor(Color.WHITE);
            drawCenteredString(graphics, cell.label, xPos, 35, CELL_WIDTH);

            graphics.setColor(Color.GRAY);
            graphics.drawLine(xPos + CELL_WIDTH, 0, xPos + CELL_WIDTH, CELL_HEIGHT);
        }
        return new Dimension(totalWidth, CELL_HEIGHT);
    }

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
            if (configList == null || configList.isEmpty()) return false;
            
            ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
            if (container == null) return false;
            
            List<String> namesToCheck = Text.fromCSV(configList.toUpperCase());
            
            for (Item item : container.getItems())
            {
                if (item == null || item.getId() <= 0) continue;
                
                ItemComposition def = client.getItemDefinition(item.getId());
                if (def == null) continue;
                
                String itemName = def.getName();
                if (itemName != null && namesToCheck.stream().anyMatch(n -> WildcardMatcher.matches(n, itemName.toUpperCase())))
                {
                    return true;
                }
            }
            return false;
        }

    private boolean areItemsOnGround()
        {
            String groundConfig = config.groundItemNames();
            if (groundConfig == null || groundConfig.isEmpty()) return false;
    
            List<String> namesToCheck = Text.fromCSV(groundConfig.toUpperCase());
            LocalPoint lp = client.getLocalPlayer().getLocalLocation();
            if (lp == null) return false;
    
            int sceneX = lp.getSceneX();
            int sceneY = lp.getSceneY();
            int range = 15; // Increased range to 15 tiles
            int plane = client.getPlane();
            Tile[][][] tiles = client.getScene().getTiles();
    
            for (int dx = -range; dx <= range; dx++)
            {
                for (int dy = -range; dy <= range; dy++)
                {
                    int x = sceneX + dx;
                    int y = sceneY + dy;
    
                    // Ensure we stay within the 0-103 scene boundaries
                    if (x < 0 || x >= 104 || y < 0 || y >= 104) continue;
    
                    Tile tile = tiles[plane][x][y];
                    if (tile == null || tile.getGroundItems() == null) continue;
    
                    for (TileItem ti : tile.getGroundItems())
                    {
                        ItemComposition def = client.getItemDefinition(ti.getId());
                        if (def != null)
                        {
                            String itemName = def.getName();
                            if (itemName != null && namesToCheck.stream().anyMatch(n -> WildcardMatcher.matches(n, itemName.toUpperCase())))
                            {
                                return true;
                            }
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

    private static class StatusCell
    {
        String label; boolean isActive;
        StatusCell(String label, boolean isActive) { this.label = label; this.isActive = isActive; }
    }
}

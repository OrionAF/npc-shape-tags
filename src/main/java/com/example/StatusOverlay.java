package com.example;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;
import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StatusOverlay extends Overlay
{
    private final Client client;
    private final CombatStateConfig config;

    private static final int BASE_CELL_WIDTH = 100;
    private static final int BASE_CELL_HEIGHT = 45;
    private static final int BASE_BOX_SIZE = 15;
    private static final int BASE_FONT_SIZE = 12;

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

        double scale = config.overlayScale() / 100.0;
        int cellWidth = (int) (BASE_CELL_WIDTH * scale);
        int cellHeight = (int) (BASE_CELL_HEIGHT * scale);
        int boxSize = (int) (BASE_BOX_SIZE * scale);
        int fontSize = (int) (BASE_FONT_SIZE * scale);

        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));

        List<StatusCell> cells = new ArrayList<>();

        if (config.showInCombat()) {
            boolean inCombat = localPlayer.getInteracting() != null;
            cells.add(new StatusCell("In Combat?", inCombat));
        }

        if (config.showTargeting()) {
            boolean isTargeting = localPlayer.getInteracting() instanceof NPC;
            cells.add(new StatusCell("Targeting?", isTargeting));
        }

        if (config.showCorrectTarget()) {
            boolean correctTarget = isAttackingCorrectTarget(localPlayer);
            cells.add(new StatusCell("Correct Target?", correctTarget));
        }

        if (config.showGroundItems()) {
            boolean itemsOnGround = areItemsOnGround();
            cells.add(new StatusCell("Items on Ground?", itemsOnGround));
        }

        if (config.showHp()) {
            boolean healthAbove50 = client.getBoostedSkillLevel(Skill.HITPOINTS) > (client.getRealSkillLevel(Skill.HITPOINTS) / 2);
            cells.add(new StatusCell("HP > 50%?", healthAbove50));
        }

        if (config.showFood()) {
            boolean hasFood = checkInventoryFor(config.foodNames());
            cells.add(new StatusCell("Has Food?", hasFood));
        }

        if (config.showPrayer()) {
            boolean prayerAbove50 = client.getBoostedSkillLevel(Skill.PRAYER) > (client.getRealSkillLevel(Skill.PRAYER) / 2);
            cells.add(new StatusCell("Prayer > 50%?", prayerAbove50));
        }

        if (config.showOutOfPrayer()) {
            boolean outOfPrayer = client.getBoostedSkillLevel(Skill.PRAYER) == 0;
            cells.add(new StatusCell("Out of Prayer?", outOfPrayer));
        }

        if (config.showIdle()) {
            boolean isIdle = localPlayer.getAnimation() == -1 
                && localPlayer.getPoseAnimation() == localPlayer.getIdlePoseAnimation()
                && localPlayer.getInteracting() == null;
            cells.add(new StatusCell("Idle?", isIdle));
        }

        if (config.showInvFull()) {
            boolean invFull = isInventoryFull();
            cells.add(new StatusCell("Inv Full?", invFull));
        }

        if (cells.isEmpty()) return null;

        int totalWidth = cells.size() * cellWidth;
        
        graphics.setColor(BG_COLOR);
        graphics.fillRect(0, 0, totalWidth, cellHeight);
        graphics.setColor(Color.GRAY);
        graphics.drawRect(0, 0, totalWidth, cellHeight);

        for (int i = 0; i < cells.size(); i++)
        {
            StatusCell cell = cells.get(i);
            int xPos = i * cellWidth;

            int boxX = xPos + (cellWidth / 2) - (boxSize / 2);
            int boxY = (int) (5 * scale);

            graphics.setColor(cell.isActive ? COLOR_TRUE : COLOR_FALSE);
            graphics.fillRect(boxX, boxY, boxSize, boxSize);
            graphics.setColor(Color.BLACK);
            graphics.drawRect(boxX, boxY, boxSize, boxSize);

            graphics.setColor(Color.WHITE);
            int textY = (int) (35 * scale);
            drawCenteredString(graphics, cell.label, xPos, textY, cellWidth);

            graphics.setColor(Color.GRAY);
            graphics.drawLine(xPos + cellWidth, 0, xPos + cellWidth, cellHeight);
        }

        return new Dimension(totalWidth, cellHeight);
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
        for (Item item : container.getItems()) {
            if (item == null || item.getId() <= 0) continue;
            ItemComposition def = client.getItemDefinition(item.getId());
            if (def != null && namesToCheck.stream().anyMatch(n -> WildcardMatcher.matches(n, def.getName().toUpperCase()))) return true;
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
        int range = 15;
        int plane = client.getPlane();
        Tile[][][] tiles = client.getScene().getTiles();
        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                int x = sceneX + dx; int y = sceneY + dy;
                if (x < 0 || x >= 104 || y < 0 || y >= 104) continue;
                Tile tile = tiles[plane][x][y];
                if (tile == null || tile.getGroundItems() == null) continue;
                for (TileItem ti : tile.getGroundItems()) {
                    ItemComposition def = client.getItemDefinition(ti.getId());
                    if (def != null && namesToCheck.stream().anyMatch(n -> WildcardMatcher.matches(n, def.getName().toUpperCase()))) return true;
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

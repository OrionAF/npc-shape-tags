package com.example;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;
import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.List;

public class ShapeOverlay extends Overlay
{
    private final Client client;
    private final CombatStateConfig config;

    @Inject
    private ShapeOverlay(Client client, CombatStateConfig config)
    {
        this.client = client;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        // 1. Render NPCs
        renderNpcs(graphics);

        // 2. Render Ground Items and Objects (Scan nearby tiles)
        renderTiles(graphics);

        return null;
    }

    private void renderNpcs(Graphics2D graphics)
    {
        String configNames = config.npcNames();
        if (configNames == null || configNames.isEmpty()) return;

        List<String> targetNames = Text.fromCSV(configNames);
        for (NPC npc : client.getNpcs())
        {
            if (config.hideInCombat())
            {
                if (npc.getInteracting() != null || npc.getHealthRatio() != -1) continue;
            }

            NPCComposition composition = npc.getTransformedComposition();
            if (composition == null) continue;

            String npcName = Text.removeTags(composition.getName()).toUpperCase();
            if (targetNames.stream().anyMatch(target -> WildcardMatcher.matches(target.toUpperCase(), npcName)))
            {
                // Calculate NPC Center
                LocalPoint lp = npc.getLocalLocation();
                if (lp != null)
                {
                    Point p = Perspective.localToCanvas(client, lp, client.getPlane(), npc.getLogicalHeight() / 2);
                    drawShapeAt(graphics, p);
                }
            }
        }
    }

    private void renderTiles(Graphics2D graphics)
    {
        String itemConfig = config.groundItemTags();
        String objectConfig = config.objectTags();
        boolean checkItems = itemConfig != null && !itemConfig.isEmpty();
        boolean checkObjects = objectConfig != null && !objectConfig.isEmpty();

        if (!checkItems && !checkObjects) return;

        List<String> itemNames = checkItems ? Text.fromCSV(itemConfig) : null;
        List<String> objectNames = checkObjects ? Text.fromCSV(objectConfig) : null;

        LocalPoint lp = client.getLocalPlayer().getLocalLocation();
        if (lp == null) return;

        int sceneX = lp.getSceneX();
        int sceneY = lp.getSceneY();
        int radius = 15; // Scan radius (save performance)
        Tile[][][] tiles = client.getScene().getTiles();
        int plane = client.getPlane();

        for (int x = -radius; x <= radius; x++)
        {
            for (int y = -radius; y <= radius; y++)
            {
                int tileX = sceneX + x;
                int tileY = sceneY + y;

                if (tileX < 0 || tileX >= 104 || tileY < 0 || tileY >= 104) continue;

                Tile tile = tiles[plane][tileX][tileY];
                if (tile == null) continue;

                // --- Check Ground Items ---
                if (checkItems && tile.getGroundItems() != null)
                {
                    for (TileItem item : tile.getGroundItems())
                    {
                        ItemComposition def = client.getItemDefinition(item.getId());
                        if (def != null && def.getName() != null)
                        {
                            if (itemNames.stream().anyMatch(n -> WildcardMatcher.matches(n.toUpperCase(), def.getName().toUpperCase())))
                            {
                                Point p = Perspective.localToCanvas(client, tile.getLocalLocation(), plane, 0);
                                drawShapeAt(graphics, p);
                                break; // Only tag once per tile if multiple items match
                            }
                        }
                    }
                }

                // --- Check Game Objects ---
                if (checkObjects)
                {
                    renderGameObject(graphics, tile.getGameObjects(), objectNames);
                    renderGameObject(graphics, tile.getWallObject(), objectNames);
                    renderGameObject(graphics, tile.getDecorativeObject(), objectNames);
                    renderGameObject(graphics, tile.getGroundObject(), objectNames);
                }
            }
        }
    }

    // Helper to handle single object or arrays of objects
    private void renderGameObject(Graphics2D graphics, Object objectOrArray, List<String> targets)
    {
        if (objectOrArray == null) return;

        // Handle Array of GameObjects
        if (objectOrArray instanceof GameObject[])
        {
            for (GameObject obj : (GameObject[]) objectOrArray)
            {
                if (obj != null) checkAndDrawObject(graphics, obj, targets);
            }
        }
        // Handle Single Object (Wall, Ground, Dec)
        else if (objectOrArray instanceof TileObject)
        {
            checkAndDrawObject(graphics, (TileObject) objectOrArray, targets);
        }
    }

    private void checkAndDrawObject(Graphics2D graphics, TileObject obj, List<String> targets)
    {
        ObjectComposition def = client.getObjectDefinition(obj.getId());
        if (def == null) return;
        
        // Handle impostors (changing objects like farming patches/doors)
        if (def.getImpostorIds() != null)
        {
            def = def.getImpostor();
            if (def == null) return;
        }

        String name = Text.removeTags(def.getName()).toUpperCase();
        if (targets.stream().anyMatch(t -> WildcardMatcher.matches(t.toUpperCase(), name)))
        {
            // Objects usually look better if we lift the shape up slightly (e.g. 50 units)
            Point p = Perspective.localToCanvas(client, obj.getLocalLocation(), client.getPlane(), 50);
            drawShapeAt(graphics, p);
        }
    }

    // --- Core Drawing Logic (Shared by NPCs, Items, and Objects) ---
    private void drawShapeAt(Graphics2D graphics, Point point)
    {
        if (point == null) return;

        graphics.setColor(config.color());
        int size = config.size();
        Shape shapeToDraw = null;

        switch (config.shape())
        {
            case SQUARE:
                shapeToDraw = new java.awt.geom.Rectangle2D.Float(point.getX() - size/2f, point.getY() - size/2f, size, size);
                break;
            case CIRCLE:
                shapeToDraw = new java.awt.geom.Ellipse2D.Float(point.getX() - size/2f, point.getY() - size/2f, size, size);
                break;
            case STAR:
                shapeToDraw = createStar(point.getX(), point.getY(), size, size / 2.5);
                break;
        }

        if (shapeToDraw != null)
        {
            if (config.filled()) graphics.fill(shapeToDraw);
            else graphics.draw(shapeToDraw);
        }
    }

    private Polygon createStar(double centerX, double centerY, double outerRadius, double innerRadius)
    {
        int points = 5;
        int[] xCoordinates = new int[points * 2];
        int[] yCoordinates = new int[points * 2];
        double angleStep = Math.PI / points;
        double rotation = -Math.PI / 2;
        for (int i = 0; i < points * 2; i++) {
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            xCoordinates[i] = (int) (centerX + Math.cos(rotation) * radius);
            yCoordinates[i] = (int) (centerY + Math.sin(rotation) * radius);
            rotation += angleStep;
        }
        return new Polygon(xCoordinates, yCoordinates, points * 2);
    }
}

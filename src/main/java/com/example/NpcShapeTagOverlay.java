package com.example;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
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

public class NpcShapeTagOverlay extends Overlay
{
    private final Client client;
    private final NpcShapeTagConfig config;

    @Inject
        private NpcShapeTagOverlay(Client client, NpcShapeTagConfig config)
        {
            this.client = client;
            this.config = config;
            setPosition(OverlayPosition.DYNAMIC);
            // "ABOVE_WIDGETS" forces it to draw over the game UI and Sprites
            setLayer(OverlayLayer.UNDER_WIDGETS); 
            // "HIGH" priority ensures it draws after other plugins
            setPriority(OverlayPriority.HIGH);
        }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        String configNames = config.npcNames();
        if (configNames == null || configNames.isEmpty())
        {
            return null;
        }

        // Split config names by comma
        List<String> targetNames = Text.fromCSV(configNames);

        for (NPC npc : client.getNpcs())
        {
            NPCComposition composition = npc.getTransformedComposition();
            if (composition == null)
            {
                continue;
            }

            String npcName = Text.removeTags(composition.getName()).toUpperCase();
            
            // Check if NPC matches any name in the config (supports wildcards)
            boolean match = targetNames.stream()
                .anyMatch(target -> WildcardMatcher.matches(target.toUpperCase(), npcName));

            if (match)
            {
                renderNpcShape(graphics, npc);
            }
        }

        return null;
    }

    private void renderNpcShape(Graphics2D graphics, NPC npc)
    {
        // Get the center of the NPC logic height
        LocalPoint localLocation = npc.getLocalLocation();
        if (localLocation == null)
        {
            return;
        }

        // Calculate the center point of the NPC model on the canvas
        // We use getLogicalHeight / 2 to try and center it vertically on the model
        Point point = Perspective.localToCanvas(client, localLocation, client.getPlane(), npc.getLogicalHeight() / 2);

        if (point == null)
        {
            return;
        }

        graphics.setColor(config.color());
        int size = config.size();

        Shape shapeToDraw = null;

        switch (config.shape())
        {
            case SQUARE:
                shapeToDraw = new java.awt.geom.Rectangle2D.Float(
                    point.getX() - size / 2.0f, 
                    point.getY() - size / 2.0f, 
                    size, 
                    size
                );
                break;
            case CIRCLE:
                shapeToDraw = new java.awt.geom.Ellipse2D.Float(
                    point.getX() - size / 2.0f, 
                    point.getY() - size / 2.0f, 
                    size, 
                    size
                );
                break;
            case STAR:
                shapeToDraw = createStar(point.getX(), point.getY(), size, size / 2.5);
                break;
        }

        if (shapeToDraw != null)
        {
            if (config.filled())
            {
                graphics.fill(shapeToDraw);
            }
            else
            {
                // Optional: set stroke width if needed, e.g., graphics.setStroke(new BasicStroke(2));
                graphics.draw(shapeToDraw);
            }
        }
    }

    /**
     * Helper to generate a Star Polygon
     * @param centerX Center X coordinate
     * @param centerY Center Y coordinate
     * @param outerRadius Length of outer points
     * @param innerRadius Length of inner points
     * @return A Polygon object representing a star
     */
    private Polygon createStar(double centerX, double centerY, double outerRadius, double innerRadius)
    {
        int points = 5;
        int[] xCoordinates = new int[points * 2];
        int[] yCoordinates = new int[points * 2];
        double angleStep = Math.PI / points;
        double rotation = -Math.PI / 2; // Start pointing up

        for (int i = 0; i < points * 2; i++)
        {
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            double x = centerX + Math.cos(rotation) * radius;
            double y = centerY + Math.sin(rotation) * radius;
            
            xCoordinates[i] = (int) x;
            yCoordinates[i] = (int) y;
            
            rotation += angleStep;
        }

        return new Polygon(xCoordinates, yCoordinates, points * 2);
    }
}

package org.anarchadia.Fractals;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

/**
 * A viewer for displaying a Koch Snowflake fractal with zooming and panning capabilities.
 */
public class KochSnowflakeViewer extends JFrame {
    private double zoom = 1.0;
    private double targetZoom = 1.0;
    private double xOffset = 0;
    private double targetXOffset = 0;
    private double yOffset = 0;
    private double targetYOffset = 0;
    private Point lastPoint;
    private BufferedImage fractalImage;
    private final KochSnowflakePanel panel;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private final Timer animationTimer;

    /**
     * Constructs the KochSnowflakeViewer and initializes its components.
     */
    public KochSnowflakeViewer() {
        setTitle("Koch Snowflake Viewer");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        panel = new KochSnowflakePanel();
        add(panel);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                lastPoint = null;
                if (SwingUtilities.isLeftMouseButton(e)) {
                    targetXOffset += (e.getX() - (double) getWidth() / 2) / zoom;
                    targetYOffset += (e.getY() - (double) getHeight() / 2) / zoom;
                    targetZoom *= 1.5;
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    targetZoom /= 1.5;
                    targetXOffset -= (e.getX() - (double) getWidth() / 2) / (zoom * 1.5 - zoom);
                    targetYOffset -= (e.getY() - (double) getHeight() / 2) / (zoom * 1.5 - zoom);
                }
                startAnimation();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    int dx = e.getX() - lastPoint.x;
                    int dy = e.getY() - lastPoint.y;
                    targetXOffset += dx / zoom;
                    targetYOffset += dy / zoom;
                    lastPoint = e.getPoint();
                    startAnimation();
                }
            }
        });

        animationTimer = new Timer(10, e -> animate());
    }

    /**
     * Starts the animation if it is not already running.
     */
    private void startAnimation() {
        if (!animationTimer.isRunning()) {
            animationTimer.start();
        }
    }

    /**
     * Animates the transition for zooming and panning by interpolating the current values towards the target values.
     */
    private void animate() {
        boolean zoomChanged = false;
        boolean offsetChanged = false;

        if (Math.abs(zoom - targetZoom) > 0.01) {
            zoom += (targetZoom - zoom) * 0.1;
            zoomChanged = true;
        }

        if (Math.abs(xOffset - targetXOffset) > 0.01) {
            xOffset += (targetXOffset - xOffset) * 0.1;
            offsetChanged = true;
        }

        if (Math.abs(yOffset - targetYOffset) > 0.01) {
            yOffset += (targetYOffset - yOffset) * 0.1;
            offsetChanged = true;
        }

        if (zoomChanged || offsetChanged) {
            panel.renderFractal();
        } else {
            animationTimer.stop();
        }
    }

    private class KochSnowflakePanel extends JPanel {
        private boolean renderingInProgress = false;

        @Override
        public void addNotify() {
            super.addNotify();
            renderFractal();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (fractalImage != null) {
                g.drawImage(fractalImage, 0, 0, this);
            }
        }

        /**
         * Renders the Koch Snowflake fractal onto the panel.
         */
        public void renderFractal() {
            if (getWidth() <= 0 || getHeight() <= 0 || renderingInProgress) {
                return;
            }

            renderingInProgress = true;

            if (fractalImage == null || fractalImage.getWidth() != getWidth() || fractalImage.getHeight() != getHeight()) {
                fractalImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            }

            int width = getWidth();
            int height = getHeight();
            Graphics2D g2d = fractalImage.createGraphics();
            g2d.setPaint(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            g2d.setPaint(Color.BLACK);

            double size = Math.min(width, height) * zoom / 1.5;
            double x1 = (width / 2.0) + xOffset * zoom - size / 2;
            double y1 = (height / 2.0) + yOffset * zoom + size * Math.sqrt(3) / 6;
            double x2 = x1 + size;
            double x3 = x1 + size / 2;
            double y3 = y1 - size * Math.sqrt(3) / 2;

            drawKochCurve(g2d, x1, y1, x2, y1, 5);
            drawKochCurve(g2d, x2, y1, x3, y3, 5);
            drawKochCurve(g2d, x3, y3, x1, y1, 5);

            g2d.dispose();
            renderingInProgress = false;
            repaint();
        }

        /**
         * Draws a segment of the Koch curve.
         *
         * @param g2d   The Graphics2D context to draw with.
         * @param x1    The starting x-coordinate.
         * @param y1    The starting y-coordinate.
         * @param x2    The ending x-coordinate.
         * @param y2    The ending y-coordinate.
         * @param level The recursion level.
         */
        private void drawKochCurve(Graphics2D g2d, double x1, double y1, double x2, double y2, int level) {
            if (level == 0) {
                g2d.draw(new Line2D.Double(x1, y1, x2, y2));
                return;
            }

            double deltaX = x2 - x1;
            double deltaY = y2 - y1;
            double xA = x1 + deltaX / 3;
            double yA = y1 + deltaY / 3;
            double xB = x1 + 2 * deltaX / 3;
            double yB = y1 + 2 * deltaY / 3;

            double angle = Math.PI / 3;
            double sin60 = Math.sin(angle);
            double cos60 = Math.cos(angle);

            double xC = xA + (deltaX / 3) * cos60 + (deltaY / 3) * sin60;
            double yC = yA - (deltaX / 3) * sin60 + (deltaY / 3) * cos60;

            drawKochCurve(g2d, x1, y1, xA, yA, level - 1);
            drawKochCurve(g2d, xA, yA, xC, yC, level - 1);
            drawKochCurve(g2d, xC, yC, xB, yB, level - 1);
            drawKochCurve(g2d, xB, yB, x2, y2, level - 1);
        }
    }
}
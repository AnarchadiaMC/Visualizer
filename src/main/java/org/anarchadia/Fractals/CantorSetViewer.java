package org.anarchadia.Fractals;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * CantorSetViewer is a graphical application that visualizes the Cantor set.
 * It allows for zooming and panning of the fractal through mouse interactions.
 */
public class CantorSetViewer extends JFrame {
    // Zoom and offset parameters
    private double zoom = 1.0;
    private double targetZoom = 1.0;
    private double xOffset = 0;
    private double targetXOffset = 0;
    private double yOffset = 0;
    private double targetYOffset = 0;
    private Point lastPoint;

    private BufferedImage fractalImage;
    private CantorSetPanel panel;

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private Timer animationTimer;

    /**
     * Creates a new CantorSetViewer instance with predefined settings
     * and initializes the UI components and event listeners.
     */
    public CantorSetViewer() {
        setTitle("Cantor Set Viewer");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        panel = new CantorSetPanel();
        add(panel);

        // Mouse listener for zooming in and out
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                lastPoint = null; // Reset lastPoint on click
                if (SwingUtilities.isLeftMouseButton(e)) {
                    targetXOffset += (e.getX() - getWidth() / 2) / zoom;
                    targetYOffset += (e.getY() - getHeight() / 2) / zoom;
                    targetZoom *= 1.5;
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    targetZoom /= 1.5;
                    targetXOffset -= (e.getX() - getWidth() / 2) / zoom;
                    targetYOffset -= (e.getY() - getHeight() / 2) / zoom;
                }
                startAnimation();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint(); // Initialize lastPoint on press
            }
        });

        // Mouse motion listener for panning
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

        // Setup the animation timer
        animationTimer = new Timer(10, e -> animate());
    }

    /**
     * Starts the animation timer if it is not already running.
     */
    private void startAnimation() {
        if (!animationTimer.isRunning()) {
            animationTimer.start();
        }
    }

    /**
     * Animates the transition for zoom and pan operations, ensuring a smooth user experience.
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

    /**
     * CantorSetPanel is a custom JPanel responsible for rendering the Cantor set fractal image.
     */
    private class CantorSetPanel extends JPanel {
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
         * Renders the Cantor set fractal image.
         * Ensures no overlapping renders and creates a new image if the panel size changes.
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
            drawCantorSet(g2d, (int)((xOffset * zoom) + width / 3), (int)(50 + yOffset * zoom), (int)(width * zoom / 3), 10, 0);

            g2d.dispose();

            renderingInProgress = false;
            repaint();
        }

        /**
         * Draws the Cantor set recursively.
         *
         * @param g2d        Graphics2D object for drawing.
         * @param x          X-coordinate for the current segment.
         * @param y          Y-coordinate for the current segment.
         * @param lineWidth  Width of the current segment.
         * @param lineHeight Height of the line segment.
         * @param depth      Current recursion depth.
         */
        private void drawCantorSet(Graphics2D g2d, int x, int y, int lineWidth, int lineHeight, int depth) {
            if (lineWidth < 1) {
                return;
            }

            g2d.fillRect(x, y, lineWidth, lineHeight);

            if (lineWidth > 1) {
                int newY = y + 2 * lineHeight;
                int newLineWidth = lineWidth / 3;
                drawCantorSet(g2d, x, newY, newLineWidth, lineHeight, depth + 1);
                drawCantorSet(g2d, x + 2 * newLineWidth, newY, newLineWidth, lineHeight, depth + 1);
            }
        }
    }
}
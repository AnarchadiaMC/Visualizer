package org.anarchadia.Fractals;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * The {@code FractalTreeViewer} class creates a JFrame window that displays a fractal tree.
 * It provides capabilities to zoom in/out and pan the view using mouse actions.
 */
public class FractalTreeViewer extends JFrame {
    private double zoom = 1.0;
    private double targetZoom = 1.0;
    private double xOffset = 0;
    private double targetXOffset = 0;
    private double yOffset = 0;
    private double targetYOffset = 0;
    private Point lastPoint;

    private BufferedImage fractalImage;
    private final FractalTreePanel panel;

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private final Timer animationTimer;

    /**
     * Constructs a new {@code FractalTreeViewer}.
     */
    public FractalTreeViewer() {
        setTitle("Fractal Tree Viewer");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        panel = new FractalTreePanel();
        add(panel);

        addMouseListener(new MouseAdapter() {
            /**
             * Handles mouse clicks for zooming in and out.
             * @param e the mouse event
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                lastPoint = null; // Reset lastPoint on click
                final int centerX = getWidth() / 2;
                final int centerY = getHeight() / 2;
                if (SwingUtilities.isLeftMouseButton(e)) {
                    targetZoom *= 1.5;
                    targetXOffset = targetXOffset - (e.getX() - centerX) / zoom + (e.getX() - centerX) / targetZoom;
                    targetYOffset = targetYOffset - (e.getY() - centerY) / zoom + (e.getY() - centerY) / targetZoom;
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    targetZoom /= 1.5;
                    targetXOffset = targetXOffset - (e.getX() - centerX) / zoom + (e.getX() - centerX) / targetZoom;
                    targetYOffset = targetYOffset - (e.getY() - centerY) / zoom + (e.getY() - centerY) / targetZoom;
                }
                startAnimation();
            }

            /**
             * Initializes lastPoint on mouse press.
             * @param e the mouse event
             */
            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            /**
             * Handles mouse dragging for panning.
             * @param e the mouse event
             */
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    int dx = e.getX() - lastPoint.x;
                    int dy = e.getY() - lastPoint.y;
                    targetXOffset += dx / zoom; // Correct X-axis dragging
                    targetYOffset += dy / zoom; // Correct Y-axis dragging
                    lastPoint = e.getPoint();
                    startAnimation();
                }
            }
        });

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
     * Animates the transition of zoom and panning by interpolating the current
     * and target values.
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
     * The {@code FractalTreePanel} class is a JPanel that renders the fractal tree.
     */
    private class FractalTreePanel extends JPanel {
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
         * Renders the fractal tree onto a BufferedImage.
         */
        public void renderFractal() {
            if (getWidth() <= 0 || getHeight() <= 0 || renderingInProgress) {
                return; // Ensure valid dimensions and no overlapping renders
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
            g2d.translate(width / 2, height);
            g2d.scale(zoom, zoom);
            g2d.translate(xOffset, yOffset);

            drawFractalTree(g2d, 0, 0, -90, 100);

            g2d.dispose();
            renderingInProgress = false;
            repaint();
        }

        /**
         * Draws the fractal tree.
         * @param g2d the Graphics2D context to draw on
         * @param x1 the starting x-coordinate
         * @param y1 the starting y-coordinate
         * @param angle the angle of the branch
         * @param length the length of the branch
         */
        private void drawFractalTree(Graphics2D g2d, int x1, int y1, double angle, double length) {
            if (length < 5) {
                return; // Base case: stop when the branch length is too small
            }

            int x2 = x1 + (int) (Math.cos(Math.toRadians(angle)) * length);
            int y2 = y1 + (int) (Math.sin(Math.toRadians(angle)) * length);

            g2d.drawLine(x1, y1, x2, y2);

            // Branching with different angles and length reduction factors
            drawFractalTree(g2d, x2, y2, angle - 20, length * 0.75); // Left branch
            drawFractalTree(g2d, x2, y2, angle + 20, length * 0.75); // Right branch
            drawFractalTree(g2d, x2, y2, angle - 45, length * 0.5);  // Extra left branch
            drawFractalTree(g2d, x2, y2, angle + 45, length * 0.5);  // Extra right branch
        }
    }
}
package org.anarchadia.Fractals;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * A viewer class for displaying the Sierpinski Carpet fractal pattern.
 * This class sets up a JFrame that displays the fractal and allows for
 * zooming and panning using mouse interactions.
 */
public class SierpinskiCarpetViewer extends JFrame {
    private double zoom = 1.0;
    private double targetZoom = 1.0;
    private double xOffset = 0;
    private double targetXOffset = 0;
    private double yOffset = 0;
    private double targetYOffset = 0;
    private Point lastPoint;

    private BufferedImage fractalImage;
    private final SierpinskiCarpetPanel panel;

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private final Timer animationTimer;

    /**
     * Constructor to set up the Sierpinski Carpet Viewer.
     */
    public SierpinskiCarpetViewer() {
        setTitle("Sierpinski Carpet Viewer");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        panel = new SierpinskiCarpetPanel();
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
     * Starts the animation timer if it is not already running.
     */
    private void startAnimation() {
        if (!animationTimer.isRunning()) {
            animationTimer.start();
        }
    }

    /**
     * Animates the zoom and pan transitions smoothly.
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
     * Inner class for the panel displaying the Sierpinski Carpet fractal.
     */
    private class SierpinskiCarpetPanel extends JPanel {
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
         * Renders the Sierpinski Carpet fractal and updates the image.
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

            int size = (int) (Math.min(width, height) * zoom);
            int x = (int) ((width / 2) + xOffset * zoom - size / 2);
            int y = (int) ((height / 2) + yOffset * zoom - size / 2);

            drawCarpet(g2d, x, y, size);
            g2d.dispose();

            renderingInProgress = false;

            repaint();
        }

        /**
         * Recursively draws the Sierpinski Carpet fractal.
         *
         * @param g2d The Graphics2D object used for drawing.
         * @param x The x-coordinate of the top-left corner of the current square.
         * @param y The y-coordinate of the top-left corner of the current square.
         * @param size The size of the current square.
         */
        private void drawCarpet(Graphics2D g2d, int x, int y, int size) {
            if (size < 3) {
                if (isInView(x, y)) {
                    g2d.fillRect(x, y, size, size);
                }
                return;
            }

            int newSize = size / 3;

            for (int dx = 0; dx < 3; dx++) {
                for (int dy = 0; dy < 3; dy++) {
                    if (dx == 1 && dy == 1) {
                        continue;
                    }
                    drawCarpet(g2d, x + dx * newSize, y + dy * newSize, newSize);
                }
            }
        }

        /**
         * Checks if the given coordinates are within the visible view.
         *
         * @param x The x-coordinate to check.
         * @param y The y-coordinate to check.
         * @return True if the coordinates are within the visible view, false otherwise.
         */
        private boolean isInView(int x, int y) {
            return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
        }
    }
}
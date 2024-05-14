package org.anarchadia.Fractals;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * A graphical viewer for displaying the Dragon Curve fractal.
 */
public class DragonCurveViewer extends JFrame {
    private double zoom = 1.0;
    private double targetZoom = 1.0;
    private double xOffset = 0;
    private double targetXOffset = 0;
    private double yOffset = 0;
    private double targetYOffset = 0;
    private Point lastPoint;

    private BufferedImage fractalImage;
    private final DragonCurvePanel panel;

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private final Timer animationTimer;

    /**
     * Constructs the DragonCurveViewer frame and initializes components.
     */
    public DragonCurveViewer() {
        setTitle("Dragon Curve Viewer");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Use DISPOSE_ON_CLOSE instead of EXIT_ON_CLOSE
        setLocationRelativeTo(null);

        panel = new DragonCurvePanel();
        add(panel);

        // Mouse listener for zooming in and out
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                lastPoint = null; // Reset lastPoint on click
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
     * Starts the animation loop for smooth transformations.
     */
    private void startAnimation() {
        if (!animationTimer.isRunning()) {
            animationTimer.start();
        }
    }

    /**
     * Animates the smooth transformation of zoom and pan.
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
     * A JPanel that handles the rendering of the Dragon Curve fractal.
     */
    private class DragonCurvePanel extends JPanel {
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
         * Renders the Dragon Curve fractal onto the panel.
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

            int iterations = 16; // Number of iterations for the Dragon Curve
            double size = Math.min(width, height) * 0.8 * zoom;
            double x1 = width / 2.0 + xOffset * zoom - size / 2;
            double y1 = height / 2.0 + yOffset * zoom + size / 3;
            double x2 = x1 + size;

            drawDragonCurve(g2d, iterations, x1, y1, x2, y1);
            g2d.dispose();

            renderingInProgress = false;
            repaint();
        }

        /**
         * Draws the Dragon Curve using recursive calls.
         *
         * @param g2d        The graphics context to draw with.
         * @param iterations The number of iterations to draw.
         * @param x1         Starting x coordinate.
         * @param y1         Starting y coordinate.
         * @param x2         Ending x coordinate.
         * @param y2         Ending y coordinate.
         */
        private void drawDragonCurve(Graphics2D g2d, int iterations, double x1, double y1, double x2, double y2) {
            AffineTransform orig = g2d.getTransform();

            if (iterations == 0) {
                g2d.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
            } else {
                double midx = (x1 + x2) / 2 + (y2 - y1) / 2;
                double midy = (y1 + y2) / 2 - (x2 - x1) / 2;

                drawDragonCurve(g2d, iterations - 1, x1, y1, midx, midy);
                drawDragonCurve(g2d, iterations - 1, x2, y2, midx, midy);
            }

            g2d.setTransform(orig);
        }
    }
}
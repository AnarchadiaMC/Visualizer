package org.anarchadia.Fractals;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * A viewer for displaying the Barnsley Fern fractal using Java Swing.
 * This class handles user interactions for zooming and panning the fractal.
 */
public class BarnsleyFernViewer extends JFrame {
    private double zoom = 1.0;
    private double targetZoom = 1.0;
    private double xOffset = 0;
    private double targetXOffset = 0;
    private double yOffset = 0;
    private double targetYOffset = 0;
    private Point lastPoint;

    private BufferedImage fractalImage;
    private final BarnsleyFernPanel panel;

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private final Timer animationTimer;

    /**
     * Constructs a new Barnsley Fern viewer with default settings.
     */
    public BarnsleyFernViewer() {
        setTitle("Barnsley Fern Viewer");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        panel = new BarnsleyFernPanel();
        add(panel);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                lastPoint = null;
                if (SwingUtilities.isLeftMouseButton(e)) {
                    targetXOffset += (e.getX() - (double) getWidth() / 2) / zoom;
                    targetYOffset -= (e.getY() - (double) getHeight() / 2) / zoom;
                    targetZoom *= 1.5;
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    targetZoom /= 1.5;
                    targetXOffset -= (e.getX() - (double) getWidth() / 2) / (zoom * 1.5 - zoom);
                    targetYOffset += (e.getY() - (double) getHeight() / 2) / (zoom * 1.5 - zoom);
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
                    targetYOffset -= dy / zoom;
                    lastPoint = e.getPoint();
                    startAnimation();
                }
            }
        });

        animationTimer = new Timer(10, e -> animate());
    }

    /**
     * Starts the animation timer for smooth zoom and pan transitions.
     */
    private void startAnimation() {
        if (!animationTimer.isRunning()) {
            animationTimer.start();
            System.out.println("Started animation timer");
        }
    }

    /**
     * Animates the fractal view by interpolating between current and target values.
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
            System.out.println("Stopped animation timer");
        }
    }

    /**
     * Inner class representing the panel for rendering the Barnsley Fern fractal.
     */
    private class BarnsleyFernPanel extends JPanel {
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
         * Renders the Barnsley Fern fractal onto an offscreen image.
         */
        public void renderFractal() {
            if (getWidth() <= 0 || getHeight() <= 0 || renderingInProgress) {
                return;
            }

            renderingInProgress = true;

            fractalImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

            int width = getWidth();
            int height = getHeight();
            Graphics2D g2d = fractalImage.createGraphics();
            g2d.setPaint(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            g2d.setPaint(Color.BLACK);

            Random random = new Random();
            double x = 0, y = 0;

            for (int i = 0; i < 100000; i++) {
                double nextX, nextY;
                double rand = random.nextDouble();

                if (rand < 0.01) {
                    nextX = 0;
                    nextY = 0.16 * y;
                } else if (rand < 0.86) {
                    nextX = 0.85 * x + 0.04 * y;
                    nextY = -0.04 * x + 0.85 * y + 1.6;
                } else if (rand < 0.93) {
                    nextX = 0.2 * x - 0.26 * y;
                    nextY = 0.23 * x + 0.22 * y + 1.6;
                } else {
                    nextX = -0.15 * x + 0.28 * y;
                    nextY = 0.26 * x + 0.24 * y + 0.44;
                }

                x = nextX;
                y = nextY;

                int plotX = (int) ((width / 2) + xOffset * zoom + x * 40 * zoom);
                int plotY = (int) ((height / 2) - yOffset * zoom - y * 40 * zoom);

                if (isInView(width, height, plotX, plotY)) {
                    fractalImage.setRGB(plotX, plotY, Color.BLACK.getRGB());
                }
            }

            g2d.dispose();
            renderingInProgress = false;
            repaint();
        }

        /**
         * Checks if a point is within the view bounds of the panel.
         *
         * @param width  the width of the panel
         * @param height the height of the panel
         * @param x      the x-coordinate to check
         * @param y      the y-coordinate to check
         * @return true if the point is within bounds, false otherwise
         */
        private boolean isInView(int width, int height, int x, int y) {
            return x >= 0 && x < width && y >= 0 && y < height;
        }
    }
}
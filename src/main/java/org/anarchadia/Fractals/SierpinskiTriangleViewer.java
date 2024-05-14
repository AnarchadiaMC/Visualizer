package org.anarchadia.Fractals;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * SierpinskiTriangleViewer is a Swing-based application that displays
 * and allows interaction with a Sierpinski Triangle fractal. The viewer supports
 * zooming and panning via mouse interactions.
 */
public class SierpinskiTriangleViewer extends JFrame {
    private double zoom = 1.0;
    private double targetZoom = 1.0;
    private double xOffset = 0;
    private double targetXOffset = 0;
    private double yOffset = 0;
    private double targetYOffset = 0;
    private Point lastPoint;

    private BufferedImage fractalImage;
    private final SierpinskiTrianglePanel panel;

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private final Timer animationTimer;

    /**
     * Constructor for SierpinskiTriangleViewer.
     * Initializes the JFrame settings and sets up mouse listeners and animation timer.
     */
    public SierpinskiTriangleViewer() {
        setTitle("Sierpinski Triangle Viewer");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        panel = new SierpinskiTrianglePanel();
        add(panel);

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
     * Animates the transition for zooming and panning.
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
     * SierpinskiTrianglePanel is a JPanel subclass used to render the Sierpinski Triangle fractal.
     */
    private class SierpinskiTrianglePanel extends JPanel {
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
         * Initiates the rendering of the Sierpinski Triangle fractal.
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

            int size = (int) (Math.min(width, height) * zoom);
            int x1 = (int) ((width / 2) + xOffset * zoom - size / 2);
            int y1 = (int) ((height / 2) + yOffset * zoom + size / 2);
            int x2 = x1 + size;
            int x3 = x1 + size / 2;
            int y3 = y1 - size;

            drawSierpinski(g2d, x1, y1, x2, y1, x3, y3);
            g2d.dispose();

            renderingInProgress = false;

            repaint();
        }

        /**
         * Recursive method to draw the Sierpinski Triangle.
         *
         * @param g2d Graphics2D object used for drawing.
         * @param x1  X coordinate of the first vertex.
         * @param y1  Y coordinate of the first vertex.
         * @param x2  X coordinate of the second vertex.
         * @param y2  Y coordinate of the second vertex.
         * @param x3  X coordinate of the third vertex.
         * @param y3  Y coordinate of the third vertex.
         */
        private void drawSierpinski(Graphics2D g2d, int x1, int y1, int x2, int y2, int x3, int y3) {
            if (Math.abs(x2 - x1) < 2 && Math.abs(y1 - y3) < 2) {
                if (isInView(x1, y1) || isInView(x2, y2) || isInView(x3, y3)) {
                    g2d.drawLine(x1, y1, x2, y2);
                    g2d.drawLine(x2, y2, x3, y3);
                    g2d.drawLine(x3, y3, x1, y1);
                }
                return;
            }

            int midx12 = (x1 + x2) / 2;
            int midy12 = (y1 + y2) / 2;
            int midx23 = (x2 + x3) / 2;
            int midy23 = (y2 + y3) / 2;
            int midx31 = (x3 + x1) / 2;
            int midy31 = (y3 + y1) / 2;

            drawSierpinski(g2d, x1, y1, midx12, midy12, midx31, midy31);
            drawSierpinski(g2d, midx12, midy12, x2, y2, midx23, midy23);
            drawSierpinski(g2d, midx31, midy31, midx23, midy23, x3, y3);
        }

        /**
         * Checks whether a given point is within the visible area of the panel.
         *
         * @param x X coordinate of the point.
         * @param y Y coordinate of the point.
         * @return true if the point is within the visible area, false otherwise.
         */
        private boolean isInView(int x, int y) {
            return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
        }
    }
}
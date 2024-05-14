package org.anarchadia.Fractals;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * A viewer for rendering and interacting with the Mandelbrot set fractal.
 * This viewer allows zooming and panning using mouse events.
 */
public class MandelbrotViewer extends JFrame {
    private double zoom = 200;
    private double targetZoom = 200;
    private double xOffset = 0;
    private double targetXOffset = 0;
    private double yOffset = 0;
    private double targetYOffset = 0;
    private final int MAX_ITER = 1000;
    private Point lastPoint;

    private final ForkJoinPool pool = new ForkJoinPool();
    private BufferedImage offScreenImage;
    private BufferedImage onScreenImage;
    private final MandelbrotPanel panel;

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private final int[] colors = new int[MAX_ITER + 1];
    private final Timer animationTimer;

    /**
     * Constructs a new MandelbrotViewer.
     */
    public MandelbrotViewer() {
        setTitle("Mandelbrot Viewer");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        panel = new MandelbrotPanel();
        add(panel);

        createColorMap();

        // Configure mouse listeners for zooming and panning
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

        // Configure mouse motion listener for panning
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    int dx = e.getX() - lastPoint.x;
                    int dy = e.getY() - lastPoint.y;
                    targetXOffset -= dx / zoom;
                    targetYOffset -= dy / zoom;
                    lastPoint = e.getPoint();
                    startAnimation();
                }
            }
        });

        animationTimer = new Timer(10, e -> animate());
    }

    /**
     * Creates the color map for the Mandelbrot set based on iteration count.
     */
    private void createColorMap() {
        for (int i = 0; i < MAX_ITER; i++) {
            colors[i] = new Color(Color.HSBtoRGB(i / 256f, 1, i / (i + 8f))).getRGB();
        }
        colors[MAX_ITER] = Color.BLACK.getRGB();
    }

    /**
     * Starts the animation for smooth zooming and panning.
     */
    private void startAnimation() {
        if (!animationTimer.isRunning()) {
            animationTimer.start();
        }
    }

    /**
     * Animate the zooming and panning actions.
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
     * Inner panel class responsible for rendering the Mandelbrot set.
     */
    private class MandelbrotPanel extends JPanel {
        private boolean renderingInProgress = false;

        /**
         * Invoked when the panel is added to a container.
         */
        @Override
        public void addNotify() {
            super.addNotify();
            renderFractal();
        }

        /**
         * Invoked by Swing to paint the component.
         *
         * @param g the Graphics object to protect
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (onScreenImage != null) {
                g.drawImage(onScreenImage, 0, 0, this);
            }
        }

        /**
         * Renders the fractal by delegating to a ForkJoinPool.
         */
        public void renderFractal() {
            if (getWidth() <= 0 || getHeight() <= 0 || renderingInProgress) {
                return; // Ensure valid dimensions and no overlapping renders
            }

            renderingInProgress = true;

            if (offScreenImage == null || offScreenImage.getWidth() != getWidth() || offScreenImage.getHeight() != getHeight()) {
                offScreenImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            }

            pool.execute(new RenderTask(0, getWidth(), 0, getHeight(), () -> {
                synchronized (MandelbrotPanel.this) {
                    onScreenImage = offScreenImage;
                }
                repaint();
                renderingInProgress = false;
            }));
        }

        /**
         * RenderTask class performs the fractal calculation and coloring using ForkJoinPool.
         */
        private class RenderTask extends RecursiveAction {
            private static final int THRESHOLD = 8000; // Adjusted for experimentation
            private final int startX, endX, startY, endY;
            private final Runnable completionCallback;

            /**
             * Constructs a RenderTask instance.
             *
             * @param startX Starting X coordinate
             * @param endX Ending X coordinate
             * @param startY Starting Y coordinate
             * @param endY Ending Y coordinate
             * @param completionCallback Callback to execute upon completion
             */
            public RenderTask(int startX, int endX, int startY, int endY, Runnable completionCallback) {
                this.startX = startX;
                this.endX = endX;
                this.startY = startY;
                this.endY = endY;
                this.completionCallback = completionCallback;
            }

            /**
             * Computes the fractal rendering task either directly or by splitting it into subtasks.
             */
            @Override
            protected void compute() {
                if ((endX - startX) * (endY - startY) <= THRESHOLD) {
                    for (int x = startX; x < endX; x++) {
                        for (int y = startY; y < endY; y++) {
                            double zx = 0, zy = 0, cX, cY;
                            cX = (x - (double) getWidth() / 2) / zoom + xOffset;
                            cY = (y - (double) getHeight() / 2) / zoom + yOffset;
                            int iter = MAX_ITER;

                            while (zx * zx + zy * zy < 4 && iter > 0) {
                                double tmp = zx * zx - zy * zy + cX;
                                zy = 2.0 * zx * zy + cY;
                                zx = tmp;
                                iter--;
                            }

                            offScreenImage.setRGB(x, y, colors[iter]);
                        }
                    }
                    if (completionCallback != null) {
                        completionCallback.run();
                    }
                } else {
                    int midX = (startX + endX) / 2;
                    int midY = (startY + endY) / 2;

                    invokeAll(
                            new RenderTask(startX, midX, startY, midY, completionCallback),
                            new RenderTask(midX, endX, startY, midY, null),
                            new RenderTask(startX, midX, midY, endY, null),
                            new RenderTask(midX, endX, midY, endY, null)
                    );
                }
            }
        }
    }
}
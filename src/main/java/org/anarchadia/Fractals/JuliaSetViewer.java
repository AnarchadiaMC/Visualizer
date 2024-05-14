package org.anarchadia.Fractals;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * A JFrame-based viewer for rendering and interacting with Julia Set fractals.
 */
public class JuliaSetViewer extends JFrame {
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
    private final JuliaSetPanel panel;

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private final int[] colors = new int[MAX_ITER + 1];
    private final Timer animationTimer;

    /**
     * Initializes the Julia set viewer.
     */
    public JuliaSetViewer() {
        setTitle("Julia Set Viewer");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        panel = new JuliaSetPanel();
        add(panel);

        createColorMap();

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
     * Creates a color map for the Julia set.
     */
    private void createColorMap() {
        for (int i = 0; i < MAX_ITER; i++) {
            colors[i] = new Color(Color.HSBtoRGB(i / 256f, 1, i / (i + 8f))).getRGB();
        }
        colors[MAX_ITER] = Color.BLACK.getRGB();
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
     * Animates the transitions for zoom and offset.
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
     * Inner class representing the panel where the Julia set is rendered.
     */
    private class JuliaSetPanel extends JPanel {
        private boolean renderingInProgress = false;

        @Override
        public void addNotify() {
            super.addNotify();
            renderFractal();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (onScreenImage != null) {
                g.drawImage(onScreenImage, 0, 0, this);
            }
        }

        /**
         * Triggers the rendering of the Julia set fractal.
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
                synchronized (JuliaSetPanel.this) {
                    onScreenImage = offScreenImage;
                }
                repaint();
                renderingInProgress = false;
            }));
        }

        /**
         * Inner class representing a recursive task to render the Julia set fractal
         * in parallel.
         */
        private class RenderTask extends RecursiveAction {
            private static final int THRESHOLD = 8000; // Adjusted for experimentation
            private final int startX, endX, startY, endY;
            private final Runnable completionCallback;

            /**
             * Constructs a RenderTask for a specific region of the panel.
             *
             * @param startX             The starting x-coordinate.
             * @param endX               The ending x-coordinate.
             * @param startY             The starting y-coordinate.
             * @param endY               The ending y-coordinate.
             * @param completionCallback The callback to run upon completion.
             */
            public RenderTask(int startX, int endX, int startY, int endY, Runnable completionCallback) {
                this.startX = startX;
                this.endX = endX;
                this.startY = startY;
                this.endY = endY;
                this.completionCallback = completionCallback;
            }

            @Override
            protected void compute() {
                if ((endX - startX) * (endY - startY) <= THRESHOLD) {
                    for (int x = startX; x < endX; x++) {
                        for (int y = startY; y < endY; y++) {
                            double zx = (x - (double) getWidth() / 2) / zoom + xOffset;
                            double zy = (y - (double) getHeight() / 2) / zoom + yOffset;
                            int iter = MAX_ITER;

                            while (zx * zx + zy * zy < 4 && iter > 0) {
                                // Real part of the constant for Julia Set
                                double cRe = -0.7;
                                double tmp = zx * zx - zy * zy + cRe;
                                // Imaginary part of the constant for Julia Set
                                double cIm = 0.27015;
                                zy = 2.0 * zx * zy + cIm;
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
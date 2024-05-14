package org.anarchadia.Fractals;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * LSystemViewer is a class that visualizes L-Systems as fractal images.
 * It supports zooming and panning using mouse interactions.
 */
public class LSystemViewer extends JFrame {

    private double zoom = 1.0;
    private double targetZoom = 1.0;
    private double xOffset = 0.0;
    private double targetXOffset = 0.0;
    private double yOffset = 0.0;
    private double targetYOffset = 0.0;
    private Point lastPoint;
    private BufferedImage fractalImage;
    private final LSystemPanel panel;

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private final Timer animationTimer;

    private final LSystem lSystem;

    /**
     * Constructs an LSystemViewer frame allowing visualization of L-Systems.
     */
    public LSystemViewer() {
        setTitle("L-System Viewer");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize L-System with rules and depth
        lSystem = new LSystem("FX", 6);
        lSystem.addRule('X', "X+YF+");
        lSystem.addRule('Y', "-FX-Y");

        lSystem.generate();

        panel = new LSystemPanel();
        add(panel);

        // Mouse listener for zooming in and out
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                lastPoint = null;
                if (SwingUtilities.isLeftMouseButton(e)) {
                    targetXOffset = (e.getX() - getWidth() / 2.0) / zoom;
                    targetYOffset = (e.getY() - getHeight() / 2.0) / zoom;
                    targetZoom *= 1.5;
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    targetZoom /= 1.5;
                    targetXOffset = -(e.getX() - getWidth() / 2.0) / zoom;
                    targetYOffset = -(e.getY() - getHeight() / 2.0) / zoom;
                }
                startAnimation();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
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
     * Starts the zoom and pan animation.
     */
    private void startAnimation() {
        if (!animationTimer.isRunning()) {
            animationTimer.start();
        }
    }

    /**
     * Handles the zoom and pan animation dynamics.
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
     * JPanel subclass to render the fractal image.
     */
    private class LSystemPanel extends JPanel {
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
         * Renders the L-System fractal to the panel.
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

            drawLSystem(g2d, lSystem.getResultingString(), width / 2 + (int) xOffset, height / 2 + (int) yOffset, zoom, Math.toRadians(90));

            g2d.dispose();

            renderingInProgress = false;
            repaint();
        }

        private void drawLSystem(Graphics2D g2d, String lSystemString, int startX, int startY, double zoom, double angle) {
            int x = startX;
            int y = startY;

            for (char c : lSystemString.toCharArray()) {
                switch (c) {
                    case 'F':
                        int x2 = (int) (x + Math.cos(angle) * 10 * zoom);
                        int y2 = (int) (y - Math.sin(angle) * 10 * zoom);
                        g2d.drawLine(x, y, x2, y2);
                        x = x2;
                        y = y2;
                        break;
                    case '+':
                        angle += Math.toRadians(90);
                        break;
                    case '-':
                        angle -= Math.toRadians(90);
                        break;
                }
            }
        }
    }

    /**
     * LSystem class that represents an L-System and generates the resulting string.
     */
    static class LSystem {
        private final String axiom;
        private String resultingString;
        private final Map<Character, String> rules;
        private final int depth;

        /**
         * Constructs an LSystem with the given axiom and depth.
         *
         * @param axiom the starting string for the L-System
         * @param depth the number of iterations to apply the rules
         */
        public LSystem(String axiom, int depth) {
            this.axiom = axiom;
            this.depth = depth;
            this.rules = new HashMap<>();
        }

        /**
         * Adds a rule to the L-System.
         *
         * @param key the character to be replaced
         * @param rule the string to replace the character
         */
        public void addRule(char key, String rule) {
            rules.put(key, rule);
        }

        /**
         * Generates the resulting string of the L-System by applying rules.
         */
        public void generate() {
            resultingString = axiom;
            for (int i = 0; i < depth; i++) {
                StringBuilder newString = new StringBuilder();
                for (char c : resultingString.toCharArray()) {
                    newString.append(rules.getOrDefault(c, String.valueOf(c)));
                }
                resultingString = newString.toString();
            }
        }

        /**
         * Gets the resulting string of the L-System after applying the rules.
         *
         * @return the resulting string
         */
        public String getResultingString() {
            return resultingString;
        }
    }
}
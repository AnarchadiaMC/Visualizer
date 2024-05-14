package org.anarchadia;

import org.anarchadia.Fractals.*;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Run the Mandelbrot viewer in the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MandelbrotViewer mandelbrotViewer = new MandelbrotViewer();
                mandelbrotViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                mandelbrotViewer.setVisible(true);
            }
        });

        // Run the Julia Set viewer in the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JuliaSetViewer juliaSetViewer = new JuliaSetViewer();
                juliaSetViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                juliaSetViewer.setVisible(true);
            }
        });

        // Run the Sierpinski Triangle viewer in the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SierpinskiTriangleViewer sierpinskiTriangleViewer = new SierpinskiTriangleViewer();
                sierpinskiTriangleViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                sierpinskiTriangleViewer.setVisible(true);
            }
        });

        // Run the Sierpinski Carpet viewer in the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SierpinskiCarpetViewer sierpinskiCarpetViewer = new SierpinskiCarpetViewer();
                sierpinskiCarpetViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                sierpinskiCarpetViewer.setVisible(true);
            }
        });

        // Run the Koch Snowflake viewer in the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                KochSnowflakeViewer kochSnowflakeViewer = new KochSnowflakeViewer();
                kochSnowflakeViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                kochSnowflakeViewer.setVisible(true);
            }
        });

        // Run the Dragon Curve viewer in the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DragonCurveViewer dragonCurveViewer = new DragonCurveViewer();
                dragonCurveViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                dragonCurveViewer.setVisible(true);
            }
        });

        // Run the Cantor set viewer in the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                CantorSetViewer cantorSetViewer = new CantorSetViewer();
                cantorSetViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                cantorSetViewer.setVisible(true);
            }
        });

        // Run the Barnsley Fern Viewer in the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                BarnsleyFernViewer barnsleyFernViewer = new BarnsleyFernViewer();
                barnsleyFernViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                barnsleyFernViewer.setVisible(true);
            }
        });

        // Run the Fractal Tree Viewer in the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                FractalTreeViewer fractalTreeViewer = new FractalTreeViewer();
                fractalTreeViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                fractalTreeViewer.setVisible(true);
            }
        });

        // Run the LSystem Viewer in the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LSystemViewer lSystemViewer = new LSystemViewer();
                lSystemViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                lSystemViewer.setVisible(true);
            }
        });
    }
}
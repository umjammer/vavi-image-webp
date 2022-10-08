/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;
import vavi.util.Debug;


/**
 * Test1.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/03/30 umjammer initial version <br>
 */
public class Test1 {

    @Test
    void test1() throws Exception {
        Path dir = Paths.get("src/test/resources/testdata");
        Files.walk(dir).filter(p -> p.getFileName().toString().matches(".+\\.webp")).forEach(f -> {
Debug.println(Level.INFO, f);
            try {
                ImageIO.read(f.toFile());
Debug.println(Level.INFO, f + ": OK");
            } catch (IOException e) {
Debug.println(Level.WARNING, f + ": " + e);
            }
        });
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        Test1 app = new Test1();
        app.exec();
    }

    BufferedImage image;

    /** */
    void exec() throws IOException {
        final JFrame frame = new JFrame();
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JPanel panel = new JPanel() {
            public void paintComponent(Graphics g) {
                g.drawImage(image, 0, 0, this);
            }
        };
        frame.getContentPane().add(panel);
        frame.setVisible(true);

        Path dir = Paths.get("src/test/resources/testdata");
        Files.walk(dir).filter(p -> p.getFileName().toString().matches(".+\\.webp")).forEach(f -> {
            try {
                image = ImageIO.read(f.toUri().toURL());
            } catch (Exception e) {
                Debug.println(f + ": " + e);
            }
            frame.setTitle(f.toString());
            panel.repaint();
        });
    }
}

/* */

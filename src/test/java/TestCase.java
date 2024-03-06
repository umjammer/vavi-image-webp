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
import java.util.Arrays;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vavi.util.Debug;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * TestCase.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/03/30 umjammer initial version <br>
 */
public class TestCase {

    String file = "src/test/resources/testdata/test.webp";

    @Test
    @DisplayName("spi")
    void test00() throws Exception {
        String[] rs = ImageIO.getReaderFormatNames();
System.err.println("-- reader --");
for (String r : rs) {
 System.err.println(r);
}
        assertTrue(Arrays.asList(rs).contains("WEBP"));
    }

    @Test
    @DisplayName("spi specified")
    void test01() throws Exception {
        ImageReader ir = ImageIO.getImageReadersByFormatName("webp").next();
        ImageInputStream iis = ImageIO.createImageInputStream(Files.newInputStream(Paths.get(file)));
        ir.setInput(iis);
        BufferedImage image = ir.read(0);
        assertNotNull(image);
    }

    @Test
    @DisplayName("spi auto")
    void test02() throws Exception {
        BufferedImage image = ImageIO.read(Files.newInputStream(Paths.get(file)));
        assertNotNull(image);
    }

    @Test
    void test1() throws Exception {
        Path dir = Paths.get("src/test/resources/testdata");
        Files.walk(dir).filter(p -> p.getFileName().toString().matches(".+\\.webp")).forEach(f -> {
Debug.println(Level.FINER, f);
            try {
                ImageIO.read(f.toFile());
Debug.println(Level.INFO, f + ": OK");
            } catch (IOException e) {
Debug.println(Level.WARNING, f + ": " + e);
                fail();
            }
        });
    }

    /** */
    public static void main(String[] args) throws Exception {
        TestCase app = new TestCase();
        app.exec();
    }

    BufferedImage image;

    /** */
    void exec() throws IOException {
        JFrame frame = new JFrame();
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel() {
            @Override
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

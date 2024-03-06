/*
 * This file is part of javavp8decoder.
 *
 * javavp8decoder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * javavp8decoder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with javavp8decoder.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.javavp8decoder.tools.vp8inspector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import org.ebml.matroska.MatroskaFile;

import net.sf.javavp8decoder.vp8Decoder.Globals;
import net.sf.javavp8decoder.vp8Decoder.SubBlock;
import net.sf.javavp8decoder.vp8Decoder.VP8Frame;


public class VP8Inspector extends JFrame implements MouseMotionListener, MouseListener, MouseWheelListener, ActionListener {

    /** */
    @Serial
    private static final long serialVersionUID = 1L;

    private BufferedImage bi;

    private final JCheckBox colorCodeCheckBox = new JCheckBox("Colour Code");

    private File currentFile = null;

    private final JRadioButton destButton = new JRadioButton("Dest");

    private JFileChooser fc = new JFileChooser();

    private final JButton fileOpenButton = new JButton("Open");

    private final JMenuItem fileOpenMenu;

    private VP8Frame frame;

    final JToolBar infoBar = new JToolBar();

    final JTextArea infoText = new JTextArea();

    private JPanel jp;

    private MatroskaFile matroskaFile;

    private IVFFile ivfFile;

    private final JCheckBox mBCheckBox = new JCheckBox("MB");

    final JMenuBar menuBar;

    private final JButton nextButton = new JButton("next");

    private final VP8Inspector panel;

    private BufferedImage predict;

    private final JRadioButton predictButton = new JRadioButton("Predict");

    private final JButton prevButton = new JButton("prev");

    private Point prevP;

    final JProgressBar progressBar;

    private BufferedImage residual;

    private final JRadioButton residualButton = new JRadioButton("Residual");

    private final JRadioButton rgbButton = new JRadioButton("RGB");

    private final JCheckBox sBCheckBox = new JCheckBox("SB");

    private float scale = 1.0f;

    private final JSlider slider = new JSlider();

    private final JScrollPane sp;

    final JToolBar toolBar;

    private BufferedImage ub;

    private final JRadioButton uButton = new JRadioButton("U");

    private BufferedImage upred;

    private BufferedImage uResidual;

    private BufferedImage vb;

    private final JRadioButton vButton = new JRadioButton("V");

    private BufferedImage vpred;

    private BufferedImage vResidual;

    private BufferedImage yb;

    private final JRadioButton yButton = new JRadioButton("Y");

    private BufferedImage ypred;

    private BufferedImage yResidual;

    VP8Inspector() {
        panel = this;
        this.setLayout(new BorderLayout());
        sp = new JScrollPane();
        toolBar = new JToolBar();
        mBCheckBox.addActionListener(this);
        sBCheckBox.addActionListener(this);
        colorCodeCheckBox.addActionListener(this);
        toolBar.add(fileOpenButton);
        fileOpenButton.addActionListener(this);
        toolBar.addSeparator();
        toolBar.add(mBCheckBox);
        toolBar.add(sBCheckBox);
        toolBar.add(colorCodeCheckBox);
        toolBar.addSeparator();
        ButtonGroup bgPlane = new ButtonGroup();
        ButtonGroup bgBuffer = new ButtonGroup();
        bgPlane.add(rgbButton);
        bgPlane.add(yButton);
        bgPlane.add(uButton);
        bgPlane.add(vButton);
        bgBuffer.add(destButton);
        bgBuffer.add(residualButton);
        bgBuffer.add(predictButton);
        rgbButton.addActionListener(this);
        yButton.addActionListener(this);
        uButton.addActionListener(this);
        vButton.addActionListener(this);
        destButton.addActionListener(this);
        residualButton.addActionListener(this);
        predictButton.addActionListener(this);
        toolBar.add(rgbButton);
        toolBar.add(yButton);
        toolBar.add(uButton);
        toolBar.add(vButton);
        toolBar.addSeparator();
        toolBar.add(destButton);
        toolBar.add(residualButton);
        toolBar.add(predictButton);
        rgbButton.setSelected(true);
        destButton.setSelected(true);
        this.add(toolBar, BorderLayout.NORTH);

        infoBar.setPreferredSize(new Dimension(300, 100));
        infoBar.add(infoText);
        this.add(infoBar, BorderLayout.EAST);

        menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        fileOpenMenu = new JMenuItem("Open");
        fileOpenMenu.addActionListener(this);
        menu.add(fileOpenMenu);

        this.setJMenuBar(menuBar);

        progressBar = new JProgressBar();
        progressBar.setValue(0);
        progressBar.setPreferredSize(new Dimension(18, 18));
        progressBar.setStringPainted(true);

        JPanel bp = new JPanel();
        bp.setLayout(new BorderLayout());
        bp.add(progressBar, BorderLayout.SOUTH);
        JPanel t = new JPanel();

        prevButton.setEnabled(false);
        prevButton.setVisible(false);
        t.add(prevButton, BorderLayout.CENTER);
        slider.setEnabled(false);
        slider.setMaximum(0);
        slider.setMajorTickSpacing(20);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        t.add(slider, BorderLayout.CENTER);
        nextButton.setEnabled(false);
        nextButton.addActionListener(this);
        t.add(nextButton, BorderLayout.CENTER);
        bp.add(t, BorderLayout.CENTER);
        add(bp, BorderLayout.SOUTH);

        progressBar.setVisible(false);
        this.add(sp, BorderLayout.CENTER);
        // loadImageData();
        this.setTitle("VP8Inspector");
        this.setVisible(true);
        this.setSize(800, 600);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if ((e.getSource() == nextButton))
            loadNextFrame();

        if ((e.getSource() == mBCheckBox || e.getSource() == sBCheckBox || e.getSource() == colorCodeCheckBox) && jp != null)
            jp.repaint();
        if (e.getSource() == fileOpenMenu || e.getSource() == fileOpenButton && !progressBar.isVisible()) {
            fc = new JFileChooser();

            fc.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".webp")
                           || f.getName().toLowerCase().endsWith(".webm") || f.getName().toLowerCase().endsWith(".ivf");
                }

                @Override
                public String getDescription() {
                    return "WebM/IVF/WebP Files";
                }
            });
            fc.setSelectedFile(new File("g:\\workspace\\javavp8decoder\\vp8-test-vectors"));
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                sp.setViewport(null);
                jp = null;
                scale = 1.0f;
                currentFile = fc.getSelectedFile();
                matroskaFile = null;
                ivfFile = null;
                loadImageData();
            } else {

            }
        }
        if (e.getSource() == rgbButton || e.getSource() == destButton || e.getSource() == residualButton
            || e.getSource() == predictButton || e.getSource() == yButton || e.getSource() == uButton
            || e.getSource() == vButton) {

            if (frame != null) {
                jp.repaint();
            }
        }
    }

    private boolean frameReader() throws IOException {
        if (Utils.getExtension(currentFile).equals("webp")) {
            ImageInputStream stream = ImageIO.createImageInputStream(currentFile);
            Utils.getWebPFrame(stream);
            frame = new VP8Frame(stream);
            nextButton.setEnabled(false);
        } else if (matroskaFile != null) {
            byte[] data = Utils.getMatroskaFrame(matroskaFile);

            if (data != null) {
                InputStream bais = new ByteArrayInputStream(data);
                ImageInputStream iis = ImageIO.createImageInputStream(bais);
                frame = new VP8Frame(iis);
            }
        } else if (ivfFile != null) {
            byte[] data = ivfFile.getNextFrame();

            if (data != null) {
                InputStream bais = new ByteArrayInputStream(data);
                ImageInputStream iis = ImageIO.createImageInputStream(bais);
                frame = new VP8Frame(iis);
            }
        } else
            return false;

        frame.addIIOReadProgressListener(new IIOReadProgressListener() {
            @Override
            public void imageComplete(ImageReader source) {
            }

            @Override
            public void imageProgress(ImageReader source, float percentageDone) {
                progressBar.setValue((int) percentageDone);
            }

            @Override
            public void imageStarted(ImageReader source, int imageIndex) {
            }

            @Override
            public void readAborted(ImageReader source) {
            }

            @Override
            public void sequenceComplete(ImageReader source) {
            }

            @Override
            public void sequenceStarted(ImageReader source, int minIndex) {
            }

            @Override
            public void thumbnailComplete(ImageReader source) {
            }

            @Override
            public void thumbnailProgress(ImageReader source, float percentageDone) {
            }

            @Override
            public void thumbnailStarted(ImageReader source, int imageIndex, int thumbnailIndex) {
            }
        });
        frame.setBuffersToCreate(12);
        if (frame.decodeFrame(true)) {
            bi = frame.getBufferedImage();
            residual = frame.getDebugImageDiff();
            predict = frame.getDebugImagePredict();
            yb = frame.getDebugImageYBuffer();
            ub = frame.getDebugImageUBuffer();
            vb = frame.getDebugImageVBuffer();
            yResidual = frame.getDebugImageYDiffBuffer();
            uResidual = frame.getDebugImageUDiffBuffer();
            vResidual = frame.getDebugImageVDiffBuffer();
            ypred = frame.getDebugImageYPredBuffer();
            upred = frame.getDebugImageUPredBuffer();
            vpred = frame.getDebugImageVPredBuffer();
            return true;
        } else
            return false;
    }

    private void loadImageData() {

        new Thread() {
            @Override
            public void run() {
                try {
                    setTitle("VP8Inspector - " + currentFile.getName() + " (Loading...)");
                    nextButton.setEnabled(false);
                    invalidate();
                    progressBar.setVisible(true);
                    if (Utils.getExtension(currentFile).equals("webm")) {
                        if (matroskaFile == null) {
                            matroskaFile = Utils.loadMatroska(currentFile);
                            if (matroskaFile != null) {
                                int kfs = Utils.countKeyFrames(matroskaFile);
                                slider.setMaximum(kfs - 1);
                                slider.setValue(0);
                                matroskaFile = Utils.loadMatroska(currentFile);
                            }
                        }
                    } else if (Utils.getExtension(currentFile).equals("ivf")) {
                        if (ivfFile == null) {
                            ivfFile = new IVFFile(currentFile);
                            int kfs = ivfFile.getKeyFrames();
                            slider.setMaximum(kfs - 1);
                            slider.setValue(0);
                        }
                    }
                    if (!frameReader()) {
                        JOptionPane.showMessageDialog(null, "Failed to load " + currentFile.getName());
                        setTitle("VP8Inspector");
                        progressBar.setVisible(false);
                        progressBar.setValue(0);
                        return;
                    }

                    class ImagePanel extends JPanel {
                        /**
                         *
                         */
                        @Serial
                        private static final long serialVersionUID = 1L;

                        private final BufferedImage bi;

                        private final BufferedImage predict;

                        private final BufferedImage residual;

                        private final BufferedImage ub;

                        private final BufferedImage uPred;

                        private final BufferedImage uResidual;

                        private final BufferedImage vb;

                        private final BufferedImage vPred;

                        private final BufferedImage vResidual;

                        private final BufferedImage yb;

                        private final BufferedImage yPred;

                        private final BufferedImage yResidual;

                        public ImagePanel(BufferedImage bi,
                                BufferedImage yb,
                                BufferedImage ub,
                                BufferedImage vb,
                                BufferedImage residual,
                                BufferedImage predict,
                                BufferedImage yResidual,
                                BufferedImage uResidual,
                                BufferedImage vResidual,
                                BufferedImage yPred,
                                BufferedImage uPred,
                                BufferedImage vPred) {
                            this.bi = bi;
                            this.yb = yb;
                            this.ub = ub;
                            this.vb = vb;
                            this.residual = residual;
                            this.predict = predict;
                            this.yResidual = yResidual;
                            this.uResidual = uResidual;
                            this.vResidual = vResidual;
                            this.yPred = yPred;
                            this.uPred = uPred;
                            this.vPred = vPred;

                        }

                        @Override
                        public void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            BufferedImage bi = this.bi;
                            if (rgbButton.isSelected() && destButton.isSelected())
                                bi = this.bi;
                            if (rgbButton.isSelected() && residualButton.isSelected())
                                bi = this.residual;
                            if (rgbButton.isSelected() && predictButton.isSelected())
                                bi = this.predict;
                            if (yButton.isSelected() && destButton.isSelected())
                                bi = this.yb;
                            if (yButton.isSelected() && residualButton.isSelected())
                                bi = this.yResidual;
                            if (yButton.isSelected() && predictButton.isSelected())
                                bi = this.yPred;
                            if (uButton.isSelected() && destButton.isSelected())
                                bi = this.ub;
                            if (uButton.isSelected() && residualButton.isSelected())
                                bi = this.uResidual;
                            if (uButton.isSelected() && predictButton.isSelected())
                                bi = this.uPred;
                            if (vButton.isSelected() && destButton.isSelected())
                                bi = this.vb;
                            if (vButton.isSelected() && residualButton.isSelected())
                                bi = this.vResidual;
                            if (vButton.isSelected() && predictButton.isSelected())
                                bi = this.vPred;

                            if (bi != null) {
                                g.drawImage(bi, 0, 0, (int) (bi.getWidth() * scale), (int) (bi.getHeight() * scale), null);
                                if (sBCheckBox.isSelected()) {
                                    g.setColor(Color.WHITE);
                                    for (int x = 4; x < bi.getHeight(); x += 4)
                                        g.drawLine(0, (int) (x * scale), (int) (bi.getWidth() * scale), (int) (x * scale));
                                    for (int y = 4; y < bi.getWidth(); y += 4)
                                        g.drawLine((int) (y * scale), 0, (int) (y * scale), (int) (bi.getHeight() * scale));
                                }
                                if (mBCheckBox.isSelected()) {
                                    g.setColor(Color.BLACK);
                                    for (int x = 16; x < bi.getHeight(); x += 16)
                                        g.drawLine(0, (int) (x * scale), (int) (bi.getWidth() * scale), (int) (x * scale));
                                    for (int y = 16; y < bi.getWidth(); y += 16)
                                        g.drawLine((int) (y * scale), 0, (int) (y * scale), (int) (bi.getHeight() * scale));
                                }
                                if (colorCodeCheckBox.isSelected() && scale > 0.1f) {
                                    Graphics2D g2 = (Graphics2D) g;
                                    for (int y = 0; y < bi.getHeight(); y += 16)
                                        for (int x = 0; x < bi.getWidth(); x += 16) {
                                            Rectangle r = jp.getVisibleRect();
                                            if (r.contains((x + 8) * scale, (y + 8) * scale)) {
                                                int mbx = x / 16;
                                                int mby = y / 16;
                                                int mode = frame.getMacroBlock(mbx, mby).getUvMode();
                                                if (yButton.isSelected() || rgbButton.isSelected()) {
                                                    mode = frame.getMacroBlock(mbx, mby).getYMode();
                                                }

                                                switch (mode) {
                                                case Globals.DC_PRED:
                                                    g.setColor(new Color(0, 0, 255, 50));
                                                    break;
                                                case Globals.V_PRED:
                                                    g.setColor(new Color(255, 0, 0, 50));
                                                    break;
                                                case Globals.H_PRED:
                                                    g.setColor(new Color(0, 255, 255, 50));
                                                    break;
                                                case Globals.TM_PRED:
                                                    g.setColor(new Color(255, 0, 255, 50));
                                                    break;
                                                case Globals.B_PRED:
                                                    g.setColor(new Color(255, 255, 255, 50));
                                                    break;
                                                }
                                                if (mode != Globals.B_PRED)
                                                    g2.fillOval((int) (x * scale) + (int) (16 * scale) / 4,
                                                                (int) (y * scale) + (int) (16 * scale) / 4,
                                                                (int) (16 * scale) / 2,
                                                                (int) (16 * scale) / 2);
                                                else if (scale > 0.9f) {
                                                    for (int sby = 0; sby < 4; sby++) {
                                                        for (int sbx = 0; sbx < 4; sbx++) {
                                                            switch (frame.getMacroBlock(mbx, mby)
                                                                    .getSubBlock(SubBlock.PLANE.Y1, sbx, sby)
                                                                    .getMode()) {
                                                            case Globals.B_DC_PRED:
                                                                g.setColor(new Color(0, 0, 255, 150));
                                                                break;
                                                            case Globals.B_TM_PRED:
                                                                g.setColor(new Color(255, 0, 255, 150));
                                                                break;
                                                            case Globals.B_VE_PRED:
                                                                g.setColor(new Color(255, 255, 127, 150));
                                                                break;
                                                            case Globals.B_HE_PRED:
                                                                g.setColor(new Color(255, 0, 255, 150));
                                                                break;
                                                            case Globals.B_LD_PRED:
                                                                g.setColor(new Color(0, 255, 255, 150));
                                                                break;
                                                            case Globals.B_RD_PRED:
                                                                g.setColor(new Color(255, 255, 0, 150));
                                                                break;
                                                            case Globals.B_VR_PRED:
                                                                g.setColor(new Color(255, 127, 255, 150));
                                                                break;
                                                            case Globals.B_VL_PRED:
                                                                g.setColor(new Color(127, 255, 127, 150));
                                                                break;
                                                            case Globals.B_HD_PRED:
                                                                g.setColor(new Color(127, 127, 255, 150));
                                                                break;
                                                            case Globals.B_HU_PRED:
                                                                g.setColor(new Color(127, 255, 255, 150));
                                                                break;
                                                            }
                                                            g.fillOval((int) (x * scale) + (int) ((sbx * 4) * scale)
                                                                       + (int) (4 * scale) / 4,
                                                                       (int) (y * scale) + (int) ((sby * 4) * scale)
                                                                                                + (int) (4 * scale) / 4,
                                                                       (int) (4 * scale) / 2,
                                                                       (int) (4 * scale) / 2);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                }

                                if (this.getMousePosition() != null) {
                                    g.setColor(Color.RED);
                                    g.drawRect((int) (((this.getMousePosition().x)
                                                       - (this.getMousePosition().x) % (16 * scale))),
                                               (int) (((this.getMousePosition().y)
                                                       - (this.getMousePosition().y) % (16 * scale))),
                                               (int) (16 * scale),
                                               (int) (16 * scale));
                                    g.setColor(Color.GREEN);
                                    g.drawRect((int) (((this.getMousePosition().x)
                                                       - (this.getMousePosition().x) % (4 * scale))),
                                               (int) (((this.getMousePosition().y)
                                                       - (this.getMousePosition().y) % (4 * scale))),
                                               (int) (4 * scale),
                                               (int) (4 * scale));
                                }

                            }
                        }
                    }
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                    setTitle("VP8Inspector - " + currentFile.getName());
                    jp = new ImagePanel(bi,
                                        yb,
                                        ub,
                                        vb,
                                        residual,
                                        predict,
                                        yResidual,
                                        uResidual,
                                        vResidual,
                                        ypred,
                                        upred,
                                        vpred);
                    jp.setPreferredSize(new Dimension(bi.getWidth(), bi.getHeight()));
                    setPreferredSize(new Dimension(bi.getWidth(), bi.getHeight()));
                    // pack();
                    jp.invalidate();
                    jp.addMouseMotionListener(panel);
                    jp.addMouseListener(panel);
                    jp.addMouseWheelListener(panel);
                    // sp.add(jp);
                    sp.setViewportView(jp);
                    if (slider.getMaximum() > 1)
                        nextButton.setEnabled(true);
                    updateInfoText(null);
                    invalidate();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null,
                                                  e.getMessage(),
                                                  "Error loading " + currentFile.getName(),
                                                  JOptionPane.ERROR_MESSAGE);
                } catch (java.lang.OutOfMemoryError e) {
                    JOptionPane.showMessageDialog(null,
                                                  "Out of Memory " + e.getMessage(),
                                                  "Out of Memory loading " + currentFile.getName(),
                                                  JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
            }
        }.start();

    }

    private void loadNextFrame() {
        if ((matroskaFile == null && ivfFile == null) || (slider.getValue() >= slider.getMaximum())) {
            nextButton.setEnabled(false);
            return;
        }
        slider.setValue(slider.getValue() + 1);
        loadImageData();
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (prevP == null) {
            prevP = new Point(e.getXOnScreen(), e.getYOnScreen());
            return;
        }
        JScrollBar hsb = sp.getHorizontalScrollBar();
        JScrollBar vsb = sp.getVerticalScrollBar();
        int deltaX = hsb.getValue() - (e.getXOnScreen() - prevP.x);

        int deltaY = vsb.getValue() - (e.getYOnScreen() - prevP.y);
        hsb.setValue(deltaX);
        vsb.setValue(deltaY);
        prevP = new Point(e.getXOnScreen(), e.getYOnScreen());
        setCursor(new Cursor(Cursor.MOVE_CURSOR));
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        updateInfoText(e.getPoint());
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
        prevP = null;
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jp.repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        jp.repaint();
        scale = scale - scale * ((e.getWheelRotation() * 0.1f));
        e.consume();
        jp.setPreferredSize(new Dimension((int) (bi.getWidth() * scale), (int) (bi.getHeight() * scale)));
        jp.setSize(new Dimension((int) (bi.getWidth() * scale), (int) (bi.getHeight() * scale)));
        // sp.remove(jp);
        // sp.add(jp);
        sp.repaint();

    }

    public void updateInfoText(Point p) {
        infoText.setText("Width: " + bi.getWidth() + "\nHeight: " + bi.getHeight());

        if (p != null) {
            int mbx = (((int) (p.x / scale)) / 16);
            int mby = (((int) (p.y / scale)) / 16);
            int sbx = (((int) (p.x / scale)) / 4) % 4;
            int sby = (((int) (p.y / scale)) / 4) % 4;
            infoText.setText(infoText.getText() + "\nMacroBlock: " + mbx + ", " + mby);
            infoText.setText(infoText.getText() + "\n" + frame.getMacroBlockDebugString(mbx, mby, sbx, sby));
            infoText.setText(infoText.getText() + "\nPixel: " + (int) (p.x / scale) + ", " + (int) (p.y / scale));

        }
        jp.repaint();
    }
}

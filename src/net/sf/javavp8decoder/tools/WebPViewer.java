/*	This file is part of javavp8decoder.

    javavp8decoder is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    javavp8decoder is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with javavp8decoder.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.javavp8decoder.tools;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import net.sf.javavp8decoder.imageio.WebPImageReaderSpi;

public class WebPViewer extends JFrame implements MouseMotionListener,
		MouseListener, MouseWheelListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		WebPViewer app;
		try {
			IIORegistry r = javax.imageio.spi.IIORegistry.getDefaultInstance();
			WebPImageReaderSpi s = new WebPImageReaderSpi();
			r.registerServiceProvider(s);
		}
		catch (NoClassDefFoundError e) {
			JOptionPane.showMessageDialog(null, "Error loading WebP ImageIo plugin",
					"Error loading WebP ImageIo plugin",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			// handle exception
		} catch (ClassNotFoundException e) {
			// handle exception
		} catch (InstantiationException e) {
			// handle exception
		} catch (IllegalAccessException e) {
			// handle exception
		}

		app = new WebPViewer();
		app.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	private BufferedImage bi;

	private final JFileChooser fc = new JFileChooser();
	private JButton fileOpenButton = new JButton("Open");
	private JMenuItem fileOpenMenu;
	private JPanel jp;
	JMenuBar menuBar;
	private WebPViewer panel;
	private Point prevP;
	JProgressBar progressBar;
	private float scale = 1.0f;
	private JScrollPane sp;
	JToolBar toolBar;

	WebPViewer() {
		panel = this;
		this.setLayout(new BorderLayout());
		sp = new JScrollPane();
		toolBar = new JToolBar();

		toolBar.add(fileOpenButton);
		fileOpenButton.addActionListener(this);
		toolBar.addSeparator();

		toolBar.addSeparator();

		this.add(toolBar, BorderLayout.NORTH);

		menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menuBar.add(menu);
		fileOpenMenu = new JMenuItem("Open");
		fileOpenMenu.addActionListener(this);
		menu.add(fileOpenMenu);

		this.setJMenuBar(menuBar);

		progressBar = new JProgressBar();
		progressBar.setValue(00);
		progressBar.setPreferredSize(new Dimension(18, 18));
		progressBar.setStringPainted(true);
		add(progressBar, BorderLayout.SOUTH);
		progressBar.setVisible(false);
		this.add(sp, BorderLayout.CENTER);
		// loadImageData();
		this.setTitle("WebPViewer");
		this.setVisible(true);
		this.setSize(800, 600);
	}

	public void actionPerformed(ActionEvent e) {
		if (jp != null)
			jp.repaint();
		if (e.getSource() == fileOpenMenu || e.getSource() == fileOpenButton
				&& !progressBar.isVisible()) {

			fc.setFileFilter(new FileFilter() {
				public boolean accept(File f) {
					return f.isDirectory()
							|| f.getName().toLowerCase().endsWith(".webp");
				}

				public String getDescription() {
					return "webp image files";
				}
			});
			fc.setSelectedFile(new File(
					"g:\\workspace\\javavp8decoder\\testdata\\test.webp"));
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				sp.setViewport(null);
				jp = null;
				File file = fc.getSelectedFile();
				scale = 1.0f;
				loadImageData(file);
			} else {

			}
		}
	}

	private void loadImageData(final File f) {

		new Thread() {
			public void run() {
				// File f = new File("testdata/samples/1_original.webp");
				try {

					setTitle("WebPViewer - " + f.getName() + " (Loading...)");
					invalidate();
					progressBar.setVisible(true);
					java.util.Iterator<ImageReader> readers = ImageIO
							.getImageReadersBySuffix("WEBP");
					ImageReader imageReader = (ImageReader) readers.next();
					ImageInputStream iis = ImageIO.createImageInputStream(f);
					imageReader.setInput(iis, false);

					imageReader
							.addIIOReadProgressListener(new IIOReadProgressListener() {
								public void imageComplete(ImageReader source) {
								}

								public void imageProgress(ImageReader source,
										float percentageDone) {
									progressBar.setValue((int) percentageDone);
								}

								public void imageStarted(ImageReader source,
										int imageIndex) {
								}

								public void readAborted(ImageReader source) {
								}

								public void sequenceComplete(ImageReader source) {
								}

								public void sequenceStarted(ImageReader source,
										int minIndex) {
								}

								public void thumbnailComplete(ImageReader source) {
								}

								public void thumbnailProgress(
										ImageReader source, float percentageDone) {
								}

								public void thumbnailStarted(
										ImageReader source, int imageIndex,
										int thumbnailIndex) {
								}
							});
					bi = imageReader.read(0);
					class ImagePanel extends JPanel {
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;
						private BufferedImage bi;

						public ImagePanel(BufferedImage bi) {
							this.bi = bi;
						}

						public void paintComponent(Graphics g) {
							super.paintComponent(g);
							BufferedImage bi = this.bi;

							if (bi != null) {
								g.drawImage(bi, 0, 0,
										(int) (bi.getWidth() * scale),
										(int) (bi.getHeight() * scale), null);
							}
						}

					}
					;
					progressBar.setVisible(false);
					progressBar.setValue(0);
					setTitle("WebPViewer - " + f.getName());
					jp = new ImagePanel(bi);
					jp.setPreferredSize(new Dimension(bi.getWidth(), bi
							.getHeight()));
					setPreferredSize(new Dimension(bi.getWidth(),
							bi.getHeight()));
					// pack();
					jp.invalidate();
					jp.addMouseMotionListener(panel);
					jp.addMouseListener(panel);
					jp.addMouseWheelListener(panel);
					// sp.add(jp);
					sp.setViewportView(jp);

					invalidate();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(),
							"Error loading " + f.getName(),
							JOptionPane.ERROR_MESSAGE);
				} catch (java.lang.OutOfMemoryError e) {
					JOptionPane.showMessageDialog(null,
							"Out of Memory " + e.getMessage(),
							"Out of Memory loading " + f.getName(),
							JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
			}
		}.start();

	}

	public void mouseClicked(MouseEvent arg0) {
	}

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

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mouseMoved(MouseEvent e) {

	}

	public void mousePressed(MouseEvent arg0) {
	}

	public void mouseReleased(MouseEvent arg0) {
		prevP = null;
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		jp.repaint();
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		jp.repaint();
		scale = scale - scale * ((e.getWheelRotation() * 0.1f));
		e.consume();
		jp.setPreferredSize(new Dimension((int) (bi.getWidth() * scale),
				(int) (bi.getHeight() * scale)));
		jp.setSize(new Dimension((int) (bi.getWidth() * scale), (int) (bi
				.getHeight() * scale)));
		// sp.remove(jp);
		// sp.add(jp);
		sp.repaint();

	}
}

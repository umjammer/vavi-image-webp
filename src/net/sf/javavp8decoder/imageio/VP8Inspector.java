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
package net.sf.javavp8decoder.imageio;

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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOException;
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
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import net.sf.javavp8decoder.vp8Decoder.Globals;
import net.sf.javavp8decoder.vp8Decoder.SubBlock;
import net.sf.javavp8decoder.vp8Decoder.VP8Frame;


public class VP8Inspector extends JFrame implements MouseMotionListener, MouseListener, MouseWheelListener, ActionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private VP8Inspector panel;

	private JScrollPane sp;
	private JPanel jp;
	private JCheckBox mBCheckBox = new JCheckBox("MB");
	private JCheckBox sBCheckBox = new JCheckBox("SB");
	private JCheckBox colorCodeCheckBox = new JCheckBox("Colour Code");
	private JRadioButton rgbButton = new JRadioButton("RGB");
	private JRadioButton yButton = new JRadioButton("Y");
	private JRadioButton uButton = new JRadioButton("U");
	private JRadioButton vButton = new JRadioButton("V");
	private JRadioButton destButton = new JRadioButton("Dest");
	private JRadioButton residualButton = new JRadioButton("Residual");
	private JRadioButton predictButton = new JRadioButton("Predict");
	JProgressBar progressBar;
	JToolBar toolBar;
	JToolBar infoBar = new JToolBar();
	JTextArea infoText = new JTextArea();
	JMenuBar menuBar;
	private JMenuItem fileOpenMenu;
	private JButton fileOpenButton = new JButton("Open");
	private final JFileChooser fc = new JFileChooser();
	private VP8Frame frame;
	private BufferedImage bi;
	private BufferedImage residual;
	private BufferedImage predict;
	private BufferedImage yb;
	private BufferedImage ub;
	private BufferedImage vb;
	private BufferedImage yResidual;
	private BufferedImage uResidual;
	private BufferedImage vResidual;
	private BufferedImage ypred;
	private BufferedImage upred;
	private BufferedImage vpred;

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
		progressBar.setValue(00);
		progressBar.setPreferredSize(new Dimension(18, 18));
		progressBar.setStringPainted(true);
		add(progressBar, BorderLayout.SOUTH);
		progressBar.setVisible(false);
		this.add(sp, BorderLayout.CENTER);
		//loadImageData();
		this.setTitle("VP8Inspector");
		this.setVisible(true);
		this.setSize(800, 600);
	}
	
	private void frameReader(File file) throws IOException {
		ImageInputStream stream = ImageIO.createImageInputStream(file);
		VP8Inspector.getFrame(stream);
		frame = new VP8Frame(stream);
		
	    frame.addIIOReadProgressListener(new IIOReadProgressListener() {
	        public void imageProgress(ImageReader source, float percentageDone) {
	        	progressBar.setValue((int) percentageDone);
	        }
	        public void imageComplete(ImageReader source) {}
	        public void imageStarted(ImageReader source, int imageIndex) {}
	        public void readAborted(ImageReader source) {}
	        public void sequenceComplete(ImageReader source) {}
	        public void sequenceStarted(ImageReader source, int minIndex) {}
	        public void thumbnailComplete(ImageReader source) {}
	        public void thumbnailProgress(ImageReader source, float percentageDone) {}
	        public void thumbnailStarted(ImageReader source, int imageIndex, int thumbnailIndex) {}
	      });
	    frame.setBuffersToCreate(12);
	    frame.decodeFrame(true);
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

	}

	private void loadImageData(final File f) {
		
		new Thread() {
			public void run() {
				//File f = new File("testdata/samples/1_original.webp");
				try {
					
					setTitle("VP8Inspector - "+f.getName()+" (Loading...)");
					invalidate();
					progressBar.setVisible(true);
				    frameReader(f);
					class ImagePanel extends JPanel {
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;
						private BufferedImage bi;
						private BufferedImage residual;
						private BufferedImage predict;
						private BufferedImage yb;
						private BufferedImage ub;
						private BufferedImage vb;
						private BufferedImage yResidual;
						private BufferedImage uResidual;
						private BufferedImage vResidual;
						private BufferedImage yPred;
						private BufferedImage uPred;
						private BufferedImage vPred;
						public ImagePanel(BufferedImage bi, BufferedImage yb, BufferedImage ub, BufferedImage vb, BufferedImage residual, BufferedImage predict, 
								BufferedImage yResidual, BufferedImage uResidual, BufferedImage vResidual, 
								BufferedImage yPred, BufferedImage uPred, BufferedImage vPred ) {
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

						public void paintComponent(Graphics g) {
							super.paintComponent(g);
							BufferedImage bi=this.bi;
							if(rgbButton.isSelected() && destButton.isSelected())
								bi=this.bi;
							if(rgbButton.isSelected() && residualButton.isSelected())
								bi=this.residual;
							if(rgbButton.isSelected() && predictButton.isSelected())
								bi=this.predict;
							if(yButton.isSelected() && destButton.isSelected())
								bi=this.yb;
							if(yButton.isSelected() && residualButton.isSelected())
								bi=this.yResidual;
							if(yButton.isSelected() && predictButton.isSelected())
								bi=this.yPred;
							if(uButton.isSelected() && destButton.isSelected())
								bi=this.ub;
							if(uButton.isSelected() && residualButton.isSelected())
								bi=this.uResidual;
							if(uButton.isSelected() && predictButton.isSelected())
								bi=this.uPred;
							if(vButton.isSelected() && destButton.isSelected())
								bi=this.vb;
							if(vButton.isSelected() && residualButton.isSelected())
								bi=this.vResidual;
							if(vButton.isSelected() && predictButton.isSelected())
								bi=this.vPred;

							if(bi!=null) {
								g.drawImage(bi, 0, 0, (int) (bi.getWidth()*scale), (int) (bi.getHeight()*scale), null);
								if(sBCheckBox.isSelected()) {
									g.setColor(Color.WHITE);
									for(int x=4; x<bi.getHeight(); x+=4)
										g.drawLine(0, (int) (x*scale), (int) (bi.getWidth()*scale), (int) (x*scale));
									for(int y=4; y<bi.getWidth(); y+=4)
										g.drawLine((int) (y*scale), 0, (int) (y*scale), (int) (bi.getHeight()*scale));
								}
								if(mBCheckBox.isSelected()) {
									g.setColor(Color.BLACK);
									for(int x=16; x<bi.getHeight(); x+=16)
										g.drawLine(0, (int) (x*scale), (int) (bi.getWidth()*scale), (int) (x*scale));
									for(int y=16; y<bi.getWidth(); y+=16)
										g.drawLine((int) (y*scale), 0, (int) (y*scale), (int) (bi.getHeight()*scale));
								}
								if(colorCodeCheckBox.isSelected() && scale>0.1f) {
									Graphics2D g2 = (Graphics2D)g;
									for(int y=0; y<bi.getHeight(); y+=16)
										for(int x=0; x<bi.getWidth(); x+=16) {
											Rectangle r = jp.getVisibleRect();
											if(r.contains((x+8)*scale, (y+8)*scale)) {
												int mbx=x/16;
												int mby=y/16;
												int mode = frame.getMacroBlock(mbx, mby).getUvMode();
												if(yButton.isSelected() || rgbButton.isSelected()){
													mode = frame.getMacroBlock(mbx, mby).getYMode();	
												}
												
												switch(mode) {
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
												if(mode!=Globals.B_PRED) 
													g2.fillOval((int)(x*scale)+(int)(16*scale)/4, (int)(y*scale)+(int)(16*scale)/4, (int) (16*scale)/2, (int) (16*scale)/2);
												else if(scale>0.9f){
													for(int sby=0; sby<4; sby++) {
														for(int sbx=0; sbx<4; sbx++) {
															switch(frame.getMacroBlock(mbx, mby).getSubBlock(SubBlock.PLANE.Y1, sbx, sby).getMode()) {
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
																g.setColor(new Color(127,255, 255, 150));
																break;
															}
															g.fillOval((int)(x*scale)+(int)((sbx*4)*scale)+(int) (4*scale)/4, (int)(y*scale)+(int)((sby*4)*scale)+(int) (4*scale)/4, (int) (4*scale)/2, (int) (4*scale)/2);
														}
													}
												}
											}
										}
									}
							
								if(this.getMousePosition()!=null) {
									g.setColor(Color.RED);
									g.drawRect((int) (((this.getMousePosition().x)-(this.getMousePosition().x)%(16*scale))), (int) (((this.getMousePosition().y)-(this.getMousePosition().y)%(16*scale))), (int)(16*scale), (int)(16*scale));
									g.setColor(Color.GREEN);
									g.drawRect((int) (((this.getMousePosition().x)-(this.getMousePosition().x)%(4*scale))), (int) (((this.getMousePosition().y)-(this.getMousePosition().y)%(4*scale))), (int)(4*scale), (int)(4*scale));
								}
							
							}
						}
					};
					progressBar.setVisible(false);
					progressBar.setValue(0);
					setTitle("VP8Inspector - "+f.getName());
					jp = new ImagePanel(bi, yb, ub, vb, residual, predict, yResidual, uResidual, vResidual, ypred, upred, vpred);
					jp.setPreferredSize(new Dimension(bi.getWidth(), bi.getHeight()));
					setPreferredSize(new Dimension(bi.getWidth(), bi.getHeight()));
					//pack();
					jp.invalidate();
					jp.addMouseMotionListener(panel);
					jp.addMouseListener(panel);
					jp.addMouseWheelListener(panel);
					//sp.add(jp);
					sp.setViewportView(jp);
					updateInfoText(null);
					invalidate();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "Error loading "+f.getName(), JOptionPane.ERROR_MESSAGE); 
				}
			    catch(java.lang.OutOfMemoryError e) {
			    	JOptionPane.showMessageDialog(null, "Out of Memory "+e.getMessage(), "Out of Memory loading "+f.getName(), JOptionPane.ERROR_MESSAGE);
			    	System.exit(0);
			    }
			}
		}.start();
		
	}

	public static void main(String[] args) {
		VP8Inspector app;
		app = new VP8Inspector();
		app.addWindowListener(new WindowAdapter() 
		{
			public void windowClosing( WindowEvent e )
			{
				System.exit( 0 );
			}
		});
	}

	private Point prevP;
	public void mouseDragged(MouseEvent e) {
		if(prevP == null){
			prevP = new Point(e.getXOnScreen(), e.getYOnScreen());
			return;
		}
		JScrollBar hsb = sp.getHorizontalScrollBar();
		JScrollBar vsb = sp.getVerticalScrollBar();
		int deltaX = hsb.getValue()-(e.getXOnScreen()-prevP.x);

	
		int deltaY = vsb.getValue()-(e.getYOnScreen()-prevP.y);
		hsb.setValue(deltaX);
		vsb.setValue(deltaY);
		prevP = new Point(e.getXOnScreen(), e.getYOnScreen());
		setCursor(new Cursor(Cursor.MOVE_CURSOR));
	}


	public void mouseMoved(MouseEvent e) {
		updateInfoText(e.getPoint());
	}
	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {
		prevP=null;
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		jp.repaint();
	}

	private float scale = 1.0f;
	public void mouseWheelMoved(MouseWheelEvent e) {
		jp.repaint();
		scale=scale-scale*((e.getWheelRotation()*0.1f));
		e.consume();
		  jp.setPreferredSize(new Dimension((int) (bi.getWidth()*scale), (int) (bi.getHeight()*scale)));
		  jp.setSize(new Dimension((int) (bi.getWidth()*scale), (int) (bi.getHeight()*scale)));
		  //sp.remove(jp);
		  //sp.add(jp);
		  sp.repaint();
		
	}
	
	public void actionPerformed(ActionEvent e) {

		if((e.getSource()==mBCheckBox || e.getSource()==sBCheckBox || e.getSource()==colorCodeCheckBox) &&jp!=null)
			jp.repaint();
		if(e.getSource()==fileOpenMenu || e.getSource()==fileOpenButton && !progressBar.isVisible()) {

		    fc.setFileFilter(new FileFilter() {
		       	public boolean accept(File f) {
		       		return f.isDirectory() || f.getName().toLowerCase().endsWith(".webp");
				}
				public String getDescription() {
					return "webp image files";
				}
		      });
			fc.setSelectedFile(new File("g:\\workspace\\javavp8decoder\\testdata\\test.webp"));
			int returnVal = fc.showOpenDialog(this);
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	        	sp.setViewport(null);
	        	jp=null;
	            File file = fc.getSelectedFile();
	            scale=1.0f;
	            loadImageData(file);
	        } else {

	        }
		}
		if(e.getSource()==rgbButton || e.getSource()==destButton ||e.getSource()==residualButton || e.getSource()==predictButton ||
				e.getSource()==yButton || e.getSource()==uButton || e.getSource()==vButton) {

			if(frame!=null) {
				jp.repaint();
			}
		}
		
	}
	
	public void updateInfoText(Point p) {
		infoText.setText("Width: "+bi.getWidth()+"\nHeight: "+bi.getHeight());
	
		if(p!=null) {
			int mbx = (((int)(p.x/scale))/16);
			int mby = (((int)(p.y/scale))/16);
			int sbx=(((int)(p.x/scale))/4)%4;
			int sby=(((int)(p.y/scale))/4)%4;
			infoText.setText(infoText.getText()+"\nMacroBlock: "+mbx+", "+mby);
			infoText.setText(infoText.getText()+"\n"+frame.getMacroBlockDebugString(mbx, mby, sbx, sby));
			infoText.setText(infoText.getText()+"\nPixel: "+(int)(p.x/scale)+", "+(int)(p.y/scale));
			
		}
		jp.repaint();
	}
		
	private static void getFrame(ImageInputStream in) throws IOException  {
		byte[] signature = new byte[4];
		try {
			in.readFully(signature);
		} catch (IOException e) {
			throw new IIOException("Error reading RIFF signature", e);
		}
		if (signature[0] != (byte)'R' || 
				signature[1] != (byte)'I' ||
				signature[2] != (byte)'F' ||
				signature[3] != (byte)'F') { // etc.
			throw new IIOException("Bad RIFF signature!");
		}
		int frameSize;
		try {
			frameSize = in.read();
			frameSize +=  in.read()<<8;
			frameSize +=  in.read()<<16;
			frameSize += in.read()<<24;
		} catch (IOException e) {
			throw new IIOException("Error reading frame size 1", e);
		}
		
		try {
			in.readFully(signature);
		} catch (IOException e) {
			throw new IIOException("Error reading WEBP signature", e);
		}
		if (signature[0] != (byte)'W' || 
				signature[1] != (byte)'E' ||
				signature[2] != (byte)'B' ||
				signature[3] != (byte)'P') { // etc.
			throw new IIOException("Bad WEBP signature!");
		}
		
		try {
			in.readFully(signature);
		} catch (IOException e) {
			throw new IIOException("Error reading VP8 signature", e);
		}
		if (signature[0] != (byte)'V' || 
				signature[1] != (byte)'P' ||
				signature[2] != (byte)'8' ) {
			
			throw new IIOException("Bad WEBP signature!");
		}
		
		try {
			frameSize = in.read();
			frameSize +=  in.read()<<8;
			frameSize +=  in.read()<<16;
			frameSize += in.read()<<24;
		} catch (IOException e) {
			throw new IIOException("Error reading frame size 1", e);
		}
	}

}

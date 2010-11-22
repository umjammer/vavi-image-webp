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




import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class plugintest extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BufferedImage bi;
	private JScrollPane sp;
	private JPanel jp;

	plugintest() {

		//File f = new File("random.jpg");
		File f = new File("testdata/test.webp");
		try {
			bi = ImageIO.read(f);
			if(bi == null) {
				System.out.println("null");
				System.exit(0);
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
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
				  g.drawImage(bi, 0, 0, null);
				}

		};
		jp = new ImagePanel(bi);
		jp.setPreferredSize(new Dimension(bi.getWidth(), bi.getHeight()));
		
		sp = new JScrollPane();
		sp.add(jp);
		sp.setViewportView(jp);
		this.add(sp);

		this.setVisible(true);
		this.setSize(1000, 1000);
	}

	public static void main(String[] args) {
		IIORegistry r = javax.imageio.spi.IIORegistry.getDefaultInstance();
		WebPImageReaderSpi s = new WebPImageReaderSpi();
		r.registerServiceProvider(s);
		//System.exit(0);
		for(String n : ImageIO.getReaderFileSuffixes())
		{
			System.out.println(n);
		}
		plugintest app;
		app = new plugintest();
		app.addWindowListener(new WindowAdapter() 
		{
			public void windowClosing( WindowEvent e )
			{
				System.exit( 0 );
			}
		});

		/*for(String n : ImageIO.getReaderFormatNames()) {
			System.out.println(n);
		}*/
		


	}

}

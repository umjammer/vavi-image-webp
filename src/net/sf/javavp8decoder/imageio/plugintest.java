package net.sf.javavp8decoder.imageio;




import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.swing.JFrame;

public class plugintest extends JFrame {
	private BufferedImage bi;
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


		this.setVisible(true);
		this.setSize(1000, 1000);
	}
	public void paint(Graphics g) {
		//System.out.println(bi.getWidth());
		g.drawImage(bi, 100,100, null);
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

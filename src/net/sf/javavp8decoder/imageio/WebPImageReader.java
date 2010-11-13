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


import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import vp8Decoder.VP8Decoder;

public class WebPImageReader extends ImageReader {

	ImageInputStream stream = null;
	int width, height;
	int colorType;
	
	// Constants enumerating the values of colorType
	static final int COLOR_TYPE_GRAY = 0;
	static final int COLOR_TYPE_RGB = 1;

	boolean gotHeader = false;
	private VP8Decoder decoder;
	Logger logger;

	public WebPImageReader(ImageReaderSpi originatingProvider) {
		super(originatingProvider);
		logger = Logger.getAnonymousLogger();
	    //SimpleFormatter formatter = new SimpleFormatter();
	    

		logger.setLevel(Level.ALL);
		logger.log(Level.INFO, "WebPImageReader");


	}

	
	public void setInput(Object input) {
		logger.log(Level.INFO, "setInput");
		super.setInput(input);
		_setInput(input);
	}
	
	public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata)  {
		logger.log(Level.INFO, "setInput");
		super.setInput(input, seekForwardOnly, ignoreMetadata);
		_setInput(input);
	}
	public void setInput(Object input, boolean isStreamable) {
		logger.log(Level.INFO, "setInput");
		super.setInput(input, isStreamable);
		_setInput(input);

	}
	private void _setInput(Object input) {
		logger.log(Level.INFO, "_setInput");
		if (input == null) {
			this.stream = null;
			return;
		}
		if (input instanceof ImageInputStream) {
			this.stream = (ImageInputStream)input;
		} else {
			throw new IllegalArgumentException("bad input");
		}
	}

	public int getNumImages(boolean allowSearch)
		throws IIOException {
		logger.log(Level.INFO, "getNumImages");
		return 1; // format can only encode a single image
	}

	private void checkIndex(int imageIndex) {
		logger.log(Level.INFO, "checkIndex");
		if (imageIndex != 0) {
			throw new IndexOutOfBoundsException("bad index");
		}
	}
	public void readHeader() throws IIOException {
		logger.log(Level.INFO, "readHeader");
		if (gotHeader) {
			return;
		}
		gotHeader = true;

		if (stream == null) {
			throw new IllegalStateException("No input stream");
		}

		// Read `myformat\n' from the stream
		byte[] signature = new byte[4];
		try {
			stream.readFully(signature);
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
			frameSize = stream.read();
			frameSize +=  stream.read()<<8;
			frameSize +=  stream.read()<<16;
			frameSize += stream.read()<<24;
		} catch (IOException e) {
			throw new IIOException("Error reading frame size 1", e);
		}
		
		try {
			stream.readFully(signature);
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
			stream.readFully(signature);
		} catch (IOException e) {
			throw new IIOException("Error reading VP8 signature", e);
		}
		if (signature[0] != (byte)'V' || 
				signature[1] != (byte)'P' ||
				signature[2] != (byte)'8' ) {
			
			throw new IIOException("Bad WEBP signature!");
		}
		
		try {
			frameSize = stream.read();
			frameSize +=  stream.read()<<8;
			frameSize +=  stream.read()<<16;
			frameSize += stream.read()<<24;
		} catch (IOException e) {
			throw new IIOException("Error reading frame size 1", e);
		}
		logger.log(Level.INFO, "VP8 IMAGE DATA SIZE: "+frameSize);
		
		int[] frame = new int[frameSize];
		for(int x=0; x<frameSize; x++)
			try {
				frame[x]=stream.read();
			} catch (IOException e) {
				throw new IIOException("Error reading frame", e);
		}
		decoder = new VP8Decoder();
		decoder.decodeFrame(frame, false);
		this.width = decoder.getWidth();
		this.height = decoder.getHeight();
		
		// Read width, height, color type, newline
		/*try {
			this.width = stream.readInt();
			this.height = stream.readInt();
			this.colorType = stream.readUnsignedByte();
			stream.readUnsignedByte(); // skip newline character
		} catch (IOException e) {
			throw new IIOException("Error reading header", e);
		}*/
	}

	public int getWidth(int imageIndex)
		throws IIOException {
		logger.log(Level.INFO, "getWidth");
		checkIndex(imageIndex); // must throw an exception if != 0
		readHeader();
		return width;
	}

	public int getHeight(int imageIndex)
		throws IIOException {
		logger.log(Level.INFO, "getHeight");
		checkIndex(imageIndex);
		readHeader();
		return height;
	}

	public Iterator getImageTypes(int imageIndex)
	throws IIOException {
		logger.log(Level.INFO, "getImageTypes");
		checkIndex(imageIndex);
		readHeader();
	
		ImageTypeSpecifier imageType = null;
		int datatype = DataBuffer.TYPE_BYTE;
		java.util.List l = new ArrayList();
		//switch (colorType) {
		//case COLOR_TYPE_GRAY:
		//	imageType = ImageTypeSpecifier.createGrayscale(8,
		//	                                               datatype,
		//	                                               false);
		//	break;
	
		//case COLOR_TYPE_RGB:
			ColorSpace rgb =
				ColorSpace.getInstance(ColorSpace.CS_sRGB);
			int[] bandOffsets = new int[3];
			bandOffsets[0] = 0;
			bandOffsets[1] = 1;
			bandOffsets[2] = 2;
			imageType =
				ImageTypeSpecifier.createInterleaved(rgb,
				                                     bandOffsets,
				                                     datatype,
				                                     false,
				                                     false);
		//	break;				
	//}
	l.add(imageType);
	return l.iterator();
}



	public BufferedImage read(int imageIndex, ImageReadParam param)
	throws IIOException {
		super.processImageStarted(0);
		logger.log(Level.INFO, "read");
		readMetadata(); // Stream is positioned at start of image data
		// Compute initial source region, clip against destination later
		Rectangle sourceRegion = getSourceRegion(param, width, height);
			
		// Set everything to default values
		int sourceXSubsampling = 1;
		int sourceYSubsampling = 1;
		int[] sourceBands = null;
		int[] destinationBands = null;
		Point destinationOffset = new Point(0, 0);

		// Get values from the ImageReadParam, if any
		if (param != null) {
			sourceXSubsampling = param.getSourceXSubsampling();
			sourceYSubsampling = param.getSourceYSubsampling();
			sourceBands = param.getSourceBands();
			destinationBands = param.getDestinationBands();
			destinationOffset = param.getDestinationOffset();
		}
		// Get the specified detination image or create a new one
		BufferedImage dst = getDestination(param,
		                                   getImageTypes(0),
		                                   width, height);
		// Enure band settings from param are compatible with images
		int inputBands = 3;//(colorType == COLOR_TYPE_RGB) ? 3 : 1;
		
		checkReadParamBandSettings(param, inputBands,
		                           dst.getSampleModel().getNumBands());

		int[] bandOffsets = new int[inputBands];
		for (int i = 0; i < inputBands; i++) {
			bandOffsets[i] = i;
		}
		int bytesPerRow = width*inputBands;
		DataBufferByte rowDB = new DataBufferByte(bytesPerRow);
		WritableRaster rowRas =
			Raster.createInterleavedRaster(rowDB,
			                               width, 1, bytesPerRow,
			                               inputBands, bandOffsets,
			                               new Point(0, 0));
		byte[] rowBuf = rowDB.getData();

		// Create an int[] that can a single pixel
		int[] pixel = rowRas.getPixel(0, 0, (int[])null);

		WritableRaster imRas = dst.getWritableTile(0, 0);
		int dstMinX = imRas.getMinX();
		int dstMaxX = dstMinX + imRas.getWidth() - 1;
		int dstMinY = imRas.getMinY();
		int dstMaxY = dstMinY + imRas.getHeight() - 1;

		// Create a child raster exposing only the desired source bands
		if (sourceBands != null) {
			rowRas = rowRas.createWritableChild(0, 0,
			                                    width, 1,
			                                    0, 0,
			                                    sourceBands);
		}

		// Create a child raster exposing only the desired dest bands
		//if (destinationBands != null) {
		//	imRas = imRas.createWritableChild(0, 0,
		//	                                  imRas.getWidth(),
		//	                                  imRas.getHeight(),
		//	                                  0, 0,
		//	                                  null);
		//}

		int [][]YBuffer = decoder.getFrame().getYBuffer();
		int [][]UBuffer = decoder.getFrame().getUBuffer();
		int [][]VBuffer = decoder.getFrame().getVBuffer();
		for(int x = 0; x< decoder.getWidth(); x++) {
			for(int y = 0; y< decoder.getHeight(); y++) {
				int c[] = new int[3];
				int yy, u, v;
				yy = YBuffer[x][y];
				u = UBuffer[x/2][y/2];
				v = VBuffer[x/2][y/2];
	
			 	c[0] = (int)( 1.164*(yy-16)+1.596*(v-128) );
			 	c[1] = (int)( 1.164*(yy-16)-0.813*(v-128)-0.391*(u-128) );
			 	c[2] = (int)( 1.164*(yy-16)+2.018*(u-128) );
				for(int z=0; z<3; z++) {
					if(c[z]<0)
						c[z]=0;
					if(c[z]>255)
						c[z]=255;
				}
				imRas.setPixel(x, y, c);
			}
		}
		//for (int srcY = 0; srcY < height; srcY++) {
			
		//}
		super.processImageComplete();
		return dst;
	}
	WebPMetadata metadata = null; // class defined below

	public IIOMetadata getStreamMetadata()
		throws IIOException {
		logger.log(Level.INFO, "getStreamMetadata");
		return null;
	}

	public IIOMetadata getImageMetadata(int imageIndex)
		throws IIOException {
		logger.log(Level.INFO, "getImageMetadata");
		if (imageIndex != 0) {
			throw new IndexOutOfBoundsException("imageIndex != 0!");
		}
		readMetadata();
		return metadata;
	}
	
	public void readMetadata() throws IIOException {
		logger.log(Level.INFO, "readMetadata");
		if (metadata != null) {
			return;
		}
		readHeader();
		this.metadata = new WebPMetadata();
		/*try {
			while (true) {
				String keyword = stream.readUTF();
				stream.readUnsignedByte();
				if (keyword.equals("END")) {
					break;
				}
				String value = stream.readUTF();
				stream.readUnsignedByte();

				metadata.keywords.add(keyword);
				metadata.values.add(value);
			}
		} catch (IOException e) {
			throw new IIOException("Exception reading metadata",
			                       e);
		}*/
	}
}




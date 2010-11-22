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

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import vp8Decoder.VP8Frame;

public class WebPImageReader extends ImageReader implements IIOReadProgressListener {

	ImageInputStream stream = null;
	int width, height;
	int colorType;

	// Constants enumerating the values of colorType
	static final int COLOR_TYPE_GRAY = 0;
	static final int COLOR_TYPE_RGB = 1;

	boolean gotHeader = false;
	private VP8Frame decoder;
	Logger logger;

	public WebPImageReader(ImageReaderSpi originatingProvider) {
		super(originatingProvider);
		logger = Logger.getAnonymousLogger();
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
		
		try {
			decoder = new VP8Frame(stream);
			decoder.addIIOReadProgressListener(this);
			decoder.decodeFrame(false);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex)
	throws IIOException {
		logger.log(Level.INFO, "getImageTypes");
		checkIndex(imageIndex);
		readHeader();
	
		ImageTypeSpecifier imageType = null;
		int datatype = DataBuffer.TYPE_BYTE;
		java.util.List<ImageTypeSpecifier> l = new ArrayList<ImageTypeSpecifier>();

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

	l.add(imageType);
	return l.iterator();
}



	public BufferedImage read(int imageIndex, ImageReadParam param)
	throws IIOException {
		super.processImageStarted(0);
		logger.log(Level.INFO, "read");
		readMetadata(); // Stream is positioned at start of image data
		// Get values from the ImageReadParam, if any
		if (param != null) {
		}
		// Get the specified detination image or create a new one
		BufferedImage dst = getDestination(param,
		                                   getImageTypes(0),
		                                   width, height);
		decoder.useBufferedImage(dst);
		//decoder.getBufferedImage();
		
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
	}
	
	public void imageProgress(ImageReader source, float percentageDone) {
		processImageProgress(percentageDone);
	}

	public void imageComplete(ImageReader source) {}
	public void imageStarted(ImageReader source, int imageIndex) {}
	public void readAborted(ImageReader source) {}
	public void sequenceComplete(ImageReader source) {}
	public void sequenceStarted(ImageReader source, int minIndex) {}
	public void thumbnailComplete(ImageReader source) {}
	public void thumbnailProgress(ImageReader source, float percentageDone) {}
	public void thumbnailStarted(ImageReader source, int imageIndex, int thumbnailIndex) {}
}




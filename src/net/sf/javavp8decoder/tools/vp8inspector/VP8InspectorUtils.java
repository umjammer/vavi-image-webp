package net.sf.javavp8decoder.tools.vp8inspector;

import java.io.File;
import java.io.IOException;

import javax.imageio.IIOException;
import javax.imageio.stream.ImageInputStream;

public class VP8InspectorUtils {
    /*
     * Get the extension of a file.
     */  
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
    
	public static void getWebPFrame(ImageInputStream in) throws IOException {
		byte[] signature = new byte[4];
		try {
			in.readFully(signature);
		} catch (IOException e) {
			throw new IIOException("Error reading RIFF signature", e);
		}
		if (signature[0] != (byte) 'R' || signature[1] != (byte) 'I'
				|| signature[2] != (byte) 'F' || signature[3] != (byte) 'F') { // etc.
			throw new IIOException("Bad RIFF signature!");
		}
		int frameSize;
		try {
			frameSize = in.read();
			frameSize += in.read() << 8;
			frameSize += in.read() << 16;
			frameSize += in.read() << 24;
		} catch (IOException e) {
			throw new IIOException("Error reading frame size 1", e);
		}

		try {
			in.readFully(signature);
		} catch (IOException e) {
			throw new IIOException("Error reading WEBP signature", e);
		}
		if (signature[0] != (byte) 'W' || signature[1] != (byte) 'E'
				|| signature[2] != (byte) 'B' || signature[3] != (byte) 'P') { // etc.
			throw new IIOException("Bad WEBP signature!");
		}

		try {
			in.readFully(signature);
		} catch (IOException e) {
			throw new IIOException("Error reading VP8 signature", e);
		}
		if (signature[0] != (byte) 'V' || signature[1] != (byte) 'P'
				|| signature[2] != (byte) '8') {

			throw new IIOException("Bad WEBP signature!");
		}

		try {
			frameSize = in.read();
			frameSize += in.read() << 8;
			frameSize += in.read() << 16;
			frameSize += in.read() << 24;
		} catch (IOException e) {
			throw new IIOException("Error reading frame size 1", e);
		}
	}
}

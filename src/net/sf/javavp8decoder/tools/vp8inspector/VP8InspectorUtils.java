package net.sf.javavp8decoder.tools.vp8inspector;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.IIOException;
import javax.imageio.stream.ImageInputStream;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.ebml.io.FileDataSource;
import org.ebml.matroska.MatroskaFile;
import org.ebml.matroska.MatroskaFileFrame;
import org.ebml.matroska.MatroskaFileTrack;

public class VP8InspectorUtils {
	
	public static void main(String[] args) {
		VP8Inspector app;
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
		app = new VP8Inspector();
		app.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}
	public static MatroskaFile loadMatroska(File f) {
		FileDataSource iFS;
		try {
			iFS = new FileDataSource(f.getAbsolutePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			return null;
		}
		MatroskaFile mF = new MatroskaFile(iFS);
		mF.setScanFirstCluster(true);
		mF.readFile();
		System.out.println(mF.getReport());
		MatroskaFileTrack[] tl = mF.getTrackList();
		MatroskaFileTrack track=null;
		for(MatroskaFileTrack t : mF.getTrackList() ) {
			if(t.CodecID.compareTo("V_VP8")==0)
				track = t;
		}
		
		return mF;		
	}
	public static int countKeyFrames(MatroskaFile mF) {
		MatroskaFileTrack[] tl = mF.getTrackList();
		MatroskaFileTrack track=null;
		for(MatroskaFileTrack t : mF.getTrackList() ) {
			if(t.CodecID.compareTo("V_VP8")==0)
				track = t;
		}
		if (track!=null)
		{
			int count=0;
			MatroskaFileFrame frame=mF.getNextFrame(track.TrackNo);
			while(frame!=null) {
				if(frame.isKeyFrame())
					count++;
				frame=mF.getNextFrame(track.TrackNo);
			}
			return count;
		}
		return 0;
	}
	
	public static byte[] getMatroskaFrame(MatroskaFile mF) {
		MatroskaFileTrack[] tl = mF.getTrackList();
		MatroskaFileTrack track=null;
		for(MatroskaFileTrack t : mF.getTrackList() ) {
			if(t.CodecID.compareTo("V_VP8")==0)
				track = t;
		}
		if (track!=null)
		{
			MatroskaFileFrame frame=mF.getNextFrame(track.TrackNo);
			while(!frame.isKeyFrame()) {
				frame=mF.getNextFrame(track.TrackNo);
			}
			return frame.Data;
		}
		return null;
	}
	
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

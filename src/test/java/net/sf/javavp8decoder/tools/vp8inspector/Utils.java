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


public class Utils {

    public static int countKeyFrames(MatroskaFile mF) {
        MatroskaFileTrack track = null;
        for (MatroskaFileTrack t : mF.getTrackList()) {
            if (t.getCodecID().compareTo("V_VP8") == 0)
                track = t;
        }
        if (track != null) {
            int count = 0;
            MatroskaFileFrame frame = mF.getNextFrame(track.getTrackNo());
            while (frame != null) {
                if (frame.isKeyFrame())
                    count++;
                frame = mF.getNextFrame(track.getTrackNo());
            }
            return count;
        }
        return 0;
    }

    /* Get the extension of a file. */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public static byte[] getMatroskaFrame(MatroskaFile mF) {
        MatroskaFileTrack track = null;
        for (MatroskaFileTrack t : mF.getTrackList()) {
            if (t.getCodecID().compareTo("V_VP8") == 0)
                track = t;
        }
        if (track != null) {
            MatroskaFileFrame frame = mF.getNextFrame(track.getTrackNo());
            while (!frame.isKeyFrame()) {
                frame = mF.getNextFrame(track.getTrackNo());
            }
            return frame.getData().array();
        }
        return null;
    }

    public static void getWebPFrame(ImageInputStream in) throws IOException {
        byte[] signature = new byte[4];
        try {
            in.readFully(signature);
        } catch (IOException e) {
            throw new IIOException("Error reading RIFF signature", e);
        }
        if (signature[0] != (byte) 'R' || signature[1] != (byte) 'I' || signature[2] != (byte) 'F'
            || signature[3] != (byte) 'F') { // etc.
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
        if (signature[0] != (byte) 'W' || signature[1] != (byte) 'E' || signature[2] != (byte) 'B'
            || signature[3] != (byte) 'P') { // etc.
            throw new IIOException("Bad WEBP signature!");
        }

        try {
            in.readFully(signature);
        } catch (IOException e) {
            throw new IIOException("Error reading VP8 signature", e);
        }
        if (signature[0] != (byte) 'V' || signature[1] != (byte) 'P' || signature[2] != (byte) '8') {

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
        for (MatroskaFileTrack t : mF.getTrackList()) {
            if (t.getCodecID().compareTo("V_VP8") == 0) {
            }
        }

        return mF;
    }

    public static void main(String[] args) {
        VP8Inspector app;
        try {
            // Set System L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException |
                 ClassNotFoundException e) {
            // handle exception
        }
        app = new VP8Inspector();
        app.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
}

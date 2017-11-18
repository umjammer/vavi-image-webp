
package net.sf.javavp8decoder.tools.vp8inspector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class IVFFile {
    private static int IVF_FRAME_HDR_SZ = (4 + 8);

    private static int IVF_FILE_HDR_SZ = (32);

    File file;

    FileInputStream in;

    public IVFFile(File file) {
        this.file = file;

        try {
            in = new FileInputStream(file);
            readFileHeader(in);
        }

        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean readFileHeader(FileInputStream in) throws IOException {
        for (int x = 0; x < IVF_FILE_HDR_SZ; x++) {
            in.read();
        }
        return true;
    }

    private byte[] getFrame() throws IOException {
        byte[] frame;

        int frameSize = in.read();
        frameSize += in.read() << 8;
        frameSize += in.read() << 16;
        frameSize += in.read() << 24;

        for (int x = 0; x < IVF_FRAME_HDR_SZ - 4; x++) {
            in.read();
        }

        frame = new byte[frameSize];
        for (int x = 0; x < frameSize; x++)
            frame[x] = (byte) in.read();
        return frame;
    }

    public int getKeyFrames() {
        int c = 0;

        try {
            while (in.available() > 5) {
                byte[] data = getFrame();
                int frameType = getBitAsInt(data[0], 0);
                if (frameType == 0)
                    c++;
            }
            in = new FileInputStream(file);
            readFileHeader(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return c;
    }

    public byte[] getNextFrame() {

        byte[] frameData = null;
        int frameType = 1;
        try {
            while (frameType != 0) {
                frameData = getFrame();
                frameType = getBitAsInt(frameData[0], 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return frameData;
    }

    private int getBitAsInt(int data, int bit) {
        int r = data & (1 << bit);
        if (r > 0)
            return 1;
        return 0;
    }
}

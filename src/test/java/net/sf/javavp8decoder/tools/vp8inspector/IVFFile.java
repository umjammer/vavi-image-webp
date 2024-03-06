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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class IVFFile {
    private static final int IVF_FRAME_HDR_SZ = (4 + 8);

    private static final int IVF_FILE_HDR_SZ = (32);

    final File file;

    FileInputStream in;

    public IVFFile(File file) {
        this.file = file;

        try {
            in = new FileInputStream(file);
            readFileHeader(in);
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean readFileHeader(FileInputStream in) throws IOException {
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

    private static int getBitAsInt(int data, int bit) {
        int r = data & (1 << bit);
        if (r > 0)
            return 1;
        return 0;
    }
}

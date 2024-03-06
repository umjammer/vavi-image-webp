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

import java.io.IOException;
import java.io.InputStream;

import net.sf.javavp8decoder.vp8Decoder.VP8Decoder;


public class IVF {

    private static final int IVF_FRAME_HDR_SZ = 4 + 8;
    private static final int IVF_FILE_HDR_SZ = 32;

    IVF() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("bbb.ivf")) {
            System.out.println("IVF");
            readFileHeader(in);

            @SuppressWarnings("unused")
            VP8Decoder f = new VP8Decoder();

            @SuppressWarnings("unused")
            int x = 0;
            while (true) {
                @SuppressWarnings("unused")
                int[] frameData = getFrame(in);

                //f.decodeFrame(frameData, false);
                x++;
            }
        }
    }

    private static int[] getFrame(InputStream in) throws IOException {
        int[] frame;
        int c;
        int frameSize = c = in.read();
        frameSize += c = in.read() << 8;
        frameSize += c = in.read() << 16;
        frameSize += c = in.read() << 24;

        System.out.print("IVF FRAME HEADER: ");
        for (int x = 0; x < IVF_FRAME_HDR_SZ - 4; x++) {
            c = in.read();
            System.out.print(toHex(c));
        }
        System.out.println();
        System.out.println("frameSize: " + frameSize);
        frame = new int[frameSize];
        for (int x = 0; x < frameSize; x++)
            frame[x] = in.read();
        return frame;
    }

    public static void main(String[] args) throws IOException {
        new IVF();
    }

    public static String toHex(int c) {
        return String.format("%1$#x ", c);
    }

    private static boolean readFileHeader(InputStream in) throws IOException {
        int c;
        System.out.print("IVF FILE HEADER: ");
        for (int x = 0; x < IVF_FILE_HDR_SZ; x++) {
            c = in.read();
            System.out.print(toHex(c));
        }
        System.out.println();
        return true;
    }
}

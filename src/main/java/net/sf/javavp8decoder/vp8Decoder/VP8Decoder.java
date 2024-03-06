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

package net.sf.javavp8decoder.vp8Decoder;

import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.stream.ImageInputStream;


public class VP8Decoder {
    private int[][][][] coefProbs;

    VP8Frame f;

    @SuppressWarnings("unused")
    private int frameCount = 0;

    public VP8Decoder() {
        coefProbs = Globals.getDefaultCoefProbs();
    }

    public void decodeFrame(ImageInputStream stream, boolean debug) throws IOException {
        coefProbs = Globals.getDefaultCoefProbs();
        f = new VP8Frame(stream, coefProbs);
        if (f.decodeFrame(debug)) {

        }
        frameCount++;
    }

    public VP8Frame getFrame() {
        return f;
    }

    public int getHeight() {
        return f.getHeight();
    }

    public int getWidth() {
        return f.getWidth();
    }

    public void writeFile(int[][] data) throws IOException {
        try (FileOutputStream out = new FileOutputStream("outagain.raw")) {
            for (int y = 0; y < data[0].length; y++)
                for (int[] datum : data) {
                    out.write(datum[y]);
                    out.write(datum[y]);
                    out.write(datum[y]);
                }
        }
    }

    public void writePGMFile(String fileName, VP8Frame frame) throws IOException {
        int[][] yData = frame.getYBuffer();
        int[][] uData = frame.getUBuffer();
        int[][] vData = frame.getVBuffer();
        int outStride = (f.getWidth() + 1) & ~1;
        int uvHeight = (f.getHeight() + 1) / 2;
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            out.write((byte) 'P');
            out.write((byte) '5');
            out.write((byte) 0x0a);
            out.write(("" + outStride).getBytes());
            out.write((byte) ' ');

            out.write(("" + (f.getHeight() + uvHeight)).getBytes());

            out.write((byte) 0x0a);
            out.write(("255").getBytes());
            out.write((byte) 0xa);
            for (int y = 0; y < f.getHeight(); y++) {
                for (int x = 0; x < f.getWidth(); x++) {
                    out.write(yData[x][y]);
                }
                if ((f.getWidth() & 1) == 1)
                    out.write(0x0);
            }
            for (int y = 0; y < (f.getHeight() + 1) / 2; y++) {
                for (int x = 0; x < (f.getWidth() + 1) / 2; x++) {
                    out.write(uData[x][y]);
                }
                for (int x = 0; x < (f.getWidth() + 1) / 2; x++) {
                    out.write(vData[x][y]);
                }
            }
        }
    }

    public void writeYV12File(String fileName, VP8Frame frame) throws IOException {
        int[][] yData = frame.getYBuffer();
        int[][] uData = frame.getUBuffer();
        int[][] vData = frame.getVBuffer();
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            out.write((byte) 'P');
            out.write((byte) '5');
            out.write((byte) 0x0a);
            out.write(("" + f.getWidth()).getBytes());
            out.write((byte) ' ');

            out.write(("" + (f.getHeight() * 3 / 2)).getBytes());
            out.write((byte) 0x0a);
            out.write(("255").getBytes());
            out.write((byte) 0xa);
            for (int y = 0; y < f.getHeight(); y++) {
                for (int x = 0; x < f.getWidth(); x++) {
                    out.write(yData[x][y]);
                }

            }
            for (int y = 0; y < (f.getHeight() + 1) / 2; y++)
                for (int x = 0; x < (f.getWidth() + 1) / 2; x++) {
                    out.write(uData[x][y]);
                }
            for (int y = 0; y < (f.getHeight() + 1) / 2; y++)
                for (int x = 0; x < (f.getWidth() + 1) / 2; x++) {
                    out.write(vData[x][y]);
                }
        }
    }
}

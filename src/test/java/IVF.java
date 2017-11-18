import java.io.IOException;
import java.io.InputStream;

import net.sf.javavp8decoder.vp8Decoder.VP8Decoder;


public class IVF {

    private static int IVF_FRAME_HDR_SZ = 4 + 8;
    private static int IVF_FILE_HDR_SZ = 32;

    IVF() {
        InputStream in = null;
        try {
            in = getClass().getResourceAsStream("bbb.ivf");
            System.out.println("IVF");
            readFileHeader(in);
            @SuppressWarnings("unused")
            int[] frameData;

            @SuppressWarnings("unused")
            VP8Decoder f = new VP8Decoder();

            @SuppressWarnings("unused")
            int x = 0;
            while (true) {
                frameData = getFrame(in);

                //f.decodeFrame(frameData, false);
                x++;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private int[] getFrame(InputStream in) throws IOException {
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

    public static void main(String[] args) {
        new IVF();
    }

    public String toHex(int c) {
        String r = new String();
        r = String.format("%1$#x ", c);
        return r;
    }

    private boolean readFileHeader(InputStream in) throws IOException {
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

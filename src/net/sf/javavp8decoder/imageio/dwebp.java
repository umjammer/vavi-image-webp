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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import vp8Decoder.VP8Decoder;



public class dwebp {
	private static int IVF_FRAME_HDR_SZ = (4+8);
	private static int IVF_FILE_HDR_SZ = (32);


	private static int[] getFrame(FileInputStream in) throws IOException  {
		int[] frame;
		int c;
		System.out.print(""+toHex(in.read()));
		System.out.print(""+toHex(in.read()));
		System.out.print(""+toHex(in.read()));
		System.out.print(""+toHex(in.read()));
		int frameSize = c = in.read();
		frameSize += c = in.read()<<8;
		frameSize += c = in.read()<<16;
		frameSize += c = in.read()<<24;
		System.out.print("RIFF IMAGE DATA SIZE: "+frameSize);

		frame = new int[frameSize];
		for(int x=0; x<frameSize; x++)
			frame[x]=in.read();
		return frame;
	}
	public static void main(String[] args) {
		FileInputStream in = null;
		try {
			String inname="testdata/small_31x13.webp";
			String outname="0.raw";
			if(args.length>0)
				outname = args[0];

			
			if(args.length>1)
				inname = args[1];
			
			in = new FileInputStream(inname);
			
			System.out.println("RIFF");
			readFileHeader(in);
			int[] frameData;
			
			VP8Decoder f = new VP8Decoder();

			frameData = getFrame(in);
			f.decodeFrame(frameData, false);
			f.writeYV12File(outname, f.getFrame());
	
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String toHex(int c) {
		String r = new String();
		r = String.format("%1$#x ", c);
		return r;
	}
	private static boolean readFileHeader(FileInputStream in) throws IOException {
		int c;
		System.out.print(""+toHex(in.read()));
		System.out.print(""+toHex(in.read()));
		System.out.print(""+toHex(in.read()));
		System.out.print(""+toHex(in.read()));
		System.out.println();
		int frameSize = c = in.read();
		frameSize += c = in.read()<<8;
		frameSize += c = in.read()<<16;
		frameSize += c = in.read()<<24;
		System.out.print("RIFF IMAGE DATA SIZE: "+frameSize);
		System.out.println();
		System.out.print(""+toHex(in.read()));
		System.out.print(""+toHex(in.read()));
		System.out.print(""+toHex(in.read()));
		System.out.print(""+toHex(in.read()));
		System.out.println();
		return true;
	}


}

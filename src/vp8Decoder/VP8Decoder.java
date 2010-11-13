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
package vp8Decoder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class VP8Decoder {
	private int[][][][] coefProbs;
	private int frameCount=0;
	VP8Frame f;
	public VP8Decoder() {
		coefProbs = Globals.get_default_coef_probs();
	}
	public void decodeFrame(int[] frameData, boolean debug) {
		coefProbs = Globals.get_default_coef_probs();
		f = new VP8Frame(frameData, coefProbs);
		if(f.decodeFrame(debug)) {
			//writeYV12File(""+frameCount+".raw", f.getYBuffer(), f.getUBuffer(), f.getVBuffer());
			//f.loopFilter();
			//writeYV12File("lf"+frameCount+".raw", f.getYBuffer(), f.getUBuffer(), f.getVBuffer());
		}
		frameCount++;
		//System.out.println(frameCount);
			
		
	}
	private void writeYV12File(String fileName, int[][] yData, int uData[][], int vData[][]) {
		
		FileOutputStream out;
		try {
			out = new FileOutputStream(fileName);
			for(int y=0;y<yData[0].length; y++)
				for(int x=0;x<yData.length; x++) {

					out.write(yData[x][y]);
				}
			for(int y=0;y<vData[0].length; y++)
				for(int x=0;x<vData.length; x++) {

					out.write(uData[x][y]);
				}
			for(int y=0;y<uData[0].length; y++)
				for(int x=0;x<uData.length; x++) {

					out.write(vData[x][y]);
				}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@SuppressWarnings("unused")
	private void writeFile(int[][] data) {
		FileOutputStream out;
		try {
			out = new FileOutputStream("outagain.raw");
			for(int y=0;y<data[0].length; y++)
				for(int x=0;x<data.length; x++) {
					out.write(data[x][y]);
					out.write(data[x][y]);
					out.write(data[x][y]);
				}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public int getWidth() {
		return f.getWidth();
	}
	public int getHeight() {
		return f.getHeight();
	}
	public VP8Frame getFrame() {
		return f;
	}
}

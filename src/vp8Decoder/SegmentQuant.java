package vp8Decoder;

public class SegmentQuant {
	private int Qindex;
	private int y1dc;
	private int y1ac;
	private int y2dc;
	private int y2ac;
	private int uvdc;
	private int uvac;
	private int filterStrength;
	public int getQindex() {
		return Qindex;
	}
	public void setQindex(int qindex) {
		Qindex = qindex;
	}
	
	public int getY1dc_delta_q() {
		return y1dc;
	}
	public void setY1dc_delta_q(int y1dc_delta_q) {
		this.y1dc = Globals.dc_qlookup[clip(Qindex+y1dc_delta_q, 127)];
		this.setY1ac_delta_q();
	}
	public int getY1ac_delta_q() {
		return y1ac;
	}
	public void setY1ac_delta_q() {
		this.y1ac = Globals.ac_qlookup[clip(Qindex, 127)];
	}
	public int getY2dc_delta_q() {
		return y2dc;
	}
	public void setY2dc_delta_q(int y2dc_delta_q) {
		this.y2dc = Globals.dc_qlookup[clip(Qindex+y2dc_delta_q, 127)]*2;
	}


	public int getY2ac_delta_q() {
		return y2ac;
	}
	public void setY2ac_delta_q(int y2ac_delta_q) {
		this.y2ac = Globals.ac_qlookup[clip(Qindex+y2ac_delta_q, 127)]* 155 / 100;
		if (this.y2ac < 8) this.y2ac = 8;
	}
	public int getUvdc_delta_q() {
		return uvdc;
	}
	public void setUvdc_delta_q(int uvdc_delta_q) {
		this.uvdc = Globals.dc_qlookup[clip(Qindex+uvdc_delta_q, 127)];
	}
	public int getUvac_delta_q() {
		return uvac;
	}
	public void setUvac_delta_q(int uvac_delta_q) {
		this.uvac = Globals.ac_qlookup[clip(Qindex+uvac_delta_q, 127)];
	}
	
	private int clip(int val, int max) {
		int r = val;
		if(val>max)
			r=max;
		if(r<0)
			r=0;
		return r;
	}
	public void setFilterStrength(int value) {
		this.filterStrength=value;
	}
}

package vp8Decoder;

import java.util.logging.Level;

public class SegmentQuants {

	private int qIndex;
	
	private SegmentQuant[] segQuants = new SegmentQuant[Globals.MAX_MB_SEGMENTS];
	
	public SegmentQuants() {
		for(int x=0; x<Globals.MAX_MB_SEGMENTS; x++)
			segQuants[x]=new SegmentQuant();
	}

	public void parse(BoolDecoder bc) {
		qIndex = bc.read_literal(7);
		//logger.log(Level.INFO, "Q: " + Qindex);

		boolean q_update = false;
		DeltaQ v = get_delta_q(bc, 0);
		int y1dc_delta_q = v.v;
		q_update = q_update || v.update;
		v = get_delta_q(bc, 0);
		int y2dc_delta_q = v.v;
		q_update = q_update || v.update;
		v = get_delta_q(bc, 0);
		int y2ac_delta_q = v.v;
		q_update = q_update || v.update;
		v = get_delta_q(bc, 0);
		int uvdc_delta_q = v.v;
		q_update = q_update || v.update;
		v = get_delta_q(bc, 0);
		int uvac_delta_q = v.v;
		q_update = q_update || v.update;

		for(SegmentQuant s : segQuants) {
			s.setQindex(qIndex);
			s.setY1dc_delta_q(y1dc_delta_q);
			s.setY2dc_delta_q(y2dc_delta_q);
			s.setY2ac_delta_q(y2ac_delta_q);
			s.setUvdc_delta_q(uvdc_delta_q);
			s.setUvac_delta_q(uvac_delta_q);
		}
	}
	
	/*private String toString() {
		for(SegmentQuant s : segQuants) {
			
		}
	}*/
	
	private static DeltaQ get_delta_q(BoolDecoder bc, int prev) {
		DeltaQ ret = new DeltaQ();
		ret.v = 0;
		ret.update = false;

		if (bc.read_bit() > 0) {
			ret.v = bc.read_literal(4);

			if (bc.read_bit() > 0)
				ret.v = -ret.v;
		}

		/* Trigger a quantizer update if the delta-q value has changed */
		if (ret.v != prev)
			ret.update = true;

		return ret;
	}

	public int getqIndex() {
		return qIndex;
	}

	public SegmentQuant[] getSegQuants() {
		return segQuants;
	}

	public void setSegQuants(SegmentQuant[] segQuants) {
		this.segQuants = segQuants;
	}
}

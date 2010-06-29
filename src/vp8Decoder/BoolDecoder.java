package vp8Decoder;

public class BoolDecoder {
	private int offset;            /* pointer to next compressed data byte */
    private int range;                 /* always identical to encoder's range */
    private int value;                 /* contains at least 24 significant bits */
    int        bit_count;          /* # of bits shifted out of value, at most 7 */
    int[] data;
	public static void main(String[] args) {
		int[] data = new int[3];
		data[0]=112;
		data[1]=0;
		data[2]=0;
		BoolDecoder d = new BoolDecoder(data, 0);
		d.read_bool(128);
		d.read_bool(128);
		d.read_bool(128);
		d.read_bool(128);
		d.read_bool(128);
		d.read_bool(128);
		d.read_bool(128);
		d.read_bool(128);
	}
	
	BoolDecoder(int[] frame, int offset) {
		this.data=frame;
		this.offset=offset;
		init_bool_decoder();
	}
	public String toString() {
		return "bc: "+value;
	}
	private void init_bool_decoder() {

		value = 0;                    /* value = first 16 input bits */


		
		value = (data[offset]) << 8;
	    offset++;

		range = 255;                       /* initial range is full */
		bit_count = 0;                      /* have not yet shifted out any bits */
	}
	
	public int read_bool(int probability) {

		    int bit = 0;
		    int split;
		    int bigsplit;
		    int range = this.range;
		    int value = this.value;
		    split = 1 + (((range - 1) * probability) >> 8);
		    bigsplit = (split << 8);
		    range = split;

		    if (value >= bigsplit)
		    {
		        range = this.range - split;
		        value = value - bigsplit;
		        bit = 1;
		    }

		    {
		        int count = this.bit_count;
		        int shift = Globals.vp8dx_bitreader_norm[range];
		        range <<= shift;
		        value <<= shift;
		        count -= shift;

		        if (count <= 0)
		        {
		            value |= data[offset] << (-count);
		            offset++;
		            count += 8 ;
		        }

		        this.bit_count = count;
		    }
		    this.value = value;
		    this.range = range;
		    return bit;
		}
	
//	public int old_read_bool(int prob) {
//	    int split = 1 + ( ((range - 1) * prob) >> 8);
//	    int bigsplit = split << 8;
//
//	    int        retval=0;                    /* will be 0 or 1 */
//	    if( value >= bigsplit) {              /* encoded a one */
//
//	    	retval = 1;
//	    	range -= split;                 /* reduce range */
//	    	value -= bigsplit;                 /* subtract off left endpoint of interval */
//	     } else {                              /* encoded a zero */
//
//	    	 retval = 0;
//	    	 range = split;                  /* reduce range, no change in left endpoint */
//	     }
//	     while( range < 128) {              /* shift out irrelevant value bits */
//	    	 value <<= 1;
//	    	 range <<= 1;
//	    	 if( ++bit_count == 8) { /* shift in new bits 8 at a time */
//	    		 bit_count = 0;
//	    		 value |= data[offset++];
//	        }
//	     }
//	    return retval;
//	}
	
	  /* Convenience function reads a "literal", that is, a "num_bits" wide
    unsigned value whose bits come high- to low-order, with each bit
    encoded at probability 128 (i.e., 1/2). */
	public int read_literal( int num_bits)
	{
		int v = 0;
		while( num_bits-->0)
			v = (v << 1) + read_bool(128);
		return v;
	}

	public int read_bit() {
		return read_bool(128);
	}
	
	int treed_read(
			int t[],		/* tree specification */
			int p[]		/* corresponding interior node probabilities */
			      ) {
		int i = 0; /* begin at root */
		
		/* Descend tree until leaf is reached */
		while( ( i = t[ i + read_bool(p[i>>1]) ] ) > 0) {}
		return -i;      /* return value is negation of nonpositive index */

	}

	int treed_read_skip(
			int t[],		/* tree specification */
			int p[],		/* corresponding interior node probabilities */
			int skip_branches) {
		int i = skip_branches*2; /* begin at root */
		
		/* Descend tree until leaf is reached */
		while( ( i = t[ i + read_bool(p[i>>1]) ] ) > 0) {}
		return -i;      /* return value is negation of nonpositive index */

	}

}

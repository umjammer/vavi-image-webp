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

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.stream.ImageInputStream;

import static java.lang.System.getLogger;


public class VP8Frame {

    private static final Logger logger = getLogger(VP8Frame.class.getName());

    private static final int BLOCK_TYPES = 4;

    private static final int COEF_BANDS = 8;

    private static final int MAX_ENTROPY_TOKENS = 12;

    private static final int MAX_MODE_LF_DELTAS = 4;

    private static final int MAX_REF_LF_DELTAS = 4;

    private static final int PREV_COEF_CONTEXTS = 3;

    private final ArrayList<IIOReadProgressListener> _listeners = new ArrayList<>();

    private int bufferCount;

    private int buffersToCreate = 1;

    private int[][][][] coefProbs;

    private boolean debug = false;

    private int filterLevel;

    private int filterType;

    private ImageInputStream frame;

    private int frameType;

    private int height;

    private int macroBlockCols;

    private int macroBlockNoCoeffSkip;

    private int macroBlockRows;

    private MacroBlock[][] macroBlocks;

    private int macroBlockSegementAbsoluteDelta;

    private int[] macroBlockSegmentTreeProbs;

    private final int[] modeLoopFilterDeltas = new int[MAX_MODE_LF_DELTAS];

    private int modeRefLoopFilterDeltaEnabled;

    private int modeRefLoopFilterDeltaUpdate;

    private int multiTokenPartition = 0;

    private long offset;

    private final int[] refLoopFilterDeltas = new int[MAX_REF_LF_DELTAS];

    private int refreshEntropyProbs;

    private int refreshLastFrame;

    private int segmentationIsEnabled;

    private SegmentQuants segmentQuants;

    private int sharpnessLevel;

    private int simpleFilter;

    private BoolDecoder tokenBoolDecoder;

    private List<BoolDecoder> tokenBoolDecoders;

    private int updateMacroBlockSegmentationMap;

    private int updateMacroBlockSegmentatonData;

    private int width;

    public VP8Frame(ImageInputStream stream) throws IOException {
        this.frame = stream;
        offset = frame.getStreamPosition();
        this.coefProbs = Globals.getDefaultCoefProbs();
        tokenBoolDecoders = new ArrayList<>();
    }

    public VP8Frame(ImageInputStream stream, int[][][][] coefProbs) throws IOException {
        this.frame = stream;
        offset = frame.getStreamPosition();
        this.coefProbs = coefProbs;
        tokenBoolDecoders = new Vector<>();
    }

    public void addIIOReadProgressListener(IIOReadProgressListener listener) {
        _listeners.add(listener);
    }

    private void createMacroBlocks() {
        macroBlocks = new MacroBlock[macroBlockCols + 2][macroBlockRows + 2];
        for (int x = 0; x < macroBlockCols + 2; x++) {
            for (int y = 0; y < macroBlockRows + 2; y++) {
                macroBlocks[x][y] = new MacroBlock(x, y, debug);
            }
        }
    }

    public boolean decodeFrame(boolean debug) throws IOException {

        this.debug = debug;
        segmentQuants = new SegmentQuants();
        int c;
        frame.seek(offset++);
        c = frame.readUnsignedByte();
        logger.log(Level.DEBUG, "frame.length: " + frame.length());
        frameType = getBitAsInt(c, 0);
        logger.log(Level.DEBUG, "Frame type: " + frameType);
        if (frameType != 0)
            return false;
        int versionNumber = getBitAsInt(c, 1) << 1;

        versionNumber += getBitAsInt(c, 2) << 1;
        versionNumber += getBitAsInt(c, 3);
        logger.log(Level.DEBUG, "Version Number: " + versionNumber);
        logger.log(Level.DEBUG, "show_frame: " + getBit(c, 4));

        int firstPartitionLengthInBytes;
        firstPartitionLengthInBytes = getBitAsInt(c, 5) << 0;
        firstPartitionLengthInBytes += getBitAsInt(c, 6) << 1;
        firstPartitionLengthInBytes += getBitAsInt(c, 7) << 2;
        frame.seek(offset++);
        c = frame.readUnsignedByte();
        firstPartitionLengthInBytes += c << 3;
        frame.seek(offset++);
        c = frame.readUnsignedByte();
        firstPartitionLengthInBytes += c << 11;
        logger.log(Level.DEBUG, "first_partition_length_in_bytes: " + firstPartitionLengthInBytes);

        frame.seek(offset++);
        c = frame.readUnsignedByte();
        logger.log(Level.DEBUG, "StartCode: " + c);
        frame.seek(offset++);
        c = frame.readUnsignedByte();
        logger.log(Level.DEBUG, " " + c);
        frame.seek(offset++);
        c = frame.readUnsignedByte();
        logger.log(Level.DEBUG, " " + c);

        frame.seek(offset++);
        c = frame.readUnsignedByte();
        int hBytes = c;
        frame.seek(offset++);
        c = frame.readUnsignedByte();
        hBytes += c << 8;
        width = (hBytes & 0x3fff);
        logger.log(Level.DEBUG, "width: " + width);
        logger.log(Level.DEBUG, "hScale: " + (hBytes >> 14));

        frame.seek(offset++);
        c = frame.readUnsignedByte();
        int vBytes = c;
        frame.seek(offset++);
        c = frame.readUnsignedByte();
        vBytes += c << 8;
        height = (vBytes & 0x3fff);
        logger.log(Level.DEBUG, "height: " + height);
        logger.log(Level.DEBUG, "vScale: " + (vBytes >> 14));
        int tWidth = width;
        int tHeight = height;
        if ((tWidth & 0xf) != 0)
            tWidth += 16 - (tWidth & 0xf);

        if ((tHeight & 0xf) != 0)
            tHeight += 16 - (tHeight & 0xf);
        macroBlockRows = tHeight >> 4;
        macroBlockCols = tWidth >> 4;
        logger.log(Level.DEBUG, "macroBlockCols: " + macroBlockCols);
        logger.log(Level.DEBUG, "macroBlockRows: " + macroBlockRows);

        createMacroBlocks();

        BoolDecoder bc = new BoolDecoder(frame, offset);

        if (frameType == 0) {
            int clr_type = bc.readBit();
            logger.log(Level.DEBUG, "clr_type: " + clr_type);
            logger.log(Level.DEBUG, "" + bc);

            int clamp_type = bc.readBit();
            logger.log(Level.DEBUG, "clamp_type: " + clamp_type);
        }
        segmentationIsEnabled = bc.readBit();
        logger.log(Level.DEBUG, "segmentation_enabled: " + segmentationIsEnabled);
        if (segmentationIsEnabled > 0) {
            logger.log(Level.DEBUG, "TODO: ");
            updateMacroBlockSegmentationMap = bc.readBit();
            updateMacroBlockSegmentatonData = bc.readBit();
            logger.log(Level.DEBUG, "update_mb_segmentaton_map: " + updateMacroBlockSegmentationMap);
            logger.log(Level.DEBUG, "update_mb_segmentaton_data: " + updateMacroBlockSegmentatonData);
            if (updateMacroBlockSegmentatonData > 0) {

                macroBlockSegementAbsoluteDelta = bc.readBit();
                // For each segmentation feature (Quant and loop filter level)
                for (int i = 0; i < Globals.MAX_MB_SEGMENTS; i++) {
                    int value = 0;
                    if (bc.readBit() > 0) {
                        value = bc.readLiteral(Globals.vp8MacroBlockFeatureDataBits[0]);
                        if (bc.readBit() > 0)
                            value = -value;
                    }
                    this.segmentQuants.getSegQuants()[i].setQindex(value);
                }
                for (int i = 0; i < Globals.MAX_MB_SEGMENTS; i++) {
                    int value = 0;
                    if (bc.readBit() > 0) {
                        value = bc.readLiteral(Globals.vp8MacroBlockFeatureDataBits[1]);
                        if (bc.readBit() > 0)
                            value = -value;
                    }
                    this.segmentQuants.getSegQuants()[i].setFilterStrength(value);
                }

                if (updateMacroBlockSegmentationMap > 0) {
                    macroBlockSegmentTreeProbs = new int[Globals.MB_FEATURE_TREE_PROBS];
                    for (int i = 0; i < Globals.MB_FEATURE_TREE_PROBS; i++) {
                        int value = 255;
                        if (bc.readBit() > 0) {
                            value = bc.readLiteral(8);
                        } else
                            value = 255;
                        macroBlockSegmentTreeProbs[i] = value;
                    }
                }
            }
        }
        simpleFilter = bc.readBit();
        logger.log(Level.DEBUG, "simpleFilter: " + simpleFilter);
        filterLevel = bc.readLiteral(6);

        logger.log(Level.DEBUG, "filter_level: " + filterLevel);
        sharpnessLevel = bc.readLiteral(3);
        logger.log(Level.DEBUG, "sharpness_level: " + sharpnessLevel);
        modeRefLoopFilterDeltaEnabled = bc.readBit();
        logger.log(Level.DEBUG, "mode_ref_lf_delta_enabled: " + modeRefLoopFilterDeltaEnabled);
        if (modeRefLoopFilterDeltaEnabled > 0) {
            // Do the deltas need to be updated
            modeRefLoopFilterDeltaUpdate = bc.readBit();
            logger.log(Level.DEBUG, "mode_ref_lf_delta_update: " + modeRefLoopFilterDeltaUpdate);
            if (modeRefLoopFilterDeltaUpdate > 0) {
                for (int i = 0; i < MAX_REF_LF_DELTAS; i++) {

                    if (bc.readBit() > 0) {
                        refLoopFilterDeltas[i] = bc.readLiteral(6);
                        if (bc.readBit() > 0) // Apply sign
                            refLoopFilterDeltas[i] = refLoopFilterDeltas[i] * -1;
                        logger.log(Level.DEBUG, "ref_lf_deltas[i]: " + refLoopFilterDeltas[i]);
                    }
                }
                for (int i = 0; i < MAX_MODE_LF_DELTAS; i++) {

                    if (bc.readBit() > 0) {
                        modeLoopFilterDeltas[i] = bc.readLiteral(6);
                        if (bc.readBit() > 0) // Apply sign
                            modeLoopFilterDeltas[i] = modeLoopFilterDeltas[i] * -1;
                        logger.log(Level.DEBUG, "mode_lf_deltas[i]: " + modeLoopFilterDeltas[i]);
                    }
                }
            }
        }

        filterType = (filterLevel == 0) ? 0 : (simpleFilter > 0) ? 1 : 2;
        logger.log(Level.DEBUG, "filter_type: " + filterType);

        setupTokenDecoder(bc, firstPartitionLengthInBytes, offset);
        bc.seek();

        segmentQuants.parse(bc, segmentationIsEnabled == 1, macroBlockSegementAbsoluteDelta == 1);

        // Determine if the golden frame or ARF buffer should be updated and
        // how.
        // For all non key frames the GF and ARF refresh flags and sign bias
        // flags must be set explicitly.
        if (frameType != 0) {
            logger.log(Level.ERROR, "TODO:");
            throw new IllegalArgumentException("bad input: not intra");
        }
        refreshEntropyProbs = bc.readBit();
        logger.log(Level.DEBUG, "refresh_entropy_probs: " + refreshEntropyProbs);
        if (refreshEntropyProbs > 0) {

        }
        refreshLastFrame = 0;
        if (frameType == 0)
            refreshLastFrame = 1;
        else
            refreshLastFrame = bc.readBit();
        logger.log(Level.DEBUG, "refresh_last_frame: " + refreshLastFrame);

        for (int i = 0; i < BLOCK_TYPES; i++)
            for (int j = 0; j < COEF_BANDS; j++)
                for (int k = 0; k < PREV_COEF_CONTEXTS; k++)
                    for (int l = 0; l < MAX_ENTROPY_TOKENS - 1; l++) {

                        if (bc.readBool(Globals.vp8CoefUpdateProbs[i][j][k][l]) > 0) {
                            int newp = bc.readLiteral(8);
                            this.coefProbs[i][j][k][l] = newp;
                        }
                    }

        // Read the mb_no_coeff_skip flag
        macroBlockNoCoeffSkip = bc.readBit();
        logger.log(Level.DEBUG, "mb_no_coeff_skip: " + macroBlockNoCoeffSkip);

        if (frameType == 0) {
            readModes(bc);
        } else {
            logger.log(Level.ERROR, "TODO:");
            throw new IllegalArgumentException("bad input: not intra");
        }

        int ibc = 0;
        int num_part = 1 << multiTokenPartition;

        for (int mb_row = 0; mb_row < macroBlockRows; mb_row++) {

            if (num_part > 1) {

                tokenBoolDecoder = tokenBoolDecoders.get(ibc);
                tokenBoolDecoder.seek();

                decodeMacroBlockRow(mb_row);

                ibc++;
                if (ibc == num_part)
                    ibc = 0;
            } else
                decodeMacroBlockRow(mb_row);

            fireProgressUpdate(mb_row);
        }

        if (this.getFilterType() > 0 && this.getFilterLevel() != 0)
            this.loopFilter();
        return true;
    }

    private void decodeMacroBlockRow(int mbRow) throws IOException {
        for (int mbCol = 0; mbCol < macroBlockCols; mbCol++) {

            MacroBlock mb = getMacroBlock(mbCol, mbRow);

            mb.decodeMacroBlock(this);

            mb.dequantMacroBlock(this);
        }
    }

    public void fireLFProgressUpdate(float p) {
        for (IIOReadProgressListener listener : _listeners) {
            listener.imageProgress(null, (100f / buffersToCreate) + (p / buffersToCreate));
        }
    }

    private void fireProgressUpdate(int mb_row) {
        for (IIOReadProgressListener listener : _listeners) {
            listener.imageProgress(null,
                    (100.0f * ((float) (mb_row + 1) / (float) getMacroBlockRows())) / buffersToCreate);
        }
    }

    public void fireRGBProgressUpdate(float p) {
        for (IIOReadProgressListener listener : _listeners) {
            listener.imageProgress(null, ((bufferCount + 4) * (100f / buffersToCreate)) + (p / buffersToCreate));
        }
    }

    public SubBlock getAboveRightSubBlock(SubBlock sb, SubBlock.PLANE plane) {
        // this might break at right edge
        SubBlock r;
        MacroBlock mb = sb.getMacroBlock();
        int x = mb.getSubblockX(sb);
        int y = mb.getSubblockY(sb);

        if (plane == SubBlock.PLANE.Y1) {

            // top row
            if (y == 0 && x < 3) {

                MacroBlock mb2 = this.getMacroBlock(mb.getX(), mb.getY() - 1);
                r = mb2.getSubBlock(plane, x + 1, 3);
                return r;
            }
            // top right
            else if (y == 0 && x == 3) {

                MacroBlock mb2 = this.getMacroBlock(mb.getX() + 1, mb.getY() - 1);
                r = mb2.getSubBlock(plane, 0, 3);

                if (mb2.getX() == this.getMacroBlockCols()) {

                    int[][] dest = new int[4][4];
                    for (int b = 0; b < 4; b++)
                        for (int a = 0; a < 4; a++) {
                            if (mb2.getY() < 0)
                                dest[a][b] = 127;
                            else
                                dest[a][b] = this.getMacroBlock(mb.getX(), mb.getY() - 1)
                                        .getSubBlock(SubBlock.PLANE.Y1, 3, 3)
                                        .getDest()[3][3];
                        }

                    r = new SubBlock(mb2, null, null, SubBlock.PLANE.Y1);
                    r.setDest(dest);
                }

                return r;
            }
            // not right edge or top row
            else if (y > 0 && x < 3) {
                r = mb.getSubBlock(plane, x + 1, y - 1);
                return r;
            }
            // else use top right
            else {
                SubBlock sb2 = mb.getSubBlock(sb.getPlane(), 3, 0);
                return this.getAboveRightSubBlock(sb2, plane);
            }
        } else {
            throw new IllegalArgumentException("bad input: getAboveRightSubBlock()"); // TODO
        }
    }

    public SubBlock getAboveSubBlock(SubBlock sb, SubBlock.PLANE plane) {

        SubBlock r = sb.getAbove();
        if (r == null) {
            MacroBlock mb = sb.getMacroBlock();
            int x = mb.getSubblockX(sb);

            MacroBlock mb2 = getMacroBlock(mb.getX(), mb.getY() - 1);
            // TODO: SPLIT
            while (plane == SubBlock.PLANE.Y2 && mb2.getYMode() == Globals.B_PRED) {
                mb2 = getMacroBlock(mb2.getX(), mb2.getY() - 1);
            }
            r = mb2.getBottomSubBlock(x, sb.getPlane());
        }

        return r;
    }

    private static boolean getBit(int data, int bit) {
        int r = data & (1 << bit);
        return r > 0;
    }

    private static int getBitAsInt(int data, int bit) {
        int r = data & (1 << bit);
        if (r > 0)
            return 1;
        return 0;
    }

    public BufferedImage getBufferedImage() {
        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        useBufferedImage(bi);
        bufferCount++;
        return bi;
    }

    public int[][][][] getCoefProbs() {
        return coefProbs;
    }

    public BufferedImage getDebugImageDiff() {

        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        WritableRaster imRas = bi.getWritableTile(0, 0);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int[] c = new int[3];
                int yy, u, v;
                yy = 127 + this.getMacroBlock(x / 16, y / 16)
                        .getSubBlock(SubBlock.PLANE.Y1, (x % 16) / 4, (y % 16) / 4)
                        .getDiff()[x % 4][y % 4];
                u = 127 + this.getMacroBlock(x / 16, y / 16)
                        .getSubBlock(SubBlock.PLANE.U, ((x / 2) % 8) / 4, ((y / 2) % 8) / 4)
                        .getDiff()[(x / 2) % 4][(y / 2) % 4];
                v = 127 + this.getMacroBlock(x / 16, y / 16)
                        .getSubBlock(SubBlock.PLANE.V, ((x / 2) % 8) / 4, ((y / 2) % 8) / 4)
                        .getDiff()[(x / 2) % 4][(y / 2) % 4];
                c[0] = (int) (1.164 * (yy - 16) + 1.596 * (v - 128));
                c[1] = (int) (1.164 * (yy - 16) - 0.813 * (v - 128) - 0.391 * (u - 128));
                c[2] = (int) (1.164 * (yy - 16) + 2.018 * (u - 128));

                for (int z = 0; z < 3; z++) {
                    if (c[z] < 0)
                        c[z] = 0;
                    if (c[z] > 255)
                        c[z] = 255;
                }
                imRas.setPixel(x, y, c);
            }
            fireRGBProgressUpdate(100.0F * x / getWidth());
        }
        bufferCount++;
        return bi;
    }

    public BufferedImage getDebugImagePredict() {
        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        WritableRaster imRas = bi.getWritableTile(0, 0);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int[] c = new int[3];
                int yy, u, v;
                yy = this.getMacroBlock(x / 16, y / 16)
                        .getSubBlock(SubBlock.PLANE.Y1, (x % 16) / 4, (y % 16) / 4)
                        .getPredict()[x % 4][y % 4];
                u = this.getMacroBlock(x / 16, y / 16)
                        .getSubBlock(SubBlock.PLANE.U, ((x / 2) % 8) / 4, ((y / 2) % 8) / 4)
                        .getPredict()[(x / 2) % 4][(y / 2) % 4];
                v = this.getMacroBlock(x / 16, y / 16)
                        .getSubBlock(SubBlock.PLANE.V, ((x / 2) % 8) / 4, ((y / 2) % 8) / 4)
                        .getPredict()[(x / 2) % 4][(y / 2) % 4];
                c[0] = (int) (1.164 * (yy - 16) + 1.596 * (v - 128));
                c[1] = (int) (1.164 * (yy - 16) - 0.813 * (v - 128) - 0.391 * (u - 128));
                c[2] = (int) (1.164 * (yy - 16) + 2.018 * (u - 128));

                for (int z = 0; z < 3; z++) {
                    if (c[z] < 0)
                        c[z] = 0;
                    if (c[z] > 255)
                        c[z] = 255;
                }
                imRas.setPixel(x, y, c);
            }
            fireRGBProgressUpdate(100.0F * x / getWidth());
        }
        bufferCount++;
        return bi;
    }

    public BufferedImage getDebugImageUBuffer() {
        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        WritableRaster imRas = bi.getWritableTile(0, 0);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int[] c = new int[3];
                int u;
                u = this.getMacroBlock(x / 16, y / 16)
                        .getSubBlock(SubBlock.PLANE.U, ((x / 2) % 8) / 4, ((y / 2) % 8) / 4)
                        .getDest()[(x / 2) % 4][(y / 2) % 4];
                c[0] = u;
                c[1] = u;
                c[2] = u;

                for (int z = 0; z < 3; z++) {
                    if (c[z] < 0)
                        c[z] = 0;
                    if (c[z] > 255)
                        c[z] = 255;
                }
                imRas.setPixel(x, y, c);
            }
            fireRGBProgressUpdate(100.0F * x / getWidth());
        }
        bufferCount++;
        return bi;
    }

    public BufferedImage getDebugImageUDiffBuffer() {
        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        WritableRaster imRas = bi.getWritableTile(0, 0);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int[] c = new int[3];
                int u;
                u = 127 + this.getMacroBlock(x / 16, y / 16)
                        .getSubBlock(SubBlock.PLANE.U, ((x / 2) % 8) / 4, ((y / 2) % 8) / 4)
                        .getDiff()[(x / 2) % 4][(y / 2) % 4];
                c[0] = u;
                c[1] = u;
                c[2] = u;

                for (int z = 0; z < 3; z++) {
                    if (c[z] < 0)
                        c[z] = 0;
                    if (c[z] > 255)
                        c[z] = 255;
                }
                imRas.setPixel(x, y, c);
            }
            fireRGBProgressUpdate(100.0F * x / getWidth());
        }
        bufferCount++;
        return bi;
    }

    public BufferedImage getDebugImageUPredBuffer() {
        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        WritableRaster imRas = bi.getWritableTile(0, 0);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int[] c = new int[3];
                int u;
                u = this.getMacroBlock(x / 16, y / 16)
                        .getSubBlock(SubBlock.PLANE.U, ((x / 2) % 8) / 4, ((y / 2) % 8) / 4)
                        .getPredict()[(x / 2) % 4][(y / 2) % 4];
                c[0] = u;
                c[1] = u;
                c[2] = u;

                for (int z = 0; z < 3; z++) {
                    if (c[z] < 0)
                        c[z] = 0;
                    if (c[z] > 255)
                        c[z] = 255;
                }
                imRas.setPixel(x, y, c);
            }
            fireRGBProgressUpdate(100.0F * x / getWidth());
        }
        bufferCount++;
        return bi;
    }

    public BufferedImage getDebugImageVBuffer() {
        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        WritableRaster imRas = bi.getWritableTile(0, 0);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int[] c = new int[3];
                int v;
                v = this.getMacroBlock(x / 16, y / 16)
                        .getSubBlock(SubBlock.PLANE.V, ((x / 2) % 8) / 4, ((y / 2) % 8) / 4)
                        .getDest()[(x / 2) % 4][(y / 2) % 4];
                c[0] = v;
                c[1] = v;
                c[2] = v;

                for (int z = 0; z < 3; z++) {
                    if (c[z] < 0)
                        c[z] = 0;
                    if (c[z] > 255)
                        c[z] = 255;
                }
                imRas.setPixel(x, y, c);
            }
            fireRGBProgressUpdate(100.0F * x / getWidth());
        }
        bufferCount++;
        return bi;
    }

    public BufferedImage getDebugImageVDiffBuffer() {
        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        WritableRaster imRas = bi.getWritableTile(0, 0);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int[] c = new int[3];
                int v;
                v = 127 + this.getMacroBlock(x / 16, y / 16)
                        .getSubBlock(SubBlock.PLANE.V, ((x / 2) % 8) / 4, ((y / 2) % 8) / 4)
                        .getDiff()[(x / 2) % 4][(y / 2) % 4];
                c[0] = v;
                c[1] = v;
                c[2] = v;

                for (int z = 0; z < 3; z++) {
                    if (c[z] < 0)
                        c[z] = 0;
                    if (c[z] > 255)
                        c[z] = 255;
                }
                imRas.setPixel(x, y, c);
            }
            fireRGBProgressUpdate(100.0F * x / getWidth());
        }
        bufferCount++;
        return bi;
    }

    public BufferedImage getDebugImageVPredBuffer() {
        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        WritableRaster imRas = bi.getWritableTile(0, 0);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int[] c = new int[3];
                int v;
                v = this.getMacroBlock(x / 16, y / 16)
                        .getSubBlock(SubBlock.PLANE.V, ((x / 2) % 8) / 4, ((y / 2) % 8) / 4)
                        .getPredict()[(x / 2) % 4][(y / 2) % 4];
                c[0] = v;
                c[1] = v;
                c[2] = v;

                for (int z = 0; z < 3; z++) {
                    if (c[z] < 0)
                        c[z] = 0;
                    if (c[z] > 255)
                        c[z] = 255;
                }
                imRas.setPixel(x, y, c);
            }
            fireRGBProgressUpdate(100.0F * x / getWidth());
        }
        bufferCount++;
        return bi;
    }

    public BufferedImage getDebugImageYBuffer() {
        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        WritableRaster imRas = bi.getWritableTile(0, 0);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int[] c = new int[3];
                int yy;
                yy = this.getMacroBlock(x / 16, y / 16)
                        .getSubBlock(SubBlock.PLANE.Y1, (x % 16) / 4, (y % 16) / 4)
                        .getDest()[x % 4][y % 4];
                c[0] = yy;
                c[1] = yy;
                c[2] = yy;

                for (int z = 0; z < 3; z++) {
                    if (c[z] < 0)
                        c[z] = 0;
                    if (c[z] > 255)
                        c[z] = 255;
                }
                imRas.setPixel(x, y, c);
            }
            fireRGBProgressUpdate(100.0F * x / getWidth());
        }
        bufferCount++;
        return bi;
    }

    public BufferedImage getDebugImageYDiffBuffer() {
        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        WritableRaster imRas = bi.getWritableTile(0, 0);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int[] c = new int[3];
                int yy;
                yy = 127 + this.getMacroBlock(x / 16, y / 16)
                        .getSubBlock(SubBlock.PLANE.Y1, (x % 16) / 4, (y % 16) / 4)
                        .getDiff()[x % 4][y % 4];
                c[0] = yy;
                c[1] = yy;
                c[2] = yy;

                for (int z = 0; z < 3; z++) {
                    if (c[z] < 0)
                        c[z] = 0;
                    if (c[z] > 255)
                        c[z] = 255;
                }
                imRas.setPixel(x, y, c);
            }
            fireRGBProgressUpdate(100.0F * x / getWidth());
        }
        bufferCount++;
        return bi;
    }

    public BufferedImage getDebugImageYPredBuffer() {
        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        WritableRaster imRas = bi.getWritableTile(0, 0);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int[] c = new int[3];
                int yy;
                yy = this.getMacroBlock(x / 16, y / 16)
                        .getSubBlock(SubBlock.PLANE.Y1, (x % 16) / 4, (y % 16) / 4)
                        .getPredict()[x % 4][y % 4];
                c[0] = yy;
                c[1] = yy;
                c[2] = yy;

                for (int z = 0; z < 3; z++) {
                    if (c[z] < 0)
                        c[z] = 0;
                    if (c[z] > 255)
                        c[z] = 255;
                }
                imRas.setPixel(x, y, c);
            }
            fireRGBProgressUpdate(100.0F * x / getWidth());
        }
        bufferCount++;
        return bi;
    }

    public int getFilterLevel() {
        return filterLevel;
    }

    public int getFilterType() {
        return filterType;
    }

    public int getFrameType() {
        return frameType;
    }

    public int getHeight() {
        return height;
    }

    public SubBlock getLeftSubBlock(SubBlock sb, SubBlock.PLANE plane) {
        SubBlock r = sb.getLeft();
        if (r == null) {
            MacroBlock mb = sb.getMacroBlock();
            int y = mb.getSubblockY(sb);
            MacroBlock mb2 = getMacroBlock(mb.getX() - 1, mb.getY());
            // TODO: SPLIT

            while (plane == SubBlock.PLANE.Y2 && mb2.getYMode() == Globals.B_PRED)
                mb2 = getMacroBlock(mb2.getX() - 1, mb2.getY());

            r = mb2.getRightSubBlock(y, sb.getPlane());
        }

        return r;
    }

    public MacroBlock getMacroBlock(int mbCol, int mbRow) {
        return macroBlocks[mbCol + 1][mbRow + 1];
    }

    public int getMacroBlockCols() {
        return macroBlockCols;
    }

    public String getMacroBlockDebugString(int mbx, int mby, int sbx, int sby) {
        String r = "";
        if (mbx < this.macroBlockCols && mby < this.getMacroBlockRows()) {
            MacroBlock mb = getMacroBlock(mbx, mby);
            r = r + mb.getDebugString();
            if (sbx < 4 && sby < 4) {
                SubBlock sb = mb.getSubBlock(SubBlock.PLANE.Y1, sbx, sby);
                r = r + "\n SubBlock " + sbx + ", " + sby + "\n  " + sb.getDebugString();
                sb = mb.getSubBlock(SubBlock.PLANE.Y2, sbx, sby);
                r = r + "\n SubBlock " + sbx + ", " + sby + "\n  " + sb.getDebugString();
                sb = mb.getSubBlock(SubBlock.PLANE.U, sbx / 2, sby / 2);
                r = r + "\n SubBlock " + sbx / 2 + ", " + sby / 2 + "\n  " + sb.getDebugString();
                sb = mb.getSubBlock(SubBlock.PLANE.V, sbx / 2, sby / 2);
                r = r + "\n SubBlock " + sbx / 2 + ", " + sby / 2 + "\n  " + sb.getDebugString();
            }
        }
        return r;
    }

    public int getMacroBlockRows() {
        return macroBlockRows;
    }

    public int getQIndex() {
        return segmentQuants.getqIndex();
    }

    public SegmentQuants getSegmentQuants() {
        return segmentQuants;
    }

    public int getSharpnessLevel() {
        return sharpnessLevel;
    }

    public BoolDecoder getTokenBoolDecoder() throws IOException {
        tokenBoolDecoder.seek();
        return tokenBoolDecoder;
    }

    public int[][] getUBuffer() {
        int[][] r = new int[macroBlockCols * 8][macroBlockRows * 8];
        for (int y = 0; y < macroBlockRows; y++) {
            for (int x = 0; x < macroBlockCols; x++) {
                MacroBlock mb = macroBlocks[x + 1][y + 1];
                for (int b = 0; b < 2; b++) {
                    for (int a = 0; a < 2; a++) {
                        SubBlock sb = mb.getUSubBlock(a, b);
                        for (int d = 0; d < 4; d++) {
                            for (int c = 0; c < 4; c++) {
                                r[(x * 8) + (a * 4) + c][(y * 8) + (b * 4) + d] = sb.getDest()[c][d];
                            }
                        }
                    }
                }
            }
        }
        return r;
    }

    public int[][] getVBuffer() {
        int[][] r = new int[macroBlockCols * 8][macroBlockRows * 8];
        for (int y = 0; y < macroBlockRows; y++) {
            for (int x = 0; x < macroBlockCols; x++) {
                MacroBlock mb = macroBlocks[x + 1][y + 1];
                for (int b = 0; b < 2; b++) {
                    for (int a = 0; a < 2; a++) {
                        SubBlock sb = mb.getVSubBlock(a, b);
                        for (int d = 0; d < 4; d++) {
                            for (int c = 0; c < 4; c++) {
                                r[(x * 8) + (a * 4) + c][(y * 8) + (b * 4) + d] = sb.getDest()[c][d];
                            }
                        }
                    }
                }
            }
        }
        return r;
    }

    public int getWidth() {
        return width;
    }

    public int[][] getYBuffer() {
        int[][] r = new int[macroBlockCols * 16][macroBlockRows * 16];
        for (int y = 0; y < macroBlockRows; y++) {
            for (int x = 0; x < macroBlockCols; x++) {
                MacroBlock mb = macroBlocks[x + 1][y + 1];
                for (int b = 0; b < 4; b++) {
                    for (int a = 0; a < 4; a++) {
                        SubBlock sb = mb.getYSubBlock(a, b);
                        for (int d = 0; d < 4; d++) {
                            for (int c = 0; c < 4; c++) {
                                r[(x * 16) + (a * 4) + c][(y * 16) + (b * 4) + d] = sb.getDest()[c][d];
                            }
                        }
                    }
                }
            }
        }
        return r;
    }

    public void loopFilter() {
        LoopFilter.loopFilter(this);
    }

    private void readModes(BoolDecoder bc) throws IOException {
        int mb_row = -1;
        int prob_skip_false = 0;

        if (macroBlockNoCoeffSkip > 0) {
            prob_skip_false = bc.readLiteral(8);
        }

        while (++mb_row < macroBlockRows) {
            int mb_col = -1;
            while (++mb_col < macroBlockCols) {

//                if (this.segmentation_enabled > 0) {
//                    logger.log(Level.TRACE, Level.SEVERE, "TODO:");
//                    throw new IllegalArgumentException("bad input: segmentation_enabled()");
//                }
                // Read the macroblock coeff skip flag if this feature is in
                // use, else default to 0
                MacroBlock mb = getMacroBlock(mb_col, mb_row);

                if ((segmentationIsEnabled > 0) && (updateMacroBlockSegmentationMap > 0)) {
                    int value = bc.readTree(Globals.macroBlockSegmentTree, this.macroBlockSegmentTreeProbs);
                    mb.setSegmentId(value);
                }

                if (modeRefLoopFilterDeltaEnabled > 0) {
                    int level = filterLevel;
                    level = level + refLoopFilterDeltas[0];
                    level = (level < 0) ? 0 : Math.min(level, 63);
                    mb.setFilterLevel(level);
                } else {
                    mb.setFilterLevel(segmentQuants.getSegQuants()[mb.getSegmentId()].getFilterStrength());
//                    logger.log(Level.ERROR, "TODO:");
                }

                int mb_skip_coeff = 0;
                if (macroBlockNoCoeffSkip > 0)
                    mb_skip_coeff = bc.readBool(prob_skip_false);
                else
                    mb_skip_coeff = 0;
                mb.setSkipCoeff(mb_skip_coeff);

                int y_mode = readYMode(bc);

                mb.setYMode(y_mode);

                if (y_mode == Globals.B_PRED) {

                    for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < 4; j++) {

                            SubBlock sb = mb.getYSubBlock(j, i);

                            SubBlock A = getAboveSubBlock(sb, SubBlock.PLANE.Y1);

                            SubBlock L = getLeftSubBlock(sb, SubBlock.PLANE.Y1);

                            int mode = readSubBlockMode(bc, A.getMode(), L.getMode());

                            sb.setMode(mode);
                        }
                    }
                    if (modeRefLoopFilterDeltaEnabled > 0) {
                        int level = mb.getFilterLevel();
                        level = level + this.modeLoopFilterDeltas[0];
                        level = (level < 0) ? 0 : Math.min(level, 63);
                        mb.setFilterLevel(level);
                    }
                } else {
                    int BMode = switch (y_mode) {
                        case Globals.DC_PRED -> Globals.B_DC_PRED;
                        case Globals.V_PRED -> Globals.B_VE_PRED;
                        case Globals.H_PRED -> Globals.B_HE_PRED;
                        case Globals.TM_PRED -> Globals.B_TM_PRED;
                        default -> Globals.B_DC_PRED;
                    };

                    for (int x = 0; x < 4; x++) {
                        for (int y = 0; y < 4; y++) {
                            SubBlock sb = mb.getYSubBlock(x, y);
                            sb.setMode(BMode);
                        }
                    }
                }
                int mode = readUvMode(bc);
                mb.setUvMode(mode);
            }
        }
    }

    private int readPartitionSize(long l) throws IOException {
        frame.seek(l);
        int size = frame.readUnsignedByte() + (frame.readUnsignedByte() << 8) + (frame.readUnsignedByte() << 16);
        return size;
    }

    private static int readSubBlockMode(BoolDecoder bc, int A, int L) throws IOException {
        int i = bc.readTree(Globals.vp8SubBlockModeTree, Globals.vp8KeyFrameSubBlockModeProb[A][L]);
        return i;
    }

    private static int readUvMode(BoolDecoder bc) throws IOException {
        int i = bc.readTree(Globals.vp8UVModeTree, Globals.vp8KeyFrameUVModeProb);
        return i;
    }

    private static int readYMode(BoolDecoder bc) throws IOException {
        int i = bc.readTree(Globals.vp8KeyFrameYModeTree, Globals.vp8KeyFrameYModeProb);
        return i;
    }

    public void removeIIOReadProgressListener(IIOReadProgressListener listener) {
        _listeners.remove(listener);
    }

    public void setBuffersToCreate(int count) {
        this.buffersToCreate = 3 + count;
        this.bufferCount = 0;
    }

    private void setupTokenDecoder(BoolDecoder bc, int first_partition_length_in_bytes, long offset) throws IOException {

        long partitionSize = 0;
        long partitionsStart = offset + first_partition_length_in_bytes;
        long partition = partitionsStart;
        multiTokenPartition = bc.readLiteral(2);
        logger.log(Level.DEBUG, "multi_token_partition: " + multiTokenPartition);
        int num_part = 1 << multiTokenPartition;
        logger.log(Level.DEBUG, "num_part: " + num_part);
        if (num_part > 1) {
            partition += 3L * (num_part - 1);
        }
        for (int i = 0; i < num_part; i++) {
            // Calculate the length of this partition. The last partition size
            // is implicit.
            if (i < num_part - 1) {

                partitionSize = readPartitionSize(partitionsStart + (i * 3L));
                bc.seek();
            } else {
                partitionSize = frame.length() - partition;
            }

            tokenBoolDecoders.add(new BoolDecoder(frame, partition));
            partition += partitionSize;
        }
        tokenBoolDecoder = tokenBoolDecoders.get(0);
    }

    public void useBufferedImage(BufferedImage dst) {
        WritableRaster imRas = dst.getWritableTile(0, 0);

        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int[] c = new int[3];
                int yy, u, v;
                yy = this.getMacroBlock(x / 16, y / 16)
                        .getSubBlock(SubBlock.PLANE.Y1, (x % 16) / 4, (y % 16) / 4)
                        .getDest()[x % 4][y % 4];
                u = this.getMacroBlock(x / 16, y / 16)
                        .getSubBlock(SubBlock.PLANE.U, ((x / 2) % 8) / 4, ((y / 2) % 8) / 4)
                        .getDest()[(x / 2) % 4][(y / 2) % 4];
                v = this.getMacroBlock(x / 16, y / 16)
                        .getSubBlock(SubBlock.PLANE.V, ((x / 2) % 8) / 4, ((y / 2) % 8) / 4)
                        .getDest()[(x / 2) % 4][(y / 2) % 4];
                c[0] = (int) (1.164 * (yy - 16) + 1.596 * (v - 128));
                c[1] = (int) (1.164 * (yy - 16) - 0.813 * (v - 128) - 0.391 * (u - 128));
                c[2] = (int) (1.164 * (yy - 16) + 2.018 * (u - 128));

                for (int z = 0; z < 3; z++) {
                    if (c[z] < 0)
                        c[z] = 0;
                    if (c[z] > 255)
                        c[z] = 255;
                }
                imRas.setPixel(x, y, c);
            }
            fireRGBProgressUpdate(100.0F * x / getWidth());
        }
    }

    public void setFrame(ImageInputStream frame) throws IOException {
        this.frame.flush();
        this.frame.close();
        this.frame = frame;
        offset = frame.getStreamPosition();
        this.coefProbs = Globals.getDefaultCoefProbs();
        tokenBoolDecoders = new Vector<>();
    }
}

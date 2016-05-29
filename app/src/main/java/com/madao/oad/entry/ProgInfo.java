package com.madao.oad.entry;

import com.madao.util.Logs;

/**
 * com.madao.oad.entry
 *
 * @auth or
 * @sinced on 2016/5/29.
 */
public class ProgInfo {

    public int iBytes = 0; // Number of bytes programmed
    public short iBlocks = 0; // Number of blocks programmed
    public short nBlocks = 0; // Total number of blocks
    public int iTimeElapsed = 0; // Time elapsed in milliseconds

    public void reset(int len, int addBlockSize, int halFlashWodSize) {

        iBytes = 0;
        iBlocks = 0;
        iTimeElapsed = 0;
        nBlocks = (short) (len / (addBlockSize / halFlashWodSize));
        Logs.e("ProgInfo", "nBlock:" + nBlocks);
    }
}

package com.madao.oad.presenter;

import com.madao.oad.entry.ImgHdr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * com.madao.oad.presenter
 *
 * @auth or
 * @sinced on 2016/5/29.
 */
public class OadManager {
    private static final String TAG = OadManager.class.getSimpleName();

    private static final int FILE_BUFFER_SIZE = 0x40000;
    private static final int OAD_BLOCK_SIZE = 16;
    private static final int HAL_FLASH_WORD_SIZE = 4;
    private static final int OAD_BUFFER_SIZE = 2 + OAD_BLOCK_SIZE;
    private static final int OAD_IMG_HDR_SIZE = 8;
    private static final long TIMER_INTERVAL = 1000;

    private static final int SEND_INTERVAL = 20; // Milliseconds (make sure this
    // is longer than the
    // connection interval)
    private static final int BLOCKS_PER_CONNECTION = 4; // May sent up to four
    // blocks per connection

    // Programming
    private final byte[] mFileBuffer = new byte[FILE_BUFFER_SIZE];
    private final byte[] mOadBuffer = new byte[OAD_BUFFER_SIZE];
    private ImgHdr mFileImgHdr = new ImgHdr();

    private static OadManager mInstance;
    private OadManager(){}

    public static OadManager getInstance() {
        if(mInstance == null) {
            synchronized (OadManager.class) {
                if(mInstance == null) {
                    mInstance = new OadManager();
                }
            }
        }
        return mInstance;
    }

    public boolean loadFile(String filepath) {
        boolean fSuccess = true;
        try {
            InputStream stream;
            File f = new File(filepath);
            stream = new FileInputStream(f);
            stream.read(mFileBuffer, 0, mFileBuffer.length);
            stream.close();
        } catch (IOException e) {
            return false;
        }

        mFileImgHdr.ver = buildUint16(mFileBuffer[5], mFileBuffer[4]);
        mFileImgHdr.len = buildInt(mFileBuffer[7], mFileBuffer[6]);
        mFileImgHdr.imgType = ((mFileImgHdr.ver & 1) == 1) ? 'B' : 'A';
        System.arraycopy(mFileBuffer, 8, mFileImgHdr.uid, 0, 4);
        return fSuccess;
    }



    private  short buildUint16(byte hi, byte lo) {
        return (short) ((hi << 8) + (lo & 0xff));
    }

    private  int buildInt(byte hi, byte lo) {
        return (0xff & lo) | (0xff00 & (hi << 8));
    }
}

package com.madao.oad;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * com.madao.oad
 *
 * @auth or
 * @sinced on 2016/5/29.
 */
public class FileHelper {

    public static String loadStorage() {
        boolean isSdCardAvailable = FileHelper.isWriteableExternalStorage();
        if (isSdCardAvailable) {
           String  downLoadDir = Environment.getExternalStorageDirectory() + File.separator
                    + "oad" ;

            return downLoadDir;
        }
        return null;
    }


    private static boolean isWriteableExternalStorage() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    public static boolean createFile(String name) throws IOException {
        File fname = new File(name);
        return fname.createNewFile();
    }
}

package com.madao.util;


/**
 * 项目名称：androidstudio_client
 * 类描述：
 * 创建人：or
 * 创建时间：2015/10/20 16:23
 * 修改人：or
 * 修改时间：2015/10/20 16:23
 * 修改备注：
 */
public class UUIDUtils {

    public static final String OAD_SERVICE = "f000ffc0-0451-4000-b000-000000000000";
    public static final String CONN_CONTROL_SERVICE = "f000ccc0-0451-4000-b000-000000000000";

    public static final String DEFAULT_ACCESS_SERVICE = "00001800-0000-1000-8000-00805f9b34fb";

    public static final String PARAMETER_SERVER = "0000FFE2-0000-1000-8000-00805f9b34fb";

    public static final String OAD_IMAGE_IDENTIFY = "f000ffc1-0451-4000-b000-000000000000";
    public static final String OAD_BLOCK_REQUEST = "f000ffc2-0451-4000-b000-000000000000";

    public static final String CONN_PARAMS = "f000ccc2-0451-4000-b000-000000000000";


    public static final String OAD_FLAG = "0000FFEA-0000-1000-8000-00805f9b34fb";


    /**
     * 转成4位的字节数组
     *
     * @param res
     * @return
     */
    public static byte[] intTobyte4(int res) {
        byte[] targets = new byte[4];

        targets[0] = (byte) (res & 0xff);// 最低位
        targets[1] = (byte) ((res >> 8) & 0xff);// 次低位
        targets[2] = (byte) ((res >> 16) & 0xff);// 次高位
        targets[3] = (byte) (res >>> 24);// 最高位,无符号右移。
        return targets;
    }

    public static String bytes2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

}

package com.xiao.wifitest;

import java.io.InputStream;

/**
 * Created by ZzZz on 2016/6/14/0014.
 */
public class Utility {
    /******************************************************************************/
    public String readFromInputStream(InputStream in) {
        int count = 0;
        byte[] inDatas = null;
        try {
            while (count == 0) {
                count = in.available();
            }
            inDatas = new byte[count];
            in.read(inDatas);
            return new String(inDatas, "gb2312");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /******************************************************************************/
}

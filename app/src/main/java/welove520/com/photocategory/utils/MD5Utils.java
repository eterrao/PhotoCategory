package welove520.com.photocategory.utils;

import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * 计算MD5
 *
 * @author tielei
 */
public class MD5Utils {
    private static final char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'};

    public static final String digest(String message) {
        try {
            byte[] strTemp = message.getBytes();
            MessageDigest mdDigest = MessageDigest.getInstance("MD5");
            mdDigest.update(strTemp);
            byte[] md = mdDigest.digest();
            return bytesToHexString(md);
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
            return null;
        }
    }

    /**
     * 计算文件的md5值。
     * 这个算法不用把文件内容都读到内存，因此是个比较省内存的操作。
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public static String getFileMD5String(String filePath) throws IOException {
        MessageDigest mdDigest;
        try {
            mdDigest = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
            return null;
        }

        InputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            byte[] buf = new byte[1024];
            int numRead;
            while ((numRead = fis.read(buf)) != -1) {
                mdDigest.update(buf, 0, numRead);
            }
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        return bytesToHexString(mdDigest.digest());
    }


    public static String bytesToHexString(byte[] bytes) {
        int j = bytes.length;
        char str[] = new char[j * 2];
        int k = 0;
        for (int i = 0; i < j; i++) {
            byte byte0 = bytes[i];
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(str);
    }

    public static void main(String[] args) {
        System.out.println(MD5Utils.digest("fdsffsgeg"));
    }
}

package cho.carbon.fg.eln.algorithm;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 	文件操作
 * @author lhb
 *
 */
public class MyFileUtils {

	/**
     * 	文件转为 byte数组
     * @param filePath
     * @return
     */
    public static byte[] readFileToByteArray(String filePath) {

		InputStream in = null;
		ByteArrayOutputStream out = null;
		try {
			in = new FileInputStream(filePath);
			out = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024 * 4];
			int n = 0;
			while ((n = in.read(buffer)) != -1) {
				out.write(buffer, 0, n);
			}
			return out.toByteArray();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return null;
	}
}

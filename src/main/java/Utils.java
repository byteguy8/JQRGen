import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Utils {
    public static final Executor executorService = Executors.newCachedThreadPool();

    public static Integer strToIntegerOrNull(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static BufferedImage generateQR(String value, String charset, int width, int height)
            throws WriterException, UnsupportedEncodingException, UnsupportedOperationException {
        byte[] bytes = value.getBytes(charset);
        String newValue = new String(bytes);

        BitMatrix matrix = new MultiFormatWriter()
                .encode(newValue, BarcodeFormat.QR_CODE, width, height);

        return MatrixToImageWriter.toBufferedImage(matrix);
    }

    public static void writeImgToFile(String path, BufferedImage image) throws IOException {
        ByteArrayOutputStream out = null;
        InputStream input = null;

        try (FileOutputStream output = new FileOutputStream(path)) {
            out = new ByteArrayOutputStream();

            ImageIO.write(image, "png", output);

            input = new ByteArrayInputStream(out.toByteArray());

            byte[] buffer = new byte[1024];

            while (true) {
                int read = input.read(buffer, 0, buffer.length);

                if (read == -1)
                    break;

                output.write(buffer, 0, read);
            }
        } finally {
            if (out != null)
                out.close();

            if (input != null)
                input.close();
        }
    }
}

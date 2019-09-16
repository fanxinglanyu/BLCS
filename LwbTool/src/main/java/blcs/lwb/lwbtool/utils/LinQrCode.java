package blcs.lwb.lwbtool.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;
import java.util.Vector;

import blcs.lwb.lwbtool.utils.camera.BitmapLuminanceSource;
import blcs.lwb.lwbtool.utils.camera.RxImageTool;

/**
 * 二维码/条形码工具类
 * 1.生成二维码图片
 * 2.生成带Logo二维码图片
 * 3.生成条形码
 * 4.解析图片中的 二维码 或者 条形码
 */
public class LinQrCode {
    private static int IMAGE_HALFWIDTH = 100;//宽度值，影响中间图片大小

    /**
     * 1.生成二维码图片
     * @param content
     * @param view
     */
    public static void createQRCode(String content, ImageView view) {
        createQRCode(content, null,view);
    }
    /**
     * 2.生成带Logo二维码图片
     * @param content
     * @param mBitmap
     * @param view
     */
    public static void createQRCode(String content, Bitmap mBitmap,ImageView view) {
        view.setImageBitmap(createQRCode(content,500,0,mBitmap));
    }
    /**
     * 生成带logo的二维码，默认二维码的大小为500，logo为二维码的1/5
     * @param url 需要生成二维码的文字、网址等
     * @param size 需要生成二维码的大小（）
     * @param margin 空白边距
     * @param mBitmap logo文件
     * @return bitmap
     */
    public static Bitmap createQRCode(String url, int size,int margin, Bitmap mBitmap) {
        try {
            IMAGE_HALFWIDTH = size/10;
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            /*
             * 设置容错级别，默认为ErrorCorrectionLevel.L
             * 因为中间加入logo所以建议你把容错级别调至H,否则可能会出现识别不了
             */
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            //空白边距设置
            hints.put(EncodeHintType.MARGIN, margin);
            BitMatrix bitMatrix = new QRCodeWriter().encode(url,
                    BarcodeFormat.QR_CODE, size, size, hints);

            int width = bitMatrix.getWidth();//矩阵高度
            int height = bitMatrix.getHeight();//矩阵宽度
            int halfW = width / 2;
            int halfH = height / 2;
            if(mBitmap!=null){
                Matrix m = new Matrix();
                float sx = (float) 2 * IMAGE_HALFWIDTH / mBitmap.getWidth();
                float sy = (float) 2 * IMAGE_HALFWIDTH / mBitmap.getHeight();
                m.setScale(sx, sy);
                //设置缩放信息
                //将logo图片按martix设置的信息缩放
                mBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
                        mBitmap.getWidth(), mBitmap.getHeight(), m, false);
            }
            int[] pixels = new int[size * size];
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (mBitmap!=null && x > halfW - IMAGE_HALFWIDTH && x < halfW + IMAGE_HALFWIDTH
                            && y > halfH - IMAGE_HALFWIDTH
                            && y < halfH + IMAGE_HALFWIDTH) {
                        //该位置用于存放图片信息 记录图片每个像素信息
                        pixels[y * width + x] = mBitmap.getPixel(x - halfW
                                + IMAGE_HALFWIDTH, y - halfH + IMAGE_HALFWIDTH);
                    } else {
                        if (bitMatrix.get(x, y)) {
                            pixels[y * size + x] = 0xff000000;
                        } else {
                            pixels[y * size + x] = 0xffffffff;
                        }
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(size, size,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 3.生成条形码
     * @param contents 需要生成的内容
     * @return 条形码的Bitmap
     */
    public static Bitmap createBarCode(String contents) {
        return createBarCode(contents, 1000, 300);
    }

    public static void createBarCode(String content, int codeWidth, int codeHeight, ImageView view) {
        view.setImageBitmap(createBarCode(content, codeWidth, codeHeight));
    }

    public static void createBarCode(String content, ImageView view) {
        view.setImageBitmap(createBarCode(content));
    }

    /**
     * 生成条形码
     * @param content      需要生成的内容
     * @param BAR_WIDTH  生成条形码的宽带
     * @param BAR_HEIGHT 生成条形码的高度
     * @return backgroundColor
     */
    public static Bitmap createBarCode(CharSequence content, int BAR_WIDTH, int BAR_HEIGHT) {
        // 条形码的编码类型
        BarcodeFormat barcodeFormat = BarcodeFormat.CODE_128;
        final int backColor = 0xFFFFFFFF;
        final int barCodeColor = 0xFF000000;

        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = null;
        try {
            result = writer.encode(content + "", barcodeFormat, BAR_WIDTH, BAR_HEIGHT, null);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        // All are 0, or black, by default
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? barCodeColor : backColor;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }


    /**
     * 4.解析图片中的 二维码 或者 条形码
     * @param photo 待解析的图片
     * @return Result 解析结果，解析识别时返回NULL
     */
    public static Result decodeFromPhoto(Bitmap photo) {
        Result rawResult = null;
        if (photo != null) {
            Bitmap smallBitmap = RxImageTool.zoomBitmap(photo, photo.getWidth() / 2, photo.getHeight() / 2);// 为防止原始图片过大导致内存溢出，这里先缩小原图显示，然后释放原始Bitmap占用的内存
            photo.recycle(); // 释放原始图片占用的内存，防止out of memory异常发生

            MultiFormatReader multiFormatReader = new MultiFormatReader();

            // 解码的参数
            Hashtable<DecodeHintType, Object> hints = new Hashtable<>(2);
            // 可以解析的编码类型
            Vector<BarcodeFormat> decodeFormats = new Vector<>();
            if (decodeFormats.isEmpty()) {
                decodeFormats = new Vector<>();

                Vector<BarcodeFormat> PRODUCT_FORMATS = new Vector<>(5);
                PRODUCT_FORMATS.add(BarcodeFormat.UPC_A);
                PRODUCT_FORMATS.add(BarcodeFormat.UPC_E);
                PRODUCT_FORMATS.add(BarcodeFormat.EAN_13);
                PRODUCT_FORMATS.add(BarcodeFormat.EAN_8);
                // PRODUCT_FORMATS.add(BarcodeFormat.RSS14);
                Vector<BarcodeFormat> ONE_D_FORMATS = new Vector<>(PRODUCT_FORMATS.size() + 4);
                ONE_D_FORMATS.addAll(PRODUCT_FORMATS);
                ONE_D_FORMATS.add(BarcodeFormat.CODE_39);
                ONE_D_FORMATS.add(BarcodeFormat.CODE_93);
                ONE_D_FORMATS.add(BarcodeFormat.CODE_128);
                ONE_D_FORMATS.add(BarcodeFormat.ITF);
                Vector<BarcodeFormat> QR_CODE_FORMATS = new Vector<>(1);
                QR_CODE_FORMATS.add(BarcodeFormat.QR_CODE);
                Vector<BarcodeFormat> DATA_MATRIX_FORMATS = new Vector<>(1);
                DATA_MATRIX_FORMATS.add(BarcodeFormat.DATA_MATRIX);

                // 这里设置可扫描的类型，我这里选择了都支持
                decodeFormats.addAll(ONE_D_FORMATS);
                decodeFormats.addAll(QR_CODE_FORMATS);
                decodeFormats.addAll(DATA_MATRIX_FORMATS);
            }
            hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
            // 设置继续的字符编码格式为UTF8
            // hints.put(DecodeHintType.CHARACTER_SET, "UTF8");
            // 设置解析配置参数
            multiFormatReader.setHints(hints);

            // 开始对图像资源解码
            try {
                rawResult = multiFormatReader.decodeWithState(new BinaryBitmap(new HybridBinarizer(new BitmapLuminanceSource(smallBitmap))));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rawResult;
    }
}

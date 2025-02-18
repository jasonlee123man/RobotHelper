package cn.xjiangwei.RobotHelper.Tools;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import cn.xjiangwei.RobotHelper.R;


public class Image {

    /**
     * 保存图片到图库
     * Image.saveImageToGallery(bt, getExternalFilesDir("").getAbsolutePath() + "/asdf.png");
     *
     * @param bmp
     */
    public static void saveImageToGallery(Bitmap bmp, String bitName) {
        File file = new File(bitName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 打开本地图片
     * @param path
     * @return
     */
    public static Bitmap openImg(String path) {

        Bitmap ret = BitmapFactory.decodeFile(path);

        if (ret == null) {
            MLog.error("打开" + path + "失败！");
        }

        return ret;
    }


    /**
     * 单点找色
     *
     * @param img
     * @param color
     * @return
     */
    public static LinkedList<Point> findPoint(Bitmap img, Color color) {
        LinkedList<Point> pl = new LinkedList<Point>();
        int width = img.getWidth();
        int height = img.getHeight();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (Color.isSame(getPoint(img, i, j), color)) {
                    pl.add(new Point(i, j));
                }
            }
        }
        return pl;
    }


    /**
     * 多色找点
     * 在屏幕某个范围内
     *
     * @param img
     * @param colorRules
     * @param leftX
     * @param leftY
     * @param rightX
     * @param rightY
     * @return
     */
    public static Point findPointByMulColor(Bitmap img, String colorRules, int leftX, int leftY, int rightX, int rightY) {
        img = cropBitmap(img, leftX, leftY, rightX, rightY);
        Point p = findPointByMulColor(img, colorRules);
        if (p.isEmpty()) {
            return p;
        }
        return new Point(p.getX() + leftX, p.getY() + leftY);
    }

    /**
     * 多色找点函数
     * @param img
     * @param colorRules
     * @return
     */
    public static Point findPointByMulColor(Bitmap img, String colorRules) {
        long now = System.currentTimeMillis();
        int[] colors = new int[img.getWidth() * img.getHeight()];
        String[] res = colorRules.split(",");
        Color firstPointColor = HexColor2DecColor(res[0], true);
        img.getPixels(colors, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
        for (int i = 0; i < colors.length; i++) {
            if (Color.isSame(new Color(colors[i]), firstPointColor)) {
                int y = (int) (i / img.getWidth());
                int x = i % img.getWidth();
                for (int k = 1; k < res.length; k++) {
                    res[k] = res[k].replace("\"", "");
                    String[] info = res[k].split("\\|");
                    int testX = x + Integer.parseInt(info[0]);
                    int testY = y + Integer.parseInt(info[1]);
                    if (testX < 0 || testY < 0 || testX > img.getWidth() || testY > img.getHeight()) {
                        break;
                    }
                    Color nextColor = getPoint(img, testX, testY);
                    if (!Color.isSame(nextColor, HexColor2DecColor(info[2], true))) {
                        break;
                    } else {
                        if (k == (res.length - 1)) {
                            MLog.info("找点用时：", String.valueOf(System.currentTimeMillis() - now));
                            return new Point(x, y);
                        }
                    }
                }
            }
        }
        return new Point(-1, -1);
    }

    /**
     * 多色找点,自定义颜色误差
     *
     * @param img
     * @param colorRules
     * @param offset
     * @return
     */
    public static Point findPointByMulColor(Bitmap img, String colorRules, int offset) {
        long now = System.currentTimeMillis();
        int[] colors = new int[img.getWidth() * img.getHeight()];
        String[] res = colorRules.split(",");
        Color firstPointColor = HexColor2DecColor(res[0], true);
        img.getPixels(colors, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
        for (int i = 0; i < colors.length; i++) {
            if (Color.isSame(new Color(colors[i]), firstPointColor, offset)) {
                int y = (int) (i / img.getWidth());
                int x = i % img.getWidth();
                for (int k = 1; k < res.length; k++) {
                    res[k] = res[k].replace("\"", "");
                    String[] info = res[k].split("\\|");
                    int testX = x + Integer.parseInt(info[0]);
                    int testY = y + Integer.parseInt(info[1]);
                    if (testX < 0 || testY < 0 || testX > img.getWidth() || testY > img.getHeight()) {
                        break;
                    }
                    Color nextColor = getPoint(img, testX, testY);
                    if (!Color.isSame(nextColor, HexColor2DecColor(info[2], true))) {
                        break;
                    } else {
                        if (k == (res.length - 1)) {
                            MLog.info("找点用时：", String.valueOf(System.currentTimeMillis() - now));
                            return new Point(x, y);
                        }
                    }
                }
            }
        }
        return new Point(-1, -1);
    }

    /**
     *
     * 已废弃
     * @deprecated
     * @param img
     * @param colorRules
     * @return
     */
    public static Point findPointByMulColorBack(Bitmap img, String colorRules) {
        long now = System.currentTimeMillis();
        String[] res = colorRules.split(",");
        Color firstPointColor = HexColor2DecColor(res[0], true);
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();
        for (int i = 0; i < imgWidth; i++) {
            for (int j = 0; j < imgHeight; j++) {
                if (Color.isSame(getPoint(img, i, j), firstPointColor)) {
                    for (int k = 1; k < res.length; k++) {
                        res[k] = res[k].replace("\"", "");
                        String[] info = res[k].split("\\|");
                        int testX = i + Integer.parseInt(info[0]);
                        int testY = j + Integer.parseInt(info[1]);
                        if (testX < 0 || testY < 0 || testX > imgWidth || testY > imgHeight) {
                            break;
                        }
                        Color nextColor = getPoint(img, testX, testY);
                        if (!Color.isSame(nextColor, HexColor2DecColor(info[2], true))) {
                            break;
                        } else {
                            if (k == (res.length - 1)) {
                                MLog.info("找点用时：", String.valueOf(System.currentTimeMillis() - now));
                                return new Point(i, j);
                            }
                        }
                    }
                }
            }
        }
        MLog.info("找点用时：", String.valueOf(System.currentTimeMillis() - now));
        return new Point(-1, -1);
    }


    /**
     * 多色找点，返回屏幕内全部满足规则的点
     * @param img
     * @param colorRules
     * @return
     */
    public static LinkedList<Point> findPointsByMulColor(Bitmap img, String colorRules) {
        LinkedList<Point> ret = new LinkedList<Point>();
        String[] res = colorRules.split(",");
        Color firstPointColor = HexColor2DecColor(res[0], true);
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();

        for (int i = 0; i < imgWidth; i++) {
            for (int j = 0; j < imgHeight; j++) {
                if (Color.isSame(getPoint(img, i, j), firstPointColor)) {
                    for (int k = 1; k < res.length; k++) {
                        res[k] = res[k].replace("\"", "");
                        String[] info = res[k].split("\\|");
                        int testX = i + Integer.parseInt(info[0]);
                        int testY = j + Integer.parseInt(info[1]);
                        if (testX < 0 || testY < 0 || testX > imgWidth || testY > imgHeight) {
                            break;
                        }
                        Color nextColor = getPoint(img, testX, testY);
                        if (!Color.isSame(nextColor, HexColor2DecColor(info[2], true))) {
                            break;
                        } else {
                            if (k == (res.length - 1)) {
                                ret.add(new Point(i, j));
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }


    /**
     *
     * @param color
     * @return
     */
    public static Color HexColor2DecColor(String color) {
        color = color.replace("#", "");
        color = color.replace("\"", "");
        try {
            int r = Integer.parseInt(color.substring(0, 2), 16);
            int g = Integer.parseInt(color.substring(2, 4), 16);
            int b = Integer.parseInt(color.substring(4, 6), 16);
            return new Color(r, g, b);

        } catch (Exception e) {
            return new Color();
        }
    }

    /**
     *
     * @param color
     * @param bgr
     * @return
     */
    public static Color HexColor2DecColor(String color, boolean bgr) {
        color = color.replace("#", "");
        color = color.replace("\"", "");
        try {
            int b = Integer.parseInt(color.substring(0, 2), 16);
            int g = Integer.parseInt(color.substring(2, 4), 16);
            int r = Integer.parseInt(color.substring(4, 6), 16);
            return new Color(r, g, b);

        } catch (Exception e) {
            return new Color();
        }
    }


    /**
     * 获取一个点的颜色
     *
     * @param img
     * @param x
     * @param y
     * @return
     */
    public static Color getPoint(Bitmap img, int x, int y) {
        try {
            return new Color(img.getPixel(x, y));
        } catch (IllegalArgumentException e) {
            return new Color(0, 0, 0);
        }


    }


    /**
     * 预览图片
     *
     * @param img
     * @param context
     */
    public static void show(Bitmap img, Context context) {
        Dialog dia = new Dialog(context, R.style.edit_AlertDialog_style2);
        dia.setContentView(R.layout.activity_start_dialog);
        ImageView imageView = (ImageView) dia.findViewById(R.id.start_img);
        imageView.setImageBitmap(img);
        dia.show();

        dia.setCanceledOnTouchOutside(true); // Sets whether this dialog is
        Window w = dia.getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.x = 0;
        lp.y = 40;
        dia.onWindowAttributesChanged(lp);
    }


    /**
     * 裁剪
     *
     * @param bitmap
     * @param leftTopX
     * @param leftTopY
     * @param rightBottomX
     * @param rightBottomY
     * @return
     */
    public static Bitmap cropBitmap(Bitmap bitmap, int leftTopX, int leftTopY, int rightBottomX, int rightBottomY) {
        return Bitmap.createBitmap(bitmap, leftTopX, leftTopY, rightBottomX - leftTopX, rightBottomY - leftTopY, null, false);
    }


    /**
     * base64 图片
     *
     * @param bitmap
     * @return
     */
    public static String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        //读取图片到ByteArrayOutputStream
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos); //参数如果为100那么就不压缩
        byte[] bytes = baos.toByteArray();

        return Base64.encodeToString(bytes, Base64.DEFAULT);


    }


}

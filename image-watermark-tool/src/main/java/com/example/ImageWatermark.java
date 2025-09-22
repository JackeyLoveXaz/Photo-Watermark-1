package com.example;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;



import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageWatermark {

    public static void main(String[] args) {
        // 修正参数数量检查，原代码检查5个参数但实际只使用4个
        if (args.length < 4) {
            System.out.println("Usage: java ImageWatermark <image-path> <font-size> <color> <position>");
            System.out.println("Example: java ImageWatermark photo.jpg 12 red \"left top\"");
            System.out.println("Positions: left top, center, right bottom");
            System.out.println("Colors: red, green, blue, black, white");
            return;
        }

        String imagePath = args[0];
        int fontSize;
        try {
            fontSize = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Error: Font size must be an integer");
            return;
        }

        String colorName = args[2].toLowerCase();
        String position = args[3].toLowerCase();

        try {
            // 读取 EXIF 信息
            File imageFile = new File(imagePath);
            if (!imageFile.exists() || !imageFile.isFile()) {
                System.err.println("Error: Image file not found or not a valid file");
                return;
            }

            // 使用metadata-extractor库读取拍摄时间
            Date captureDate = getCaptureDate(imageFile);
            if (captureDate == null) {
                System.out.println("Warning: No EXIF date found, using current date");
                captureDate = new Date();
            }

            String watermarkText = new SimpleDateFormat("yyyy-MM-dd").format(captureDate);

            // 添加水印
            addWatermark(imageFile, watermarkText, fontSize, colorName, position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用metadata-extractor库获取图片拍摄日期
     */
    private static Date getCaptureDate(File imageFile) throws ImageProcessingException, IOException {
        Metadata metadata = ImageMetadataReader.readMetadata(imageFile);

        // 获取EXIF子目录，其中包含拍摄时间信息
        Directory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (directory != null) {
            // 尝试获取拍摄时间
            return directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        }
        return null;
    }

    private static void addWatermark(File imageFile, String watermarkText, int fontSize, String colorName, String position) throws IOException {
        BufferedImage originalImage = ImageIO.read(imageFile);
        Graphics2D g2d = (Graphics2D) originalImage.getGraphics();

        // 设置字体
        g2d.setFont(new Font("Arial", Font.BOLD, fontSize));
        Color color = getColor(colorName);
        g2d.setColor(color);

        // 计算水印位置
        int x = 0;
        int y = 0;
        switch (position) {
            case "left top":
                x = 10;
                y = 30;
                break;
            case "center":
                FontMetrics fm = g2d.getFontMetrics();
                x = (originalImage.getWidth() - fm.stringWidth(watermarkText)) / 2;
                y = originalImage.getHeight() / 2;
                break;
            case "right bottom":
                x = originalImage.getWidth() - g2d.getFontMetrics().stringWidth(watermarkText) - 10;
                y = originalImage.getHeight() - 10;
                break;
            default:
                System.out.println("Unknown position. Defaulting to 'left top'.");
                x = 10;
                y = 30;
        }

        g2d.drawString(watermarkText, x, y);
        g2d.dispose();

        // 保存带水印的图片
        saveWatermarkedImage(originalImage, imageFile);
    }

    private static void saveWatermarkedImage(BufferedImage image, File originalFile) throws IOException {
        String originalPath = originalFile.getAbsolutePath();
        String watermarkDirPath = originalPath.substring(0, originalPath.lastIndexOf(File.separator)) + File.separator + originalFile.getName().replace(".jpg", "") + "_watermark";
        new File(watermarkDirPath).mkdirs();
        
        File watermarkedImageFile = new File(watermarkDirPath, originalFile.getName());
        ImageIO.write(image, "jpg", watermarkedImageFile);
        System.out.println("Watermarked image saved to: " + watermarkedImageFile.getAbsolutePath());
    }

    private static Color getColor(String colorName) {
        switch (colorName) {
            case "red": return Color.RED;
            case "green": return Color.GREEN;
            case "blue": return Color.BLUE;
            case "black": return Color.BLACK;
            case "white": return Color.WHITE;
            default: return Color.BLACK; // 默认颜色
        }
    }
}
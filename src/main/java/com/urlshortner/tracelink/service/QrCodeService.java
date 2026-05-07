package com.urlshortner.tracelink.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/*
    Service responsible for generating QR codes for the shortened URLs. It supports both PNG and SVG formats. The generated QR code includes a watermark with the 
    text "TraceLink" below the code. The service uses the ZXing library to create the QR code and handles the necessary image manipulation to add the watermark and adjust the canvas size accordingly.
*/
@Service
public class QrCodeService {

    public byte[] generateQrCode(String redirectUrl, String originalUrl, String format, int width, int height)
            throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = qrCodeWriter.encode(redirectUrl, BarcodeFormat.QR_CODE, width, height, hints);

        if ("svg".equalsIgnoreCase(format)) {
            return generateSvg(bitMatrix, originalUrl);
        } 
        else {
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            int extraHeight = 35;
            BufferedImage finalImage = new BufferedImage(width, height + extraHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = finalImage.createGraphics();

            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height + extraHeight);

            g.drawImage(qrImage, 0, 0, null);

            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            FontMetrics fm = g.getFontMetrics();

            String text1 = "TraceLink";
            int text1X = (width - fm.stringWidth(text1)) / 2;
            g.drawString(text1, text1X, height + 22);

            g.dispose();

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            ImageIO.write(finalImage, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        }
    }

    private byte[] generateSvg(BitMatrix bitMatrix, String originalUrl) {
        StringBuilder svg = new StringBuilder();
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();

        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"")
                .append(width)
                .append("\" height=\"")
                .append(height)
                .append("\" viewBox=\"0 0 ")
                .append(width)
                .append(" ")
                .append(height)
                .append("\">\n");

        svg.append("<rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>\n");
        svg.append("<path d=\"");

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bitMatrix.get(x, y)) {
                    svg.append("M").append(x).append(",").append(y)
                            .append("h1v1h-1z ");
                }
            }
        }
        svg.append("\" fill=\"#000000\"/>\n");

        // Add watermark text to SVG
        int extraHeight = 35;
        String text1 = "TraceLink";

        svg.append("<text x=\"50%\" y=\"").append(height + 22)
                .append("\" font-family=\"Arial\" font-size=\"10\" fill=\"black\" text-anchor=\"middle\">").append(text1)
                .append("</text>\n");

        svg.append("</svg>");

        // Replace viewBox and height to accommodate extra height
        String svgStr = svg.toString();
        svgStr = svgStr.replace("height=\"" + height + "\"", "height=\"" + (height + extraHeight) + "\"");
        svgStr = svgStr.replace("viewBox=\"0 0 " + width + " " + height + "\"",
                "viewBox=\"0 0 " + width + " " + (height + extraHeight) + "\"");

        return svgStr.getBytes();
    }
}

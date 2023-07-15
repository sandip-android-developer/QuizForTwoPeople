/*
package com.example.quizfortwopeople.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;

public class PdfGenerator {

    public static void generatePdf(String filePath,String text) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Create a canvas with a bitmap
            Bitmap bitmap = Bitmap.createBitmap((int) PageSize.A4.getWidth(), (int) PageSize.A4.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            // Draw on the canvas
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(12f);
            canvas.drawText(text, 50, 50, paint);

            // Convert the bitmap to an iText image and add it to the document
            Image image = Image.getInstance(bitmap.toString());
            document.add(image);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
*/

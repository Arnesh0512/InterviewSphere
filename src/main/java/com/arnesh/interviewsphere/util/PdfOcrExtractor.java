package com.arnesh.interviewsphere.util;

import com.arnesh.interviewsphere.exception.CustomException;
import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.File;

@Component
public class PdfOcrExtractor {

    
    public String extractTextWithoutOcr(File pdfFile) {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            throw new CustomException("Failed to extract text from PDF: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
    public String extractTextWithOcr(File pdfFile) {
        StringBuilder text = new StringBuilder();
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            Tesseract tesseract = new Tesseract();

            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, 300);
                text.append(tesseract.doOCR(bufferedImage)).append("\n");
            }
            return text.toString();
        } catch (Exception e) {
            throw new CustomException("OCR extraction failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
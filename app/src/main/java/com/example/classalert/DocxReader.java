package com.example.classalert;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;

import java.io.InputStream;

public class DocxReader {

    /**
     * Read .docx file from input stream and extract text
     * Includes paragraphs and table content
     */
    public static String readDocxFromStream(InputStream inputStream) throws Exception {
        XWPFDocument document = new XWPFDocument(inputStream);
        StringBuilder builder = new StringBuilder();

        // Read paragraphs
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            String text = paragraph.getText();
            if (text != null && !text.trim().isEmpty()) {
                builder.append(text).append("\n");
            }
        }

        // Read tables
        for (XWPFTable table : document.getTables()) {
            builder.append("\n--- TABLE ---\n");
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    builder.append(cell.getText()).append(" | ");
                }
                builder.append("\n");
            }
            builder.append("--- END TABLE ---\n\n");
        }

        document.close();
        return builder.toString();
    }

    /**
     * Get document statistics
     */
    public static DocumentStats getDocumentStats(InputStream inputStream) throws Exception {
        XWPFDocument document = new XWPFDocument(inputStream);
        DocumentStats stats = new DocumentStats();

        stats.paragraphCount = document.getParagraphs().size();
        stats.tableCount = document.getTables().size();

        int wordCount = 0;
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            String[] words = paragraph.getText().split("\\s+");
            wordCount += words.length;
        }
        stats.wordCount = wordCount;

        document.close();
        return stats;
    }

    /**
     * Document statistics class
     */
    public static class DocumentStats {
        public int paragraphCount;
        public int tableCount;
        public int wordCount;

        @Override
        public String toString() {
            return String.format("Paragraphs: %d | Tables: %d | Words: %d",
                    paragraphCount, tableCount, wordCount);
        }
    }
}
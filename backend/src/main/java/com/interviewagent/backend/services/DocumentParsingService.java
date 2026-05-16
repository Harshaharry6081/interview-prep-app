package com.interviewagent.backend.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DocumentParsingService {

    private static final int CHUNK_SIZE = 500; // characters per chunk

    public List<String> extractAndChunkText(MultipartFile file) throws IOException {
        String text = "";
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";

        if (filename.toLowerCase().endsWith(".pdf")) {
            // PDFBox 3.x API: use Loader.loadPDF(byte[]) instead of PDDocument.load(InputStream)
            try (PDDocument document = Loader.loadPDF(file.getBytes())) {
                PDFTextStripper stripper = new PDFTextStripper();
                text = stripper.getText(document);
            }
        } else {
            // Treat as plain text
            text = new String(file.getBytes());
        }

        return chunkText(text.trim());
    }

    private List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) return chunks;

        String[] sentences = text.split("(?<=[.!?])\\s+");
        StringBuilder current = new StringBuilder();

        for (String sentence : sentences) {
            if (current.length() + sentence.length() > CHUNK_SIZE && current.length() > 0) {
                chunks.add(current.toString().trim());
                current = new StringBuilder();
            }
            current.append(sentence).append(" ");
        }
        if (current.length() > 0) {
            chunks.add(current.toString().trim());
        }
        return chunks;
    }
}

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class app extends JFrame implements ActionListener {

    private JButton openButton, convertButton;
    private JFileChooser fileChooser;
    private File selectedFile;
    private JLabel statusLabel;

    public app() {
        super("PDF to Flashcard Converter");

        // UI elements
        openButton = new JButton("Open PDF");
        convertButton = new JButton("Convert to CSV");
        convertButton.setEnabled(false); // Disable until a file is opened
        statusLabel = new JLabel("No file selected");

        // File chooser
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));

        // Action listeners
        openButton.addActionListener(this);
        convertButton.addActionListener(this);

        // Layout
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(openButton);
        panel.add(convertButton);
        panel.add(statusLabel);

        // Add panel to frame
        add(panel, BorderLayout.CENTER);

        // Frame settings
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null); // Center the frame
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == openButton) {
            int returnVal = fileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                statusLabel.setText("File selected: " + selectedFile.getName());
                convertButton.setEnabled(true);
            } else {
                statusLabel.setText("Open command cancelled by user.");
                convertButton.setEnabled(false);
            }
        } else if (e.getSource() == convertButton) {
            if (selectedFile != null) {
                try {
                    List<String[]> flashcards = extractFlashcardsFromPDF(selectedFile);
                    writeFlashcardsToCSV(flashcards, selectedFile.getName().replace(".pdf", ".csv"));
                    statusLabel.setText("CSV file created successfully!");
                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

    private List<String[]> extractFlashcardsFromPDF(File pdfFile) throws IOException {
        List<String[]> flashcards = new ArrayList<>();
        PDDocument document = null;
        try {
            document = PDDocument.load(pdfFile);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // Simple splitting logic (can be improved with regex or more sophisticated methods)
            String[] parts = text.split("Q:"); // Assuming questions start with "Q:"
            for (int i = 1; i < parts.length; i++) {
                String[] questionAnswer = parts[i].split("A:"); // Assuming answers start with "A:"
                if (questionAnswer.length == 2) {
                    String question = questionAnswer[0].trim();
                    String answer = questionAnswer[1].trim();
                    flashcards.add(new String[]{question, answer});
                }
            }
        } finally {
            if (document != null) {
                document.close();
            }
        }
        return flashcards;
    }

    private void writeFlashcardsToCSV(List<String[]> flashcards, String csvFileName) throws IOException {
        try (
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFileName));
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                    .withHeader("Question", "Answer"))
        ) {
            for (String[] flashcard : flashcards) {
                csvPrinter.printRecord(flashcard[0], flashcard[1]);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new app());
    }
}

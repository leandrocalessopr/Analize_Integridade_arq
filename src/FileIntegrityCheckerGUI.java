import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class FileIntegrityCheckerGUI {

    private JFrame frame;
    private JTextField filePathField;
    private JTextArea resultArea;
    private JComboBox<String> hashTypeComboBox;
    private JTextField originalHashField;
    private JTextField generatedHashField;

    private static final Map<String, String> HASH_ALGORITHMS = new HashMap<>();
    static {
        HASH_ALGORITHMS.put("SHA-256", "SHA-256");
        HASH_ALGORITHMS.put("MD5", "MD5");
        HASH_ALGORITHMS.put("SHA-1", "SHA-1");
        HASH_ALGORITHMS.put("SHA-384", "SHA-384");
        HASH_ALGORITHMS.put("SHA-512", "SHA-512");
        HASH_ALGORITHMS.put("SHA3-256", "SHA3-256");
        HASH_ALGORITHMS.put("SHA3-384", "SHA3-384");
        HASH_ALGORITHMS.put("SHA3-512", "SHA3-512");
    }

    public FileIntegrityCheckerGUI() {
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("File Integrity CheckerV1.0");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("Verificador de Integridade de Arquivo");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(titleLabel);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);

        JLabel filePathLabel = new JLabel("Selecionar Arquivo:");
        constraints.gridx = 0;
        constraints.gridy = 0;
        centerPanel.add(filePathLabel, constraints);

        filePathField = new JTextField(30);
        filePathField.setEditable(false);
        constraints.gridx = 1;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        centerPanel.add(filePathField, constraints);

        JButton browseButton = new JButton("Buscar");
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectFile();
            }
        });
        constraints.gridx = 2;
        constraints.weightx = 0.0;
        centerPanel.add(browseButton, constraints);

        JLabel hashTypeLabel = new JLabel("Tipo de Hash:");
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        centerPanel.add(hashTypeLabel, constraints);

        hashTypeComboBox = new JComboBox<>(HASH_ALGORITHMS.keySet().toArray(new String[0]));
        constraints.gridx = 1;
        constraints.weightx = 0.5;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        centerPanel.add(hashTypeComboBox, constraints);

        JLabel originalHashLabel = new JLabel("Hash Original:");
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.weightx = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        centerPanel.add(originalHashLabel, constraints);

        originalHashField = new JTextField(30);
        constraints.gridx = 1;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        centerPanel.add(originalHashField, constraints);

        JButton generateHashButton = new JButton("Gerar Hash");
        generateHashButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateHash();
            }
        });
        constraints.gridx = 2;
        constraints.weightx = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        centerPanel.add(generateHashButton, constraints);

        JLabel generatedHashLabel = new JLabel("Hash Gerado:");
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.weightx = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        centerPanel.add(generatedHashLabel, constraints);

        generatedHashField = new JTextField(30);
        generatedHashField.setEditable(false);
        constraints.gridx = 1;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        centerPanel.add(generatedHashField, constraints);

        JButton checkIntegrityButton = new JButton("Verificar Integridade");
        checkIntegrityButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkIntegrity();
            }
        });
        constraints.gridx = 1;
        constraints.gridy = 4;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        centerPanel.add(checkIntegrityButton, constraints);

        resultArea = new JTextArea(10, 50);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.BOTH;
        centerPanel.add(scrollPane, constraints);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        frame.getContentPane().add(mainPanel);
        frame.setVisible(true);
    }

    private void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Arquivos", "*");
        fileChooser.setFileFilter(filter);

        int returnVal = fileChooser.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            filePathField.setText(file.getAbsolutePath());
            resultArea.setText("");
        }
    }

    private void generateHash() {
        String filePath = filePathField.getText();
        String selectedHashType = HASH_ALGORITHMS.get(hashTypeComboBox.getSelectedItem());

        if (!filePath.isEmpty() && selectedHashType != null) {
            String computedHash = computeHash(filePath, selectedHashType);
            if (computedHash != null) {
                generatedHashField.setText(computedHash);
            } else {
                generatedHashField.setText("Erro ao calcular o hash.");
            }
        } else {
            generatedHashField.setText("Selecione um arquivo e um tipo de hash.");
        }
    }

    private void checkIntegrity() {
        String filePath = filePathField.getText();
        String selectedHashType = HASH_ALGORITHMS.get(hashTypeComboBox.getSelectedItem());
        String originalHash = originalHashField.getText();

        if (!filePath.isEmpty() && selectedHashType != null && !originalHash.isEmpty()) {
            String computedHash = computeHash(filePath, selectedHashType);
            if (computedHash != null && computedHash.equals(originalHash)) {
                resultArea.setText("O arquivo está íntegro.");
            } else {
                resultArea.setText("O arquivo está corrompido ou foi modificado.");
            }
        } else {
            resultArea.setText("Preencha todos os campos corretamente.");
        }
    }

    private String computeHash(String filePath, String hashType) {
        try {
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            MessageDigest md = MessageDigest.getInstance(hashType);
            byte[] hashBytes = md.digest(fileBytes);
            return bytesToHex(hashBytes);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FileIntegrityCheckerGUI();
            }
        });
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.regex.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.nio.file.attribute.BasicFileAttributes;

public class FileSorter extends JFrame {

    // Declare settings variables
    private boolean darkMode = false; // Default to light mode
    String defaultSourceLocation;
    String defaultDestinationLocation;

    // Registry key for storing settings
    private static final String REGISTRY_KEY = "SOFTWARE\\FileSorter";

    // Other components declaration
    JTextField sourceTextField;
    JTextField destinationTextField;
    JList<String> sourceFileList;
    JList<String> destinationFileList;
    private Set<String> allowedExtensions = new HashSet<>();

    public FileSorter() {
        super("File Sorter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Load settings from preferences file
        loadPreferences();

        // Components for source folder
        JLabel sourceLabel = new JLabel("Source Folder:");
        sourceTextField = new JTextField(defaultSourceLocation);
        JButton sourceBrowseButton = new JButton("Browse");

        // Components for destination folder
        JLabel destinationLabel = new JLabel("Destination Folder:");
        destinationTextField = new JTextField(defaultDestinationLocation);
        destinationTextField.setBackground(Color.DARK_GRAY);
        JButton destinationBrowseButton = new JButton("Browse");

        // JList for source files
        DefaultListModel<String> sourceListModel = new DefaultListModel<>();
        sourceFileList = new JList<>(sourceListModel);
        JScrollPane sourceScrollPane = new JScrollPane(sourceFileList);

        // JList for destination files
        DefaultListModel<String> destinationListModel = new DefaultListModel<>();
        destinationFileList = new JList<>(destinationListModel);
        JScrollPane destinationScrollPane = new JScrollPane(destinationFileList);

        // Button to move selected files or folders
        JButton moveButton = new JButton("Move");
        moveButton.setPreferredSize(new Dimension(100, 30));

        // Button to sort files
        JButton sortButton = new JButton("Sort Files");
        sortButton.setPreferredSize(new Dimension(100, 30));

        // Button to open settings
        JButton settingsButton = new JButton("Settings");

        // Action listener for the settings button
        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSettings();
            }
        });

        // Action listener for the source browse button
        sourceBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseFolder(sourceTextField);
                updateFileList(new File(sourceTextField.getText()), sourceListModel);
            }
        });

        // Action listener for the destination browse button
        destinationBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseFolder(destinationTextField);
                updateFileList(new File(destinationTextField.getText()), destinationListModel);
            }
        });

        // Update file lists for default source and destination paths
        updateFileList(new File(sourceTextField.getText()), sourceListModel);
        updateFileList(new File(destinationTextField.getText()), destinationListModel);

        // Action listener for the move button
        moveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveFiles();
            }
        });

        // Action listener for the sort button
        sortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sortFiles();
            }
        });

        // Adding components to the frame
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(sourceLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(sourceTextField, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        add(sourceBrowseButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(destinationLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(destinationTextField, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        add(destinationBrowseButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(sourceScrollPane, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        add(moveButton, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(destinationScrollPane, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        add(sortButton, gbc);

        gbc.gridx = 2;
        gbc.gridy = 3;
        add(settingsButton, gbc);

        // Adjusting the layout
        pack();
        setLocationRelativeTo(null); // Center the frame

        // Load settings from the registry
        loadSettings();

        // Apply initial dark mode setting
        updateDarkMode();
    }

    // Method to open settings window
    private void openSettings() {
        JFrame settingsFrame = new JFrame("Settings");
        settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        settingsFrame.setSize(600, 200);

        // Create components for settings window (e.g., checkboxes, text fields)
        JCheckBox darkModeCheckBox = new JCheckBox("Dark Mode");
        darkModeCheckBox.setSelected(darkMode);
        darkModeCheckBox.setOpaque(false); // Make checkbox transparent

        // Customize checkbox appearance for dark mode
        if (darkMode) {
            UIManager.put("CheckBox.foreground", Color.WHITE);
            UIManager.put("CheckBox.background", new Color(0, 0, 0, 0)); // Transparent
        } else {
            UIManager.put("CheckBox.foreground", Color.BLACK);
            UIManager.put("CheckBox.background", new Color(255, 255, 255, 0)); // Transparent
        }

        JTextField defaultSourceLocationField = new JTextField(defaultSourceLocation);
        JButton sourceBrowseButton = new JButton("Browse");
        sourceBrowseButton.setBackground((Color.DARK_GRAY));
        sourceBrowseButton.setForeground(Color.WHITE);
        JTextField defaultDestinationLocationField = new JTextField(defaultDestinationLocation);
        JButton destinationBrowseButton = new JButton("Browse");
        destinationBrowseButton.setForeground(Color.WHITE);
        destinationBrowseButton.setBackground(Color.DARK_GRAY);
        JButton saveButton = new JButton("Save");

        // Action listeners for browse buttons
        sourceBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseFolder(defaultSourceLocationField);
            }
        });

        destinationBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseFolder(defaultDestinationLocationField);
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Update settings variables with new values
                darkMode = darkModeCheckBox.isSelected();
                defaultSourceLocation = defaultSourceLocationField.getText();
                defaultDestinationLocation = defaultDestinationLocationField.getText();

                // Save settings to the registry
                saveSettings();
                savePreferences(); // Save preferences to file

                // Close settings window
                settingsFrame.dispose();

                // Update UI to reflect changes
                updateDarkMode();
            }
        });

        // Layout settings components
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Dark mode checkbox
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(darkModeCheckBox, gbc);

        // Default Source Location label and text field
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Default Source Location:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(defaultSourceLocationField, gbc);

        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        panel.add(sourceBrowseButton, gbc);

        // Default Destination Location label and text field
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Default Destination Location:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(defaultDestinationLocationField, gbc);

        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        panel.add(destinationBrowseButton, gbc);

        // Save button
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(saveButton, gbc);

        // Update UI to reflect dark mode
        if (darkMode) {
            panel.setBackground(Color.BLACK);
            darkModeCheckBox.setForeground(Color.WHITE);
            defaultSourceLocationField.setBackground(Color.BLACK);
            defaultDestinationLocationField.setBackground(Color.BLACK);
            defaultSourceLocationField.setForeground(Color.WHITE);
            defaultDestinationLocationField.setForeground(Color.WHITE);
            saveButton.setBackground(Color.DARK_GRAY);
            saveButton.setForeground(Color.WHITE);
        } else {
            panel.setBackground(Color.WHITE);
            darkModeCheckBox.setForeground(Color.BLACK);
            defaultSourceLocationField.setBackground(Color.WHITE);
            defaultDestinationLocationField.setBackground(Color.WHITE);
            defaultSourceLocationField.setForeground(Color.BLACK);
            defaultDestinationLocationField.setForeground(Color.BLACK);
            saveButton.setBackground(Color.WHITE);
            saveButton.setForeground(Color.BLACK);
        }

        settingsFrame.add(panel);
        settingsFrame.setVisible(true);
    }

    // Method to save settings to the registry
    private void saveSettings() {
        try {
            // Open or create the registry key
            Preferences prefs = Preferences.userRoot().node(REGISTRY_KEY);

            // Save settings
            prefs.putBoolean("darkMode", darkMode);
            prefs.put("defaultSourceLocation", defaultSourceLocation);
            prefs.put("defaultDestinationLocation", defaultDestinationLocation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to load settings from the registry
    private void loadSettings() {
        try {
            // Open the registry key
            Preferences prefs = Preferences.userRoot().node(REGISTRY_KEY);

            // Load settings
            darkMode = prefs.getBoolean("darkMode", false); // Default to false if not found
            defaultSourceLocation = prefs.get("defaultSourceLocation", "C:\\Users\\t.colwell\\Desktop");
            defaultDestinationLocation = prefs.get("defaultDestinationLocation", "C:\\Users\\t.colwell\\TechSmith Corporation\\Tech Support - Ticket Data");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to paste a filepath into the text field
    private void pasteFilePath(JTextField textField) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);

        if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String pastedText = (String) contents.getTransferData(DataFlavor.stringFlavor);
                textField.setText(pastedText);

                // Update the file list based on the pasted folder
                updateFileList(new File(pastedText), (DefaultListModel<String>) sourceFileList.getModel());
            } catch (UnsupportedFlavorException | IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void updateFileList(File folder, DefaultListModel<String> listModel) {
        // Update the file list based on the selected folder
        listModel.clear();

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                listModel.addElement(file.getName());
            }
        }
    }

    private void chooseFolder(JTextField textField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            textField.setText(selectedFile.getAbsolutePath());
        }
    }

    void moveFiles() {
        File[] selectedFiles = sourceFileList.getSelectedValuesList()
                .stream()
                .map(fileName -> new File(sourceTextField.getText(), fileName))
                .toArray(File[]::new);

        String destinationPath = destinationTextField.getText();

        int confirmResult = JOptionPane.showConfirmDialog(this,
                "You're about to move " + selectedFiles.length + " item(s). Are you sure?",
                "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirmResult == JOptionPane.YES_OPTION) {
            for (File selectedFile : selectedFiles) {
                moveFileOrFolder(selectedFile, destinationPath);
            }

            // Update source and destination file lists after the move
            updateFileList(new File(sourceTextField.getText()), (DefaultListModel<String>) sourceFileList.getModel());
            updateFileList(new File(destinationTextField.getText()), (DefaultListModel<String>) destinationFileList.getModel());

            // Display success message
            JOptionPane.showMessageDialog(this, "Items moved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void moveFileOrFolder(File source, String destinationPath) {
        String fileName = source.getName();
        File destination = new File(destinationPath, fileName);

        if (destination.exists()) {
            // If the file with the same name already exists in the destination folder
            String nameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
            int copyIndex = 1;

            // Append a number until a unique filename is found
            while (destination.exists()) {
                String newName = nameWithoutExtension + "(" + copyIndex + ")." + extension;
                destination = new File(destinationPath, newName);
                copyIndex++;
            }
        }

        try {
            Files.move(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void sortFiles() {
        String sourcePath = sourceTextField.getText();
        String destinationPath = destinationTextField.getText();

        StringBuilder successMessageBuilder = new StringBuilder();

        try {
            Files.walkFileTree(Paths.get(sourcePath), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String sourceFileName = file.getFileName().toString();

                    // Check for files with 6 consecutive numbers in the title
                    if (hasConsecutiveNumbers(sourceFileName, 6)) {
                        String commonNumberPart = getCommonNumberPart(sourceFileName);
                        File destinationFolder = new File(destinationPath, commonNumberPart);
                        File destinationFile = new File(destinationFolder, sourceFileName);

                        // If the file already exists in the destination folder, append a number to the filename
                        int copyIndex = 1;
                        while (destinationFile.exists()) {
                            String fileName = sourceFileName;
                            int extensionIndex = fileName.lastIndexOf('.');
                            String nameWithoutExtension = fileName.substring(0, extensionIndex);
                            String extension = fileName.substring(extensionIndex + 1);
                            String newName = nameWithoutExtension + "(" + copyIndex + ")." + extension;
                            destinationFile = new File(destinationFolder, newName);
                            copyIndex++;
                        }

                        try {
                            Files.move(file, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            if (successMessageBuilder.length() > 0) {
                                successMessageBuilder.append(System.lineSeparator()); // Add a newline if not the first message
                            }
                            successMessageBuilder.append(sourceFileName).append(" was moved to folder ").append(commonNumberPart);
                        } catch (NoSuchFileException e) {
                            // Skip the file if the destination folder does not exist
                        }
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    String sourceDirName = dir.getFileName().toString();

                    // Check for directories with 6 consecutive numbers in the title
                    if (hasConsecutiveNumbers(sourceDirName, 6)) {
                        String commonNumberPart = getCommonNumberPart(sourceDirName);
                        File destinationFolder = new File(destinationPath, commonNumberPart);
                        File destinationDir = new File(destinationFolder, sourceDirName);

                        // If the directory already exists in the destination folder, append a number to the directory name
                        int copyIndex = 1;
                        while (destinationDir.exists()) {
                            destinationDir = new File(destinationFolder, sourceDirName + "(" + copyIndex + ")");
                            copyIndex++;
                        }

                        try {
                            Files.move(dir, destinationDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            if (successMessageBuilder.length() > 0) {
                                successMessageBuilder.append(System.lineSeparator()); // Add a newline if not the first message
                            }
                            successMessageBuilder.append(sourceDirName).append(" was moved to folder ").append(commonNumberPart);
                        } catch (NoSuchFileException e) {
                            // Skip the directory if the destination folder does not exist
                        }
                    }

                    return FileVisitResult.CONTINUE;
                }
            });

            // Display success message
            JOptionPane.showMessageDialog(this, successMessageBuilder.toString(), "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            // Display error message in case of an exception
            JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Update source and destination file lists after sorting
        updateFileList(new File(sourcePath), (DefaultListModel<String>) sourceFileList.getModel());
        updateFileList(new File(destinationPath), (DefaultListModel<String>) destinationFileList.getModel());
    }


    private void moveDirectoryWithCommonNumber(File sourceDir, String commonNumberPart, String destinationPath) {
        File destinationFolder = new File(destinationPath, commonNumberPart);

        if (!destinationFolder.exists()) {
            if (!destinationFolder.mkdir()) {
                JOptionPane.showMessageDialog(this, "Error creating destination folder: " + destinationFolder.getAbsolutePath(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        File destinationDir = new File(destinationFolder, sourceDir.getName());

        // If the directory already exists in the destination folder, append a number to the directory name
        int copyIndex = 1;
        while (destinationDir.exists()) {
            destinationDir = new File(destinationFolder, sourceDir.getName() + "(" + copyIndex + ")");
            copyIndex++;
        }

        try {
            Files.move(sourceDir.toPath(), destinationDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void moveFileWithCommonNumber(File sourceFile, String commonNumberPart, String destinationPath) {
        File destinationFolder = new File(destinationPath, commonNumberPart);

        if (!destinationFolder.exists()) {
            if (!destinationFolder.mkdir()) {
                JOptionPane.showMessageDialog(this, "Error creating destination folder: " + destinationFolder.getAbsolutePath(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        File destinationFile = new File(destinationFolder, sourceFile.getName());

        // If the file already exists in the destination folder, append a number to the filename
        int copyIndex = 1;
        String fileName = sourceFile.getName();
        int extensionIndex = fileName.lastIndexOf('.');
        String nameWithoutExtension = fileName.substring(0, extensionIndex);
        String extension = fileName.substring(extensionIndex + 1);

        while (destinationFile.exists()) {
            String newName = nameWithoutExtension + "(" + copyIndex + ")." + extension;
            destinationFile = new File(destinationFolder, newName);
            copyIndex++;
        }

        try {
            Files.move(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean hasConsecutiveNumbers(String fileName, int consecutiveCount) {
        Pattern pattern = Pattern.compile("\\d{" + consecutiveCount + "}");
        Matcher matcher = pattern.matcher(fileName);

        return matcher.find();
    }

    private String getCommonNumberPart(String fileName) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find()) {
            return matcher.group();
        }

        return "";
    }

    private void updateDarkMode() {
        // Update UI colors for dark mode
        if (darkMode) {
            getContentPane().setBackground(Color.BLACK);
            setButtonColors(Color.DARK_GRAY, Color.WHITE);
            setTextFieldColors(Color.BLACK, Color.WHITE);
        } else {
            getContentPane().setBackground(Color.WHITE);
            setButtonColors(null, null);
            setTextFieldColors(null, null);
        }
    }

    private void setButtonColors(Color background, Color foreground) {
        // Set button colors
        Component[] components = getContentPane().getComponents();
        for (Component component : components) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                if (background != null) {
                    button.setBackground(background);
                }
                if (foreground != null) {
                    button.setForeground(foreground);
                }
            }
        }
    }

    private void setTextFieldColors(Color background, Color foreground) {
        // Set text field colors
        sourceTextField.setBackground(background);
        destinationTextField.setBackground(background);
        sourceTextField.setForeground(foreground);
        destinationTextField.setForeground(foreground);
    }

    void loadPreferences() {
        try (BufferedReader reader = new BufferedReader(new FileReader("preferences.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("defaultSourceLocation=")) {
                    defaultSourceLocation = line.substring(line.indexOf('=') + 1);
                } else if (line.startsWith("defaultDestinationLocation=")) {
                    defaultDestinationLocation = line.substring(line.indexOf('=') + 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // If there's an error, fallback to defaults
            defaultSourceLocation = "C:\\Users\\t.colwell\\Desktop";
            defaultDestinationLocation = "C:\\Users\\t.colwell\\TechSmith Corporation\\Tech Support - Ticket Data";
        }
    }

    void savePreferences() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("preferences.txt"))) {
            writer.write("defaultSourceLocation=" + defaultSourceLocation);
            writer.newLine();
            writer.write("defaultDestinationLocation=" + defaultDestinationLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FileSorter().setVisible(true);
            }
        });
    }

    public void savePreferencesToFile(String customSourcePath, String customDestinationPath) {
    }
}

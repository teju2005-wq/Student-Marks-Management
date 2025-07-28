import java.sql.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.text.DecimalFormat;

public class EnhancedMarksReport extends JFrame {
    // Database connection parameters
    private static final String URL = "jdbc:mysql://localhost:3306/student_management";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final String[] SUBJECTS = {"Mathematics", "Science", "English", "History", "Geography", "Computer Science"};

    private JTextField tfRoll, tfName, tfClass;
    private JComboBox<String> cbSubject;
    private JTextField tfMarks;
    private JTable resultTable;
    private DefaultTableModel tableModel; //to manage data displayed by JTable
    private JLabel statusLabel;
    private JButton btnSearch, btnExport, btnClear, btnInsert, btnUpdate, btnDelete;
    private JTabbedPane tabbedPane;

    // For insert/update form
    private JTextField tfInsertRoll, tfInsertName, tfInsertClass;
    private JComboBox<String> cbInsertSubject;
    // Add these text fields for individual subjects
    private JTextField tfMaths, tfScience, tfEnglish, tfHistory, tfGeography, tfComputer;
    private JButton btnSaveStudent, btnClearForm;

  
    public EnhancedMarksReport() {
        setTitle("Student Marks Management System");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        setupLayout();

        setVisible(true);
    }
    private void initComponents() {
        // Initialize tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Initialize search panel components
        tfRoll = new JTextField(10);
        tfName = new JTextField(15);
        tfClass = new JTextField(5);
        tfMarks = new JTextField(5);
        cbSubject = new JComboBox<>();
        cbSubject.addItem("Select Subject");
        for (String subject : SUBJECTS) {
            cbSubject.addItem(subject);
        }
    
        // Initialize buttons
        btnSearch = new JButton("Search");
        btnExport = new JButton("Export");
        btnClear = new JButton("Clear");
        btnInsert = new JButton("Add New");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
    
        // Initialize table
        String[] columns = {"Roll No.", "Name", "Class", "Mathematics", "Science", "English", 
                           "History", "Geography", "Computer Science", "Total", "Percentage", "Rank"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        resultTable = new JTable(tableModel); //Creates a JTable using the table model
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        resultTable.getTableHeader().setReorderingAllowed(false);
        
        // Add selection listener to table
        resultTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && resultTable.getSelectedRow() != -1) {
                populateFieldsFromSelection();
            }
        });
    
        // Status label
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Color.BLUE);
    
        // Insert/Update form components
        tfInsertRoll = new JTextField(10);
        tfInsertName = new JTextField(20);
        tfInsertClass = new JTextField(5);
        // Initialize subject-specific text fields
        tfMaths = new JTextField(5);
        tfScience = new JTextField(5);
        tfEnglish = new JTextField(5);
        tfHistory = new JTextField(5);
        tfGeography = new JTextField(5);
        tfComputer = new JTextField(5);
        
        btnSaveStudent = new JButton("Save");
        btnClearForm = new JButton("Clear");
    
        // Add action listeners
        btnSearch.addActionListener(e -> performSearch());
        btnExport.addActionListener(e -> exportResults());
        btnClear.addActionListener(e -> clearFields());
        btnInsert.addActionListener(e -> tabbedPane.setSelectedIndex(1));
        btnUpdate.addActionListener(e -> updateSelectedStudent());
        btnDelete.addActionListener(e -> deleteSelectedStudent());
        btnSaveStudent.addActionListener(e -> saveStudent());
        btnClearForm.addActionListener(e -> clearInsertForm());
    }
    
    private JPanel createDataEntryPanel() {
        JPanel panel = new JPanel(new GridBagLayout()); //flexible layout manager.
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints(); //to control component positioning in GridBagLayout.
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Row 1
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Roll Number:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(tfInsertRoll, gbc);
        
        // Row 2
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(tfInsertName, gbc);
        
        // Row 3
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("Class:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(tfInsertClass, gbc);
        
        // Subject marks - using a panel for organizing these fields
        JPanel marksPanel = new JPanel(new GridLayout(6, 2, 10, 5));
        marksPanel.setBorder(BorderFactory.createTitledBorder("Subject Marks"));
        
        marksPanel.add(new JLabel("Mathematics:"));
        marksPanel.add(tfMaths);
        
        marksPanel.add(new JLabel("Science:"));
        marksPanel.add(tfScience);
        
        marksPanel.add(new JLabel("English:"));
        marksPanel.add(tfEnglish);
        
        marksPanel.add(new JLabel("History:"));
        marksPanel.add(tfHistory);
        
        marksPanel.add(new JLabel("Geography:"));
        marksPanel.add(tfGeography);
        
        marksPanel.add(new JLabel("Computer Science:"));
        marksPanel.add(tfComputer);
        
        // Add the marks panel
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(marksPanel, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnSaveStudent);
        buttonPanel.add(btnClearForm);
        
        panel.add(buttonPanel, gbc);
        
        return panel;
    }

    private void setupLayout() {
        // Create the search panel
        JPanel searchPanel = createSearchPanel();
        
        // Create the data entry panel
        JPanel dataEntryPanel = createDataEntryPanel();
        
        // Add panels to tabbed pane
        tabbedPane.addTab("Search & View", createViewPanel(searchPanel));
        tabbedPane.addTab("Add/Edit Student", dataEntryPanel);
        
        // Main layout
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
        
        // Status panel at bottom
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Criteria"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // First row
        gbc.gridx = 0; gbc.gridy = 0;
        searchPanel.add(new JLabel("Roll No:"), gbc);
        
        gbc.gridx = 1;
        searchPanel.add(tfRoll, gbc);
        
        gbc.gridx = 2;
        searchPanel.add(new JLabel("Name:"), gbc);
        
        gbc.gridx = 3;
        searchPanel.add(tfName, gbc);
        
        // Second row
        gbc.gridx = 0; gbc.gridy = 1;
        searchPanel.add(new JLabel("Class:"), gbc);
        
        gbc.gridx = 1;
        searchPanel.add(tfClass, gbc);
        
        gbc.gridx = 2;
        searchPanel.add(new JLabel("Subject:"), gbc);
        
        gbc.gridx = 3;
        searchPanel.add(cbSubject, gbc);
        
        // Third row
        gbc.gridx = 0; gbc.gridy = 2;
        searchPanel.add(new JLabel("Marks:"), gbc);
        
        gbc.gridx = 1;
        searchPanel.add(tfMarks, gbc);
        
        return searchPanel;
    }
    
    private JPanel createViewPanel(JPanel searchPanel) {
        JPanel viewPanel = new JPanel(new BorderLayout());
        viewPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Table in center
        viewPanel.add(new JScrollPane(resultTable), BorderLayout.CENTER);
        
        // Button panel at bottom
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnSearch);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnInsert);
        buttonPanel.add(btnExport);
        buttonPanel.add(btnClear);
        
        viewPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return viewPanel;
    }
    
    

    private void performSearch() {
        String subject = (String) cbSubject.getSelectedItem();
        searchRecords(subject);
    }

    private void searchRecords(String subject) {
    boolean filterBySubject = !subject.equals("Select Subject") && !subject.isEmpty();
    String marksStr = tfMarks.getText().trim();
    boolean filterByMarks = !marksStr.isEmpty() && marksStr.matches("\\d+");

    StringBuilder queryBuilder = new StringBuilder(
        "SELECT s.student_id, s.roll_number, s.name, s.class, " +
        "MAX(CASE WHEN m.subject = 'Mathematics' THEN m.marks_obtained ELSE 0 END) AS Mathematics, " +
        "MAX(CASE WHEN m.subject = 'Science' THEN m.marks_obtained ELSE 0 END) AS Science, " +
        "MAX(CASE WHEN m.subject = 'English' THEN m.marks_obtained ELSE 0 END) AS English, " +
        "MAX(CASE WHEN m.subject = 'History' THEN m.marks_obtained ELSE 0 END) AS History, " +
        "MAX(CASE WHEN m.subject = 'Geography' THEN m.marks_obtained ELSE 0 END) AS Geography, " +
        "MAX(CASE WHEN m.subject = 'Computer Science' THEN m.marks_obtained ELSE 0 END) AS ComputerScience " +
        "FROM students s " +
        "LEFT JOIN marks m ON s.student_id = m.student_id " +
        "WHERE 1=1 "
    );
              
    List<String> conditions = new ArrayList<>();
    List<Object> parameters = new ArrayList<>();
    
    String roll = tfRoll.getText().trim();
    String name = tfName.getText().trim();
    String className = tfClass.getText().trim();
    
    if (!roll.isEmpty()) {
        conditions.add("AND LOWER(s.roll_number) LIKE LOWER(?)");
        parameters.add("%" + roll + "%");
    }
    
    if (!name.isEmpty()) {
        conditions.add("AND LOWER(s.name) LIKE LOWER(?)");
        parameters.add("%" + name + "%");
    }
    
    if (!className.isEmpty()) {
        conditions.add("AND LOWER(s.class) LIKE LOWER(?)");
        parameters.add("%" + className + "%");
    }
    
    if (filterBySubject) {
        if (filterByMarks) {
            conditions.add("AND EXISTS (SELECT 1 FROM marks m2 WHERE m2.student_id = s.student_id " +
                          "AND LOWER(m2.subject) = LOWER(?) AND m2.marks_obtained = ?)");
            parameters.add(subject);
            parameters.add(Integer.parseInt(marksStr));
        } else {
            conditions.add("AND EXISTS (SELECT 1 FROM marks m2 WHERE m2.student_id = s.student_id " +
                          "AND LOWER(m2.subject) = LOWER(?))");
            parameters.add(subject);
        }
    } else if (filterByMarks) {
        conditions.add("AND EXISTS (SELECT 1 FROM marks m2 WHERE m2.student_id = s.student_id " +
                      "AND m2.marks_obtained = ?)");
        parameters.add(Integer.parseInt(marksStr));
    }
    
    for (String condition : conditions) {
        queryBuilder.append(condition);
    }
    
    queryBuilder.append(" GROUP BY s.student_id, s.roll_number, s.name, s.class");
    queryBuilder.append(" ORDER BY s.roll_number"); 
    
    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
         PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {
        
        for (int i = 0; i < parameters.size(); i++) {
            pstmt.setObject(i + 1, parameters.get(i));
        }
        
        ResultSet rs = pstmt.executeQuery();
        
        // Process results with column visibility based on subject filter
        processResults(rs, !filterBySubject, subject);
        
        setStatus("Search complete. " + tableModel.getRowCount() + " records found.", false);
        
    } catch (SQLException e) {
        showErrorMessage("⚠️ Database error: " + e.getMessage());
    }
}

private void processResults(ResultSet rs, boolean showAllColumns, String filteredSubject) throws SQLException {
    // Clear previous data
    tableModel.setRowCount(0);
    
    // Create proper column structure based on filter
    String[] columns;
    if (showAllColumns) {
        columns = new String[]{
            "Roll No.", "Name", "Class", "Mathematics", "Science", "English",
            "History", "Geography", "Computer Science", "Total", "Percentage", "Rank"
        };
    } else {
        columns = new String[]{
            "Roll No.", "Name", "Class", filteredSubject, "Total", "Percentage", "Rank"
        };
    }
    
    // Create new table model with correct columns
    DefaultTableModel newModel = new DefaultTableModel(columns, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    
    // List to store all students for ranking
    List<StudentRecord> students = new ArrayList<>();
    
    while (rs.next()) {
        int studentId = rs.getInt("student_id");
        String rollNumber = rs.getString("roll_number");
        String name = rs.getString("name");
        String className = rs.getString("class");
        
        int mathMarks = rs.getInt("Mathematics");
        int scienceMarks = rs.getInt("Science");
        int englishMarks = rs.getInt("English");
        int historyMarks = rs.getInt("History");
        int geographyMarks = rs.getInt("Geography");
        int computerMarks = rs.getInt("ComputerScience");
        
        int totalMarks = mathMarks + scienceMarks + englishMarks + 
                         historyMarks + geographyMarks + computerMarks;
        
        double percentage = (totalMarks / 600.0) * 100;
        
        students.add(new StudentRecord(
            studentId, rollNumber, name, className, 
            mathMarks, scienceMarks, englishMarks,
            historyMarks, geographyMarks, computerMarks,
            totalMarks, percentage
        ));
    }
    
    // Sort students by total marks (descending)
    Collections.sort(students, Comparator.comparing(StudentRecord::getTotalMarks).reversed());
    
    // Assign ranks and populate table
    int rank = 1;
    int prevTotal = -1;
    int sameRankCount = 0;
    
    for (StudentRecord student : students) {
        if (prevTotal != student.getTotalMarks()) {
            rank += sameRankCount;
            sameRankCount = 1;
            prevTotal = student.getTotalMarks();
        } else {
            sameRankCount++;
        }
        
        DecimalFormat df = new DecimalFormat("0.00");
        
        Object[] row;
        if (showAllColumns) {
            row = new Object[]{
                student.getRollNumber(),
                student.getName(),
                student.getClassName(),
                student.getMathMarks(),
                student.getScienceMarks(),
                student.getEnglishMarks(),
                student.getHistoryMarks(),
                student.getGeographyMarks(),
                student.getComputerMarks(),
                student.getTotalMarks(),
                df.format(student.getPercentage()) + "%",
                rank
            };
        } else {
            // Get marks for the filtered subject
            int subjectMarks = 0;
            switch (filteredSubject) {
                case "Mathematics": subjectMarks = student.getMathMarks(); break;
                case "Science": subjectMarks = student.getScienceMarks(); break;
                case "English": subjectMarks = student.getEnglishMarks(); break;
                case "History": subjectMarks = student.getHistoryMarks(); break;
                case "Geography": subjectMarks = student.getGeographyMarks(); break;
                case "Computer Science": subjectMarks = student.getComputerMarks(); break;
            }
            
            row = new Object[]{
                student.getRollNumber(),
                student.getName(),
                student.getClassName(),
                subjectMarks,  // Correct subject marks
                student.getTotalMarks(),
                df.format(student.getPercentage()) + "%",
                rank
            };
        }
        
        newModel.addRow(row);
    }
    
    // Set the new model to the table
    resultTable.setModel(newModel);
    tableModel = newModel;
    
    // Configure column sizes
    for (int i = 0; i < resultTable.getColumnCount(); i++) {
        resultTable.getColumnModel().getColumn(i).setPreferredWidth(100);
    }
}
    private void exportResults() {
        // Get the export file name from user
        String roll = JOptionPane.showInputDialog(this, "Enter Roll Number (leave blank for all):");
        String name = JOptionPane.showInputDialog(this, "Enter Student Name (leave blank for all):");
        
        // Check if user cancelled
        if (roll == null || name == null) {
            return;
        }
        
        // Filter the data for export
        List<Integer> rowsToExport = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String rowRoll = (String) tableModel.getValueAt(i, 0);
            String rowName = (String) tableModel.getValueAt(i, 1);
            
            boolean matchesRoll = roll.isEmpty() || rowRoll.toLowerCase().contains(roll.toLowerCase());
            boolean matchesName = name.isEmpty() || rowName.toLowerCase().contains(name.toLowerCase());
            
            if (matchesRoll && matchesName) {
                rowsToExport.add(i);
            }
        }
        
        if (rowsToExport.isEmpty()) {
            showErrorMessage("No matching records found for export.");
            return;
        }
        
        // Choose export location
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Export File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        File file = fileChooser.getSelectedFile();
        String filePath = file.getAbsolutePath();
        if (!filePath.toLowerCase().endsWith(".csv")) {
            filePath += ".csv";
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write header
            writer.println("Roll Number,Name,Class,Mathematics,Science,English,History,Geography,Computer Science,Total,Percentage,Rank");
            
            // Write data rows
            for (int rowIndex : rowsToExport) {
                StringBuilder line = new StringBuilder();
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    line.append(tableModel.getValueAt(rowIndex, j));
                    if (j < tableModel.getColumnCount() - 1) {
                        line.append(",");
                    }
                }
                writer.println(line.toString());
            }
            
            setStatus("✅ Successfully exported " + rowsToExport.size() + " records to " + filePath, false);
            
        } catch (IOException e) {
            showErrorMessage("Export failed: " + e.getMessage());
        }
    }
    
    
    private void clearFields() {
        tfRoll.setText("");
        tfName.setText("");
        tfClass.setText("");
        tfMarks.setText("");
        cbSubject.setSelectedIndex(0);
        tableModel.setRowCount(0);
        setStatus("Fields cleared.", false);
    }
    
    private void populateFieldsFromSelection() {
        int selectedRow = resultTable.getSelectedRow();
        if (selectedRow != -1) {
            tfInsertRoll.setText(resultTable.getValueAt(selectedRow, 0).toString());
            tfInsertName.setText(resultTable.getValueAt(selectedRow, 1).toString());
            tfInsertClass.setText(resultTable.getValueAt(selectedRow, 2).toString());
            
            tfMaths.setText(resultTable.getValueAt(selectedRow, 3).toString());
            tfScience.setText(resultTable.getValueAt(selectedRow, 4).toString());
            tfEnglish.setText(resultTable.getValueAt(selectedRow, 5).toString());
            tfHistory.setText(resultTable.getValueAt(selectedRow, 6).toString());
            tfGeography.setText(resultTable.getValueAt(selectedRow, 7).toString());
            tfComputer.setText(resultTable.getValueAt(selectedRow, 8).toString());
    
            statusLabel.setText("Row selected. Ready to Update/Delete.");
        }
    }
    
   
    private void saveStudent() {
        String rollNo = tfInsertRoll.getText().trim();
        String name = tfInsertName.getText().trim();
        String className = tfInsertClass.getText().trim();
    
        // Fetch marks for all subjects from respective text fields
        String mathsMarks = tfMaths.getText().trim();
        String scienceMarks = tfScience.getText().trim();
        String englishMarks = tfEnglish.getText().trim();
        String historyMarks = tfHistory.getText().trim();
        String geographyMarks = tfGeography.getText().trim();
        String computerMarks = tfComputer.getText().trim();
    
        // Validate inputs
        if (rollNo.isEmpty() || name.isEmpty() || className.isEmpty()) {
            showErrorMessage("Please enter Roll Number, Name, and Class.");
            return;
        }
    
        // Validate all subjects' marks
        if (!validateMarks(mathsMarks, "Mathematics") || 
            !validateMarks(scienceMarks, "Science") || 
            !validateMarks(englishMarks, "English") || 
            !validateMarks(historyMarks, "History") || 
            !validateMarks(geographyMarks, "Geography") ||
            !validateMarks(computerMarks, "Computer Science")) {
            return;
        }
    
        // Convert marks to integers
        int maths = Integer.parseInt(mathsMarks);
        int science = Integer.parseInt(scienceMarks);
        int english = Integer.parseInt(englishMarks);
        int history = Integer.parseInt(historyMarks);
        int geography = Integer.parseInt(geographyMarks);
        int computer = Integer.parseInt(computerMarks);
    
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false);
    
            // Check if student exists
            int studentId = getStudentId(conn, rollNo);
    
            if (studentId == -1) {
                // Insert new student
                String insertStudentSql = "INSERT INTO students (roll_number, name, class) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertStudentSql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, rollNo);
                    pstmt.setString(2, name);
                    pstmt.setString(3, className);
                    pstmt.executeUpdate();
    
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            studentId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Failed to get student ID after insert.");
                        }
                    }
                }
                setStatus("✅ Added new student: " + name, false);
            } else {
                // Update existing student
                String updateStudentSql = "UPDATE students SET name = ?, class = ? WHERE student_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateStudentSql)) {
                    pstmt.setString(1, name);
                    pstmt.setString(2, className);
                    pstmt.setInt(3, studentId);
                    pstmt.executeUpdate();
                }
                setStatus("✅ Updated student: " + name, false);
            }
    
            // Insert or update marks for all subjects
            insertOrUpdateMarks(conn, studentId, "Mathematics", maths);
            insertOrUpdateMarks(conn, studentId, "Science", science);
            insertOrUpdateMarks(conn, studentId, "English", english);
            insertOrUpdateMarks(conn, studentId, "History", history);
            insertOrUpdateMarks(conn, studentId, "Geography", geography);
            insertOrUpdateMarks(conn, studentId, "Computer Science", computer);
    
            conn.commit();
            clearInsertForm();
            performSearch(); // Refresh table
    
            JOptionPane.showMessageDialog(this, 
                "Student information has been successfully saved.", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            tabbedPane.setSelectedIndex(0);
    
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback transaction if error occurs
                } catch (SQLException ex) {
                    showErrorMessage("Transaction rollback failed: " + ex.getMessage());
                }
            }
            showErrorMessage("Database error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    showErrorMessage("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
    
    private boolean validateMarks(String marksStr, String subject) {
        if (marksStr.isEmpty()) {
            showErrorMessage("Please enter marks for " + subject);
            return false;
        }
        
        if (!marksStr.matches("\\d+")) {
            showErrorMessage("Invalid marks for " + subject + ". Please enter a number.");
            return false;
        }
        
        int marks = Integer.parseInt(marksStr);
        if (marks < 0 || marks > 100) {
            showErrorMessage("Marks for " + subject + " must be between 0 and 100.");
            return false;
        }
        
        return true;
    }
    
    private void insertOrUpdateMarks(Connection conn, int studentId, String subject, int marks) throws SQLException {
        String checkSql = "SELECT * FROM marks WHERE student_id = ? AND subject = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setInt(1, studentId);
            pstmt.setString(2, subject);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Update existing marks
                String updateSql = "UPDATE marks SET marks_obtained = ? WHERE student_id = ? AND subject = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, marks);
                    updateStmt.setInt(2, studentId);
                    updateStmt.setString(3, subject);
                    updateStmt.executeUpdate();
                }
            } else {
                // Insert new marks
                String insertSql = "INSERT INTO marks (student_id, subject, marks_obtained) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, studentId);
                    insertStmt.setString(2, subject);
                    insertStmt.setInt(3, marks);
                    insertStmt.executeUpdate();
                }
            }
        }
    }
    private int getStudentId(Connection conn, String rollNo) throws SQLException {
        String sql = "SELECT student_id FROM students WHERE roll_number = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rollNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("student_id");
                }
                return -1;
            }
        }
    }
    
    private void updateSelectedStudent() {
        int selectedRow = resultTable.getSelectedRow();
        if (selectedRow == -1) {
            showErrorMessage("Please select a student to update");
            return;
        }
        tabbedPane.setSelectedIndex(1);
        
        String rollNo = tableModel.getValueAt(selectedRow, 0).toString();
        String name = tableModel.getValueAt(selectedRow, 1).toString();
        String className = tableModel.getValueAt(selectedRow, 2).toString();
        
        // Get subject marks
        Object mathsObj = tableModel.getValueAt(selectedRow, 3);
        Object scienceObj = tableModel.getValueAt(selectedRow, 4);
        Object englishObj = tableModel.getValueAt(selectedRow, 5);
        Object historyObj = tableModel.getValueAt(selectedRow, 6);
        Object geographyObj = tableModel.getValueAt(selectedRow, 7);
        Object computerObj = tableModel.getValueAt(selectedRow, 8);
        
        tfInsertRoll.setText(rollNo);
        tfInsertName.setText(name);
        tfInsertClass.setText(className);
        
        // Set subject marks
        tfMaths.setText(mathsObj.toString());
        tfScience.setText(scienceObj.toString());
        tfEnglish.setText(englishObj.toString());
        tfHistory.setText(historyObj.toString());
        tfGeography.setText(geographyObj.toString());
        tfComputer.setText(computerObj.toString());
        
        setStatus("Selected student: " + name + ". Edit details and click Save to update.", false);
    }
    
    private void deleteSelectedStudent() {
        int selectedRow = resultTable.getSelectedRow();
        if (selectedRow == -1) {
            showErrorMessage("Please select a student to delete");
            return;
        }
        
        String rollNo = tableModel.getValueAt(selectedRow, 0).toString();
        String name = tableModel.getValueAt(selectedRow, 1).toString();
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete student " + name + " (" + rollNo + ")?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);
            
            // Get student ID
            int studentId = getStudentId(conn, rollNo);
            if (studentId == -1) {
                showErrorMessage("Student not found in database");
                return;
            }
            
            // Delete marks first (foreign key constraint)
            String deleteMarksSql = "DELETE FROM marks WHERE student_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteMarksSql)) {
                pstmt.setInt(1, studentId);
                pstmt.executeUpdate();
            }
            
            // Delete student
            String deleteStudentSql = "DELETE FROM students WHERE student_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteStudentSql)) {
                pstmt.setInt(1, studentId);
                pstmt.executeUpdate();
            }
            
            conn.commit();
            tableModel.removeRow(selectedRow);
            setStatus("✅ Student " + name + " has been deleted", false);
            
        } catch (SQLException e) {
            showErrorMessage("Database error: " + e.getMessage());
        }
    }
    private void clearInsertForm() {
        tfInsertRoll.setText("");
        tfInsertName.setText("");
        tfInsertClass.setText("");
        cbInsertSubject.setSelectedIndex(0);
        // Clear subject-specific fields
        tfMaths.setText("");
        tfScience.setText("");
        tfEnglish.setText("");
        tfHistory.setText("");
        tfGeography.setText("");
        tfComputer.setText("");
    }
    private void setStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? Color.RED : Color.BLUE);
    }
    
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        setStatus(message, true);
    }
    
    // Helper class to store student records for ranking
    private static class StudentRecord {
        private final int studentId;
        private final String rollNumber;
        private final String name;
        private final String className;
        private final int mathMarks;
        private final int scienceMarks;
        private final int englishMarks;
        private final int historyMarks;
        private final int geographyMarks;
        private final int computerMarks;
        private final int totalMarks;
        private final double percentage;
        
        public StudentRecord(
                int studentId, String rollNumber, String name, String className,
                int mathMarks, int scienceMarks, int englishMarks,
                int historyMarks, int geographyMarks, int computerMarks,
                int totalMarks, double percentage) {
            this.studentId = studentId;
            this.rollNumber = rollNumber;
            this.name = name;
            this.className = className;
            this.mathMarks = mathMarks;
            this.scienceMarks = scienceMarks;
            this.englishMarks = englishMarks;
            this.historyMarks = historyMarks;
            this.geographyMarks = geographyMarks;
            this.computerMarks = computerMarks;
            this.totalMarks = totalMarks;
            this.percentage = percentage;
        }
        
        public String getRollNumber() { return rollNumber; }
        public String getName() { return name; }
        public String getClassName() { return className; }
        public int getMathMarks() { return mathMarks; }
        public int getScienceMarks() { return scienceMarks; }
        public int getEnglishMarks() { return englishMarks; }
        public int getHistoryMarks() { return historyMarks; }
        public int getGeographyMarks() { return geographyMarks; }
        public int getComputerMarks() { return computerMarks; }
        public int getTotalMarks() { return totalMarks; }
        public double getPercentage() { return percentage; }
    }
    
    public static void main(String[] args) {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Set Nimbus look and feel for better UI appearance
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, 
                "MySQL JDBC Driver not found. Please add MySQL Connector/J to your classpath.",
                "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (Exception e) {
            // Fallback to default look and feel
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                // Ignore
            }
        }
        
        // Launch the application on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                new EnhancedMarksReport();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, 
                    "Error initializing application: " + e.getMessage(),
                    "Application Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}
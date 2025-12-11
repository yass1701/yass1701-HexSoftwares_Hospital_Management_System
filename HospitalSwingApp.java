import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class HospitalSwingApp extends JFrame {

    private static final String URL = "jdbc:postgresql://localhost:5432/hospital_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Yash@1701";

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField nameField;
    private JTextField ageField;
    private JComboBox<String> genderBox;
    private JTextField searchField;
    private JLabel statusLabel;

    public HospitalSwingApp() {
        setTitle("Hospital Management System");
        setSize(900, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.EAST);
        add(buildStatusBar(), BorderLayout.SOUTH);

        refreshTable();
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

        JButton refreshBtn = new JButton("Refresh");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton resetBtn = new JButton("Reset All");

        refreshBtn.addActionListener(e -> refreshTable());
        deleteBtn.addActionListener(e -> deleteSelected());
        resetBtn.addActionListener(e -> resetDatabase());

        bar.add(refreshBtn);
        bar.add(deleteBtn);
        bar.add(resetBtn);

        bar.add(new JLabel("Search:"));
        searchField = new JTextField(14);
        bar.add(searchField);
        JButton searchBtn = new JButton("Go");
        searchBtn.addActionListener(e -> searchByName());
        JButton clearSearch = new JButton("Clear");
        clearSearch.addActionListener(e -> {
            searchField.setText("");
            refreshTable();
        });
        bar.add(searchBtn);
        bar.add(clearSearch);

        JButton exitBtn = new JButton("Exit");
        exitBtn.addActionListener(e -> System.exit(0));
        bar.add(exitBtn);

        return bar;
    }

    private JPanel buildTablePanel() {
        String[] columns = {"ID", "Name", "Age", "Gender"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> populateFormFromSelection());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void refreshTable() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM patients ORDER BY id ASC")) {

            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("gender")
                });
            }
            setStatus("Loaded patients.");
        } catch (Exception ex) {
            showError("Load failed: " + ex.getMessage());
        }
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(260, 0));

        nameField = new JTextField();
        ageField = new JTextField();
        genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});

        panel.add(new JLabel("Name"));
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(2));

        panel.add(new JLabel("Age"));
        panel.add(ageField);
        panel.add(Box.createVerticalStrut(8));

        panel.add(new JLabel("Gender"));
        panel.add(genderBox);
        panel.add(Box.createVerticalStrut(16));

        JButton saveBtn = new JButton("Save (Add/Update)");
        JButton clearBtn = new JButton("Clear Form");
        saveBtn.addActionListener(e -> savePatient());
        clearBtn.addActionListener(e -> clearForm());
        panel.add(saveBtn);
        panel.add(Box.createVerticalStrut(6));
        panel.add(clearBtn);

        return panel;
    }

    private void populateFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        ageField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        genderBox.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 3)));
    }

    private void clearForm() {
        nameField.setText("");
        ageField.setText("");
        genderBox.setSelectedIndex(0);
        table.clearSelection();
        setStatus("Form cleared.");
    }

    private void savePatient() {
        String name = nameField.getText().trim();
        String ageStr = ageField.getText().trim();
        String gender = (String) genderBox.getSelectedItem();

        if (name.isEmpty() || ageStr.isEmpty()) {
            showWarn("Please fill all fields.");
            return;
        }
        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException ex) {
            showWarn("Age must be a number.");
            return;
        }

        int selectedRow = table.getSelectedRow();
        boolean updating = selectedRow >= 0;
        Integer id = updating ? (Integer) tableModel.getValueAt(selectedRow, 0) : null;

        try (Connection conn = getConnection()) {
            if (!updating || !name.equals(String.valueOf(tableModel.getValueAt(selectedRow, 1)))) {
                try (PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM patients WHERE name = ?")) {
                    checkStmt.setString(1, name);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        showWarn("Patient '" + name + "' already exists.");
                        return;
                    }
                }
            }

            if (updating) {
                try (PreparedStatement pstmt = conn.prepareStatement("UPDATE patients SET name=?, age=?, gender=? WHERE id=?")) {
                    pstmt.setString(1, name);
                    pstmt.setInt(2, age);
                    pstmt.setString(3, gender);
                    pstmt.setInt(4, id);
                    int rows = pstmt.executeUpdate();
                    if (rows > 0) setStatus("Updated patient ID " + id);
                    else showWarn("ID not found.");
                }
            } else {
                try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO patients (name, age, gender) VALUES (?, ?, ?)")) {
                    pstmt.setString(1, name);
                    pstmt.setInt(2, age);
                    pstmt.setString(3, gender);
                    pstmt.executeUpdate();
                    setStatus("Added patient.");
                }
            }
            refreshTable();
            clearForm();
        } catch (Exception ex) {
            showError("Save failed: " + ex.getMessage());
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarn("Select a patient to delete.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected patient?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM patients WHERE id=?")) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                setStatus("Deleted patient ID " + id);
                refreshTable();
                clearForm();
            } else {
                showWarn("ID not found.");
            }
        } catch (Exception ex) {
            showError("Delete failed: " + ex.getMessage());
        }
    }

    private void resetDatabase() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "WARNING: This will delete ALL patients and reset ID to 1.\nAre you sure?",
                "Reset Database", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

                stmt.execute("TRUNCATE TABLE patients RESTART IDENTITY");
                setStatus("Database reset. Next ID will be 1.");
                refreshTable();
                clearForm();
            } catch (Exception ex) {
                showError("Reset failed: " + ex.getMessage());
            }
        }
    }

    private void searchByName() {
        String fragment = searchField.getText() == null ? "" : searchField.getText().trim();
        if (fragment.isEmpty()) {
            refreshTable();
            return;
        }
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM patients WHERE name ILIKE ? ORDER BY id")) {
            pstmt.setString(1, "%" + fragment + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                tableModel.setRowCount(0);
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    tableModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("age"),
                            rs.getString("gender")
                    });
                }
                if (any) setStatus("Search results for '" + fragment + "'");
                else setStatus("No patients found for '" + fragment + "'");
            }
        } catch (Exception ex) {
            showError("Search failed: " + ex.getMessage());
        }
    }

    private JPanel buildStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        statusLabel = new JLabel("Ready.");
        panel.add(statusLabel, BorderLayout.CENTER);
        return panel;
    }

    private void setStatus(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private Connection getConnection() throws Exception {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    } 

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HospitalSwingApp().setVisible(true));
    }
}
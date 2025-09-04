import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

//creating main class
public class PharmacyManagementSystem {
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db"; //initialze jdbc connection
    private Connection connection;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new PharmacyManagementSystem().initializeSystem();
        });
    }

    public void initializeSystem() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            initializeDatabase();
            new LoginFrame();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
//creating SQL Code within the same java file so its easier for anyone to run it
    //Used AI to generate the part cause i was lazy
    private void initializeDatabase() throws SQLException {
        Statement stmt = connection.createStatement();

        // Create usrs table
        stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL)");

        // Create medicines table
        stmt.execute("CREATE TABLE IF NOT EXISTS medicines (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "batch_number TEXT NOT NULL," +
                "expiry_date DATE NOT NULL," +
                "quantity INTEGER NOT NULL," +
                "price REAL NOT NULL)");

        // Create sales table
        stmt.execute("CREATE TABLE IF NOT EXISTS sales (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "medicine_id INTEGER," +
                "medicine_name TEXT," +
                "quantity INTEGER," +
                "price_per_unit REAL," +
                "total_amount REAL," +
                "sale_date DATE," +
                "FOREIGN KEY(medicine_id) REFERENCES medicines(id))");

        // Insert default user if not exists
        PreparedStatement pstmt = connection.prepareStatement(
                "INSERT OR IGNORE INTO users (username, password) VALUES (?, ?)");
        pstmt.setString(1, "admin");
        pstmt.setString(2, "admin123");
        pstmt.executeUpdate();

        // Insert sample medicines if table is empty
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM medicines");
        if (rs.next() && rs.getInt(1) == 0) {
            insertSampleData();
        }
    }

    private void insertSampleData() throws SQLException {
        String[] sampleMedicines = {
                "Paracetamol,BATCH001,2025-12-31,100,5.50",
                "Aspirin,BATCH002,2025-06-30,75,8.25",
                "Ibuprofen,BATCH003,2025-09-15,50,12.00",
                "Amoxicillin,BATCH004,2025-03-20,25,15.75"
        };

        PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO medicines (name, batch_number, expiry_date, quantity, price) VALUES (?, ?, ?, ?, ?)");

        for (String medicine : sampleMedicines) {
            String[] parts = medicine.split(",");
            pstmt.setString(1, parts[0]);
            pstmt.setString(2, parts[1]);
            pstmt.setString(3, parts[2]);
            pstmt.setInt(4, Integer.parseInt(parts[3]));
            pstmt.setDouble(5, Double.parseDouble(parts[4]));
            pstmt.executeUpdate();
        }
    }

    // Create login frame and give properties
    class LoginFrame extends JFrame {
        private JTextField usernameField;
        private JPasswordField passwordField;

        public LoginFrame() {
            setTitle("Pharmacy Management System - Login");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(400, 300);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());

            // Create main panel
            JPanel mainPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            // Title
            JLabel titleLabel = new JLabel("Pharmacy Management System", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(20, 20, 20, 20);
            mainPanel.add(titleLabel, gbc);

            // Username
            gbc.gridwidth = 1; gbc.insets = new Insets(10, 20, 5, 5);
            gbc.gridx = 0; gbc.gridy = 1;
            mainPanel.add(new JLabel("Username:"), gbc);

            usernameField = new JTextField(15);
            gbc.gridx = 1; gbc.gridy = 1; gbc.insets = new Insets(10, 5, 5, 20);
            mainPanel.add(usernameField, gbc);

            // Password
            gbc.gridx = 0; gbc.gridy = 2; gbc.insets = new Insets(5, 20, 10, 5);
            mainPanel.add(new JLabel("Password:"), gbc);

            passwordField = new JPasswordField(15);
            gbc.gridx = 1; gbc.gridy = 2; gbc.insets = new Insets(5, 5, 10, 20);
            mainPanel.add(passwordField, gbc);

            // Login buttons
            JButton loginButton = new JButton("Login");
            loginButton.setPreferredSize(new Dimension(100, 30));
            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.insets = new Insets(20, 20, 20, 20); //generated by AI cause dimensions are hard
            mainPanel.add(loginButton, gbc);

            loginButton.addActionListener(e -> authenticate());

            // Add Enter key listener
            KeyListener enterListener = new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        authenticate();
                    }
                }
            };
            usernameField.addKeyListener(enterListener);
            passwordField.addKeyListener(enterListener);

            add(mainPanel, BorderLayout.CENTER);

            // Info panel
            JPanel infoPanel = new JPanel();
            infoPanel.add(new JLabel("Default login: admin / admin123"));
            add(infoPanel, BorderLayout.SOUTH);

            setVisible(true);
        }

        private void authenticate() {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            //basic authenticator using prepared statements
            try {
                PreparedStatement pstmt = connection.prepareStatement(
                        "SELECT * FROM users WHERE username = ? AND password = ?");
                pstmt.setString(1, username);
                pstmt.setString(2, password);

                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    dispose();
                    new MainDashboard();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials!");
                    passwordField.setText("");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Authentication error: " + e.getMessage());
            }
        }
    }

    // Main Dashboard basic design
    class MainDashboard extends JFrame {
        private JTabbedPane tabbedPane;
        private MedicinePanel medicinePanel;
        private SalesPanel salesPanel;
        private ReportsPanel reportsPanel;

        public MainDashboard() {
            setTitle("Pharmacy Management System - Dashboard");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(1000, 600);
            setLocationRelativeTo(null);

            // Create menu bar here
            JMenuBar menuBar = new JMenuBar();
            JMenu systemMenu = new JMenu("System");
            JMenuItem logoutItem = new JMenuItem("Logout");
            logoutItem.addActionListener(e -> {
                dispose(); //delete function
                new LoginFrame();
            });
            systemMenu.add(logoutItem);
            menuBar.add(systemMenu);
            setJMenuBar(menuBar);

            // Create tabbed pane
            tabbedPane = new JTabbedPane();

            medicinePanel = new MedicinePanel();
            salesPanel = new SalesPanel();
            reportsPanel = new ReportsPanel();

            tabbedPane.addTab("Medicine Management", medicinePanel);
            tabbedPane.addTab("Sales", salesPanel);
            tabbedPane.addTab("Reports", reportsPanel);

            add(tabbedPane);
            setVisible(true);
        }
    }

    // Medicine Panel
    class MedicinePanel extends JPanel {
        private JTable medicineTable;
        private DefaultTableModel tableModel;
        private JTextField searchField;
        private JTextField nameField, batchField, expiryField, quantityField, priceField;

        public MedicinePanel() {
            setLayout(new BorderLayout());

            // Top panel for search and add
            JPanel topPanel = new JPanel(new BorderLayout());

            // Search panel
            JPanel searchPanel = new JPanel(new FlowLayout());
            searchPanel.add(new JLabel("Search:"));
            searchField = new JTextField(20);
            searchPanel.add(searchField);
            JButton searchButton = new JButton("Search");
            searchButton.addActionListener(e -> searchMedicines());
            searchPanel.add(searchButton);

            topPanel.add(searchPanel, BorderLayout.WEST);

            // Adding our panels to form
            JPanel addPanel = new JPanel(new GridLayout(2, 6, 5, 5));
            addPanel.setBorder(BorderFactory.createTitledBorder("Add/Update Medicine"));

            addPanel.add(new JLabel("Name:"));
            nameField = new JTextField();
            addPanel.add(nameField);

            addPanel.add(new JLabel("Batch:"));
            batchField = new JTextField();
            addPanel.add(batchField);

            addPanel.add(new JLabel("Expiry (YYYY-MM-DD):"));
            expiryField = new JTextField();
            addPanel.add(expiryField);

            addPanel.add(new JLabel("Quantity:"));
            quantityField = new JTextField();
            addPanel.add(quantityField);

            addPanel.add(new JLabel("Price:"));
            priceField = new JTextField();
            addPanel.add(priceField);

            JButton addButton = new JButton("Add Medicine");
            addButton.addActionListener(e -> addMedicine());
            addPanel.add(addButton);

            topPanel.add(addPanel, BorderLayout.CENTER);
            add(topPanel, BorderLayout.NORTH);

            // Medicine table
            String[] columns = {"ID", "Name", "Batch", "Expiry Date", "Quantity", "Price"};
            tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            medicineTable = new JTable(tableModel);
            medicineTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            medicineTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        editSelectedMedicine();
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(medicineTable);
            add(scrollPane, BorderLayout.CENTER);

            // Bottom panel for actions
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton updateButton = new JButton("Update Selected");
            updateButton.addActionListener(e -> updateMedicine());
            buttonPanel.add(updateButton);

            JButton deleteButton = new JButton("Delete Selected");
            deleteButton.addActionListener(e -> deleteMedicine());
            buttonPanel.add(deleteButton);

            JButton refreshButton = new JButton("Refresh");
            refreshButton.addActionListener(e -> loadMedicines());
            buttonPanel.add(refreshButton);

            JButton lowStockButton = new JButton("Check Low Stock");
            lowStockButton.addActionListener(e -> checkLowStock());
            buttonPanel.add(lowStockButton);

            add(buttonPanel, BorderLayout.SOUTH);

            loadMedicines();
        }

        private void loadMedicines() {
            try {
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM medicines ORDER BY name");

                tableModel.setRowCount(0);
                while (rs.next()) {
                    Object[] row = {
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("batch_number"),
                            rs.getString("expiry_date"),
                            rs.getInt("quantity"),
                            rs.getDouble("price")
                    };
                    tableModel.addRow(row);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Errr loading medicines: " + e.getMessage());
            }
        }

        private void searchMedicines() {
            String searchTerm = searchField.getText().trim();
            if (searchTerm.isEmpty()) {
                loadMedicines();
                return;
            }

            try {
                PreparedStatement pstmt = connection.prepareStatement(
                        "SELECT * FROM medicines WHERE name LIKE ? OR batch_number LIKE ? ORDER BY name");
                String pattern = "%" + searchTerm + "%";
                pstmt.setString(1, pattern);
                pstmt.setString(2, pattern);

                ResultSet rs = pstmt.executeQuery();
                tableModel.setRowCount(0);

                while (rs.next()) {
                    Object[] row = {
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("batch_number"),
                            rs.getString("expiry_date"),
                            rs.getInt("quantity"),
                            rs.getDouble("price")
                    };
                    tableModel.addRow(row);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "The error is: " + e.getMessage());
            }
        }

        private void addMedicine() {
            try {
                String name = nameField.getText().trim();
                String batch = batchField.getText().trim();
                String expiry = expiryField.getText().trim();
                String quantityStr = quantityField.getText().trim();
                String priceStr = priceField.getText().trim();

                if (name.isEmpty() || batch.isEmpty() || expiry.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill all fields!");
                    return;
                }

                int quantity = Integer.parseInt(quantityStr);
                double price = Double.parseDouble(priceStr);

                PreparedStatement pstmt = connection.prepareStatement(
                        "INSERT INTO medicines (name, batch_number, expiry_date, quantity, price) VALUES (?, ?, ?, ?, ?)");
                pstmt.setString(1, name);
                pstmt.setString(2, batch);
                pstmt.setString(3, expiry);
                pstmt.setInt(4, quantity);
                pstmt.setDouble(5, price);

                pstmt.executeUpdate();
                clearFields();
                loadMedicines();
                JOptionPane.showMessageDialog(this, "Medicine added successfully!");

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for quantity and price!");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error adding medicine: " + e.getMessage());
            }
        }
//edit med method
        private void editSelectedMedicine() {
            int selectedRow = medicineTable.getSelectedRow();
            if (selectedRow == -1) return;

            nameField.setText((String) tableModel.getValueAt(selectedRow, 1));
            batchField.setText((String) tableModel.getValueAt(selectedRow, 2));
            expiryField.setText((String) tableModel.getValueAt(selectedRow, 3));
            quantityField.setText(String.valueOf(tableModel.getValueAt(selectedRow, 4)));
            priceField.setText(String.valueOf(tableModel.getValueAt(selectedRow, 5)));
        }
 //update method when something is added
        private void updateMedicine() {
            int selectedRow = medicineTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a medicine to update!");
                return;
            }

            try {
                int id = (Integer) tableModel.getValueAt(selectedRow, 0);
                String name = nameField.getText().trim();
                String batch = batchField.getText().trim();
                String expiry = expiryField.getText().trim();
                int quantity = Integer.parseInt(quantityField.getText().trim());
                double price = Double.parseDouble(priceField.getText().trim());

                PreparedStatement pstmt = connection.prepareStatement(
                        "UPDATE medicines SET name=?, batch_number=?, expiry_date=?, quantity=?, price=? WHERE id=?");
                pstmt.setString(1, name);
                pstmt.setString(2, batch);
                pstmt.setString(3, expiry);
                pstmt.setInt(4, quantity);
                pstmt.setDouble(5, price);
                pstmt.setInt(6, id);

                pstmt.executeUpdate();
                clearFields();
                loadMedicines();
                JOptionPane.showMessageDialog(this, "Medicine updated successfully!");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error updating medicine: " + e.getMessage());
            }
        }
//create delete method
        private void deleteMedicine() {
            int selectedRow = medicineTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a medicine to delete!");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this medicine?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int id = (Integer) tableModel.getValueAt(selectedRow, 0);
                    PreparedStatement pstmt = connection.prepareStatement("DELETE FROM medicines WHERE id=?");
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();

                    loadMedicines();
                    JOptionPane.showMessageDialog(this, "Medicine deleted successfully!");

                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error deleting medicine: " + e.getMessage());
                }
            }
        }

        private void checkLowStock() {
            String thresholdStr = JOptionPane.showInputDialog(this,
                    "Enter low stock threshold:", "10");
            if (thresholdStr == null) return;

            try {
                int threshold = Integer.parseInt(thresholdStr);
                PreparedStatement pstmt = connection.prepareStatement(
                        "SELECT * FROM medicines WHERE quantity <= ? ORDER BY quantity");
                pstmt.setInt(1, threshold);

                ResultSet rs = pstmt.executeQuery();
                StringBuilder lowStockItems = new StringBuilder();

                while (rs.next()) {
                    lowStockItems.append(rs.getString("name"))
                            .append(" (Qty: ").append(rs.getInt("quantity")).append(")\n");
                }

                if (lowStockItems.length() > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Low Stock Items:\n" + lowStockItems.toString(),
                            "Low Stock Alert", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No low stock items found!");
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error checking low stock: " + e.getMessage());
            }
        }
//method to clear fields that has been disposed of
        private void clearFields() {
            nameField.setText("");
            batchField.setText("");
            expiryField.setText("");
            quantityField.setText("");
            priceField.setText("");
        }
    }

    // Sales Panel
    class SalesPanel extends JPanel {
        private JComboBox<String> medicineCombo;
        private JTextField quantityField;
        private JLabel priceLabel, totalLabel;
        private JTable salesTable;
        private DefaultTableModel salesTableModel;
        private List<Medicine> medicines;

        public SalesPanel() {
            setLayout(new BorderLayout());

            // Sales form panel
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createTitledBorder("Make Sale"));
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.insets = new Insets(5, 5, 5, 5);

            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Medicine:"), gbc);

            medicineCombo = new JComboBox<>();
            medicineCombo.addActionListener(e -> updatePriceLabel());
            gbc.gridx = 1; gbc.gridy = 0;
            formPanel.add(medicineCombo, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Quantity:"), gbc);

            quantityField = new JTextField(10);
            quantityField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    calculateTotal();
                }
            });
            gbc.gridx = 1; gbc.gridy = 1;
            formPanel.add(quantityField, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Price per unit:"), gbc);

            priceLabel = new JLabel("K0.00");
            gbc.gridx = 1; gbc.gridy = 2;
            formPanel.add(priceLabel, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Total:"), gbc);

            totalLabel = new JLabel("K0.00");
            totalLabel.setFont(new Font("Arial", Font.BOLD, 12));
            gbc.gridx = 1; gbc.gridy = 3;
            formPanel.add(totalLabel, gbc);

            JButton sellButton = new JButton("Complete Sale");
            sellButton.addActionListener(e -> completeSale());
            gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
            formPanel.add(sellButton, gbc);

            add(formPanel, BorderLayout.NORTH);

            // Sales history table
            String[] columns = {"ID", "Medicine", "Quantity", "Price/Unit", "Total", "Date"}; //Stored in an array
            salesTableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            salesTable = new JTable(salesTableModel);
            JScrollPane scrollPane = new JScrollPane(salesTable);
            scrollPane.setBorder(BorderFactory.createTitledBorder("Sales History"));
            add(scrollPane, BorderLayout.CENTER);

            JButton refreshButton = new JButton("Refresh");
            refreshButton.addActionListener(e -> {
                loadMedicines();
                loadSalesHistory();
            });
            add(refreshButton, BorderLayout.SOUTH);

            loadMedicines();
            loadSalesHistory();
        }

        private void loadMedicines() {
            try {
                medicines = new ArrayList<>();
                medicineCombo.removeAllItems();

                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM medicines WHERE quantity > 0 ORDER BY name");

                while (rs.next()) {
                    Medicine med = new Medicine();
                    med.id = rs.getInt("id");
                    med.name = rs.getString("name");
                    med.quantity = rs.getInt("quantity");
                    med.price = rs.getDouble("price");
                    medicines.add(med);

                    medicineCombo.addItem(med.name + " (Stock: " + med.quantity + ")");
                }

                updatePriceLabel();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error loading medicins: " + e.getMessage());
            }
        }

        private void updatePriceLabel() {
            if (medicineCombo.getSelectedIndex() >= 0 && !medicines.isEmpty()) {
                Medicine selected = medicines.get(medicineCombo.getSelectedIndex());
                priceLabel.setText(String.format("K%.2f", selected.price));
                calculateTotal();
            }
        }

        private void calculateTotal() {
            try {
                if (medicineCombo.getSelectedIndex() >= 0 && !medicines.isEmpty()) {
                    int quantity = Integer.parseInt(quantityField.getText().trim());
                    Medicine selected = medicines.get(medicineCombo.getSelectedIndex());
                    double total = quantity * selected.price;
                    totalLabel.setText(String.format("K%.2f",  total));
                }
            } catch (NumberFormatException e) {
                totalLabel.setText("K0.00");
            }
        }

        private void completeSale() {
            try {
                if (medicineCombo.getSelectedIndex() < 0) {
                    JOptionPane.showMessageDialog(this, "Please select a medicine!");
                    return;
                }

                int quantity = Integer.parseInt(quantityField.getText().trim());
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid quantity!");
                    return;
                }

                Medicine selected = medicines.get(medicineCombo.getSelectedIndex());

                if (quantity > selected.quantity) {
                    JOptionPane.showMessageDialog(this, "Insufficient stock! Available: " + selected.quantity);
                    return;
                }

                double totalAmount = quantity * selected.price;

                // Record sale
                PreparedStatement pstmt = connection.prepareStatement(
                        "INSERT INTO sales (medicine_id, medicine_name, quantity, price_per_unit, total_amount, sale_date) " +
                                "VALUES (?, ?, ?, ?, ?, ?)");
                pstmt.setInt(1, selected.id);
                pstmt.setString(2, selected.name);
                pstmt.setInt(3, quantity);
                pstmt.setDouble(4, selected.price);
                pstmt.setDouble(5, totalAmount);
                pstmt.setString(6, LocalDate.now().toString());
                pstmt.executeUpdate();

                // Update medicine quantity
                pstmt = connection.prepareStatement("UPDATE medicines SET quantity = quantity - ? WHERE id = ?");
                pstmt.setInt(1, quantity);
                pstmt.setInt(2, selected.id);
                pstmt.executeUpdate();

                quantityField.setText("");
                totalLabel.setText("$0.00");
                loadMedicines();
                loadSalesHistory();

                JOptionPane.showMessageDialog(this,
                        String.format("Sale completed!\nTotal: K%.2f", totalAmount));

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid quantity!");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error completing sale: " + e.getMessage());
            }
        }

        private void loadSalesHistory() {
            try {
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM sales ORDER BY sale_date DESC, id DESC LIMIT 100");

                salesTableModel.setRowCount(0);
                while (rs.next()) {
                    Object[] row = {
                            rs.getInt("id"),
                            rs.getString("medicine_name"),
                            rs.getInt("quantity"),
                            String.format("K%.2f", rs.getDouble("price_per_unit")),
                            String.format("K%.2f", rs.getDouble("total_amount")),
                            rs.getString("sale_date")
                    };
                    salesTableModel.addRow(row);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error loading sales history: " + e.getMessage());
            }
        }
    }

    // Reports panel class by extending JPanel
    class ReportsPanel extends JPanel {
        private JTextArea reportArea;

        public ReportsPanel() {
            setLayout(new BorderLayout());

            JPanel buttonPanel = new JPanel(new FlowLayout());

            JButton dailyReportButton = new JButton("Daily Sales Report");
            dailyReportButton.addActionListener(e -> generateDailyReport());
            buttonPanel.add(dailyReportButton);

            JButton weeklyReportButton = new JButton("Weekly Sales Report");
            weeklyReportButton.addActionListener(e -> generateWeeklyReport());
            buttonPanel.add(weeklyReportButton);

            JButton monthlyReportButton = new JButton("Monthly Sales Report");
            monthlyReportButton.addActionListener(e -> generateMonthlyReport());
            buttonPanel.add(monthlyReportButton);

            JButton inventoryReportButton = new JButton("Inventory Report");
            inventoryReportButton.addActionListener(e -> generateInventoryReport());
            buttonPanel.add(            inventoryReportButton);

            JButton expiredReportButton = new JButton("Expired Medicines Report");
            expiredReportButton.addActionListener(e -> generateExpiredMedicinesReport());
            buttonPanel.add(expiredReportButton);

            add(buttonPanel, BorderLayout.NORTH);

            reportArea = new JTextArea();
            reportArea.setEditable(false);
            reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JScrollPane scrollPane = new JScrollPane(reportArea);
            scrollPane.setBorder(BorderFactory.createTitledBorder("Report Output"));
            add(scrollPane, BorderLayout.CENTER);

            JPanel exportPanel = new JPanel(new FlowLayout());
            JButton exportButton = new JButton("Export Report to File");
            exportButton.addActionListener(e -> exportReport());
            exportPanel.add(exportButton);

            JButton clearButton = new JButton("Clear Report");
            clearButton.addActionListener(e -> reportArea.setText(""));
            exportPanel.add(clearButton);

            add(exportPanel, BorderLayout.SOUTH);
        }

        private void generateDailyReport() {
            try {
                String today = LocalDate.now().toString();
                PreparedStatement pstmt = connection.prepareStatement(
                        "SELECT * FROM sales WHERE sale_date = ? ORDER BY id");
                pstmt.setString(1, today);

                ResultSet rs = pstmt.executeQuery();
                generateSalesReport(rs, "Daily Sales Report - " + today);

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error generating daily report: " + e.getMessage());
            }
        }

        private void generateWeeklyReport() {
            try {
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(7);

                PreparedStatement pstmt = connection.prepareStatement(
                        "SELECT * FROM sales WHERE sale_date BETWEEN ? AND ? ORDER BY sale_date, id");
                pstmt.setString(1, startDate.toString());
                pstmt.setString(2, endDate.toString());

                ResultSet rs = pstmt.executeQuery();
                generateSalesReport(rs, "Weekly Sales Report - " + startDate + " to " + endDate);

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error generating weekly report: " + e.getMessage());
            }
        }

        private void generateMonthlyReport() {
            try {
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(30);

                PreparedStatement pstmt = connection.prepareStatement(
                        "SELECT * FROM sales WHERE sale_date BETWEEN ? AND ? ORDER BY sale_date, id");
                pstmt.setString(1, startDate.toString());
                pstmt.setString(2, endDate.toString());

                ResultSet rs = pstmt.executeQuery();
                generateSalesReport(rs, "Monthly sales report - " + startDate + " to " + endDate);

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error generating monthly report: " + e.getMessage());
            }
        }

        private void generateSalesReport(ResultSet rs, String title) throws SQLException {
            StringBuilder report = new StringBuilder();
            report.append(title).append("\n");
            report.append("=".repeat(80)).append("\n\n");

            double totalRevenue = 0;
            int totalItems = 0;

            report.append(String.format("%-20s %-10s %-12s %-12s %-12s\n",
                    "Medicine", "Quantity", "Price/Unit", "Total", "Date"));
            report.append("-".repeat(80)).append("\n");

            while (rs.next()) {
                String medicine = rs.getString("medicine_name");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price_per_unit");
                double total = rs.getDouble("total_amount");
                String date = rs.getString("sale_date");

                report.append(String.format("%-20s %-10d $%-11.2f $%-11.2f %-12s\n",
                        medicine, quantity, price, total, date));

                totalRevenue += total;
                totalItems += quantity;
            }

            report.append("\n").append("=".repeat(80)).append("\n");
            report.append(String.format("Total Items Sold: %d\n", totalItems));
            report.append(String.format("Total Revenue: K%.2f\n", totalRevenue));
            report.append("=".repeat(80));

            reportArea.setText(report.toString());
        }

        private void generateInventoryReport() {
            try {
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM medicines ORDER BY name");

                StringBuilder report = new StringBuilder();
                report.append("Inventory Report\n");
                report.append("=".repeat(80)).append("\n\n");

                report.append(String.format("%-20s %-15s %-15s %-10s %-10s\n",
                        "Medicine", "Batch Number", "Expiry Date", "Quantity", "Price"));
                report.append("-".repeat(80)).append("\n");

                int totalItems = 0;
                double totalValue = 0;

                while (rs.next()) {
                    String name = rs.getString("name");
                    String batch = rs.getString("batch_number");
                    String expiry = rs.getString("expiry_date");
                    int quantity = rs.getInt("quantity");
                    double price = rs.getDouble("price");

                    report.append(String.format("%-20s %-15s %-15s %-10d K%-9.2f\n",
                            name, batch, expiry, quantity, price));

                    totalItems += quantity;
                    totalValue += quantity * price;
                }

                report.append("\n").append("=".repeat(80)).append("\n");
                report.append(String.format("Total Items in Stock: %d\n", totalItems));
                report.append(String.format("Total Inventory Value: K%.2f\n", totalValue));
                report.append("=".repeat(80));

                reportArea.setText(report.toString());

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error generating inventory report: " + e.getMessage());
            }
        }

        private void generateExpiredMedicinesReport() {
            try {
                String today = LocalDate.now().toString();
                PreparedStatement pstmt = connection.prepareStatement(
                        "SELECT * FROM medicines WHERE expiry_date < ? ORDER BY expiry_date");
                pstmt.setString(1, today);

                ResultSet rs = pstmt.executeQuery();

                StringBuilder report = new StringBuilder();
                report.append("Expired Medicines Report\n");
                report.append("=".repeat(80)).append("\n\n");

                report.append(String.format("%-20s %-15s %-15s %-10s %-10s\n",
                        "Medicine", "Batch Number", "Expiry Date", "Quantity", "Price"));
                report.append("-".repeat(80)).append("\n");

                int expiredItems = 0;
                double expiredValue = 0;
                boolean hasExpired = false;

                while (rs.next()) {
                    hasExpired = true;
                    String name = rs.getString("name");
                    String batch = rs.getString("batch_number");
                    String expiry = rs.getString("expiry_date");
                    int quantity = rs.getInt("quantity");
                    double price = rs.getDouble("price");

                    report.append(String.format("%-20s %-15s %-15s %-10d K%-9.2f\n",
                            name, batch, expiry, quantity, price));

                    expiredItems += quantity;
                    expiredValue += quantity * price;
                }

                if (!hasExpired) {
                    report.append("Expired Medicines Not Found!.\n");
                } else {
                    report.append("\n").append("=".repeat(80)).append("\n");
                    report.append(String.format("Total Expired Items: %d\n", expiredItems));
                    report.append(String.format("Total Value of Expired Stock: K%.2f\n", expiredValue));
                }

                report.append("=".repeat(80));
                reportArea.setText(report.toString());

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error generating expired medicines report: " + e.getMessage());
            }
        }

        private void exportReport() {
            if (reportArea.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No report to export!");
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Report");
            fileChooser.setSelectedFile(new java.io.File("pharmacy_report.txt"));

            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                try {
                    java.io.File file = fileChooser.getSelectedFile();
                    java.io.FileWriter writer = new java.io.FileWriter(file);
                    writer.write(reportArea.getText());
                    writer.close();
                    JOptionPane.showMessageDialog(this, "Report exported successfully to: " + file.getName());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error exporting report: " + e.getMessage());
                }
            }
        }
    }

    // Helper class for medicine data and allat shit
    class Medicine {
        int id;
        String name;
        int quantity;
        double price;
    }
}

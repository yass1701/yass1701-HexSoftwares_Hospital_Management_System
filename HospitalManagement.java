import java.sql.*;
import java.util.Scanner;

public class HospitalManagement {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("\n=== HOSPITAL MANAGEMENT SYSTEM ===");
            System.out.println("1. Add Patient");
            System.out.println("2. View Patients");
            System.out.println("3. Update Patient");
            System.out.println("4. Delete Patient");
            System.out.println("5. Search Patients by Name");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");
            
            int choice = scanner.nextInt();
            
            switch (choice) {
                case 1:
                    addPatient(scanner);
                    break;
                case 2:
                    viewPatients();
                    break;
                case 3:
                    updatePatient(scanner);
                    break;
                case 4:
                    deletePatient(scanner);
                    break;
                case 5:
                    searchPatientsByName(scanner);
                    break;
                case 6:
                    System.out.println("Exiting... Thank you!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // --- 1. ADD PATIENT ---
    private static void addPatient(Scanner scanner) {
        System.out.print("Enter Patient Name: ");
        scanner.nextLine(); // Fix for skipping input
        String name = scanner.nextLine();
        
        System.out.print("Enter Patient Age: ");
        int age = scanner.nextInt();
        
        System.out.print("Enter Patient Gender: ");
        String gender = scanner.next();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO patients (name, age, gender) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.setString(3, gender);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Patient added successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- 2. VIEW PATIENTS ---
    private static void viewPatients() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM patients ORDER BY id ASC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n+----+----------------------+-----+--------+");
            System.out.println("| ID | Name                 | Age | Gender |");
            System.out.println("+----+----------------------+-----+--------+");
            
            while (rs.next()) {
                System.out.printf("| %-2d | %-20s | %-3d | %-6s |\n", 
                    rs.getInt("id"), 
                    rs.getString("name"), 
                    rs.getInt("age"), 
                    rs.getString("gender"));
            }
            System.out.println("+----+----------------------+-----+--------+");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- 3. UPDATE PATIENT ---
    private static void updatePatient(Scanner scanner) {
        System.out.print("Enter Patient ID to Update: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        System.out.print("Enter New Name: ");
        String newName = scanner.nextLine();
        
        System.out.print("Enter New Age: ");
        int newAge = scanner.nextInt();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE patients SET name = ?, age = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, newName);
            pstmt.setInt(2, newAge);
            pstmt.setInt(3, id);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Patient updated successfully!");
            } else {
                System.out.println("❌ Patient not found with ID: " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- 4. DELETE PATIENT ---
    private static void deletePatient(Scanner scanner) {
        System.out.print("Enter Patient ID to Delete: ");
        int id = scanner.nextInt();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "DELETE FROM patients WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Patient deleted successfully!");
            } else {
                System.out.println("❌ Patient not found with ID: " + id);
            }
        } catch (SQLException d) {
            d.printStackTrace(); 
        }
    }

    // --- 5. SEARCH PATIENT ---
    private static void searchPatientsByName(Scanner scanner) {
        System.out.print("Enter Name to Search: ");
        scanner.nextLine(); // consume newline
        String nameQuery = scanner.nextLine();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // using ILIKE for case-insensitive search in PostgreSQL
            String query = "SELECT * FROM patients WHERE name ILIKE ?"; 
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, "%" + nameQuery + "%");
            
            ResultSet rs = pstmt.executeQuery();
            
            System.out.println("\n--- Search Results ---");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("ID: %d | Name: %s | Age: %d | Gender: %s\n", 
                    rs.getInt("id"), 
                    rs.getString("name"), 
                    rs.getInt("age"), 
                    rs.getString("gender"));
            }
            if (!found) {
                System.out.println("No patients found.");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
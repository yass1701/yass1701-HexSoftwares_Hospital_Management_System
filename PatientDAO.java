import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    public List<Patient> findAll() throws SQLException {
        String sql = "SELECT id, name, age, gender FROM patients ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Patient> patients = new ArrayList<>();
            while (rs.next()) {
                patients.add(mapRow(rs));
            }
            return patients;
        }
    }

    public List<Patient> searchByName(String fragment) throws SQLException {
        String sql = "SELECT id, name, age, gender FROM patients WHERE LOWER(name) LIKE LOWER(?) ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + fragment + "%");
            try (ResultSet rs = ps.executeQuery()) {
                List<Patient> patients = new ArrayList<>();
                while (rs.next()) {
                    patients.add(mapRow(rs));
                }
                return patients;
            }
        }
    }

    public int insert(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients (name, age, gender) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patient.getName());
            ps.setInt(2, patient.getAge());
            ps.setString(3, patient.getGender());
            return ps.executeUpdate();
        }
    }

    public int update(Patient patient) throws SQLException {
        String sql = "UPDATE patients SET name = ?, age = ?, gender = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patient.getName());
            ps.setInt(2, patient.getAge());
            ps.setString(3, patient.getGender());
            ps.setInt(4, patient.getId());
            return ps.executeUpdate();
        }
    }

    public int delete(int id) throws SQLException {
        String sql = "DELETE FROM patients WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    private Patient mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        int age = rs.getInt("age");
        String gender = rs.getString("gender");
        return new Patient(id, name, age, gender);
    }
}

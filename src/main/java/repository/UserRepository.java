package repository;

import db.ConnectionManager;
import model.User;
import java.sql.*;

public class UserRepository {

    private static final UserRepository instance = new UserRepository();
    private UserRepository() {}
    public static UserRepository getInstance() {
        return instance;
    }

    public void save(User user) {
        String sql = "INSERT INTO users (userId, password, name, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getName());
            pstmt.setString(4, user.getEmail());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("회원 저장 중 오류 발생", e);
        }
    }

    public User findById(String userId) {
        String sql = "SELECT * FROM users WHERE userId = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getString("userId"), rs.getString("password"),
                            rs.getString("name"), rs.getString("email"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("회원 조회 중 오류 발생", e);
        }
        return null;
    }
}
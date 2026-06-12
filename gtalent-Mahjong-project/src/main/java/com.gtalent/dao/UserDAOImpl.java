package com.gtalent.dao;

import com.gtalent.db.DBConnectionPool;
import com.gtalent.model.User;
import java.sql.*;
import java.time.LocalDateTime;

public class UserDAOImpl implements UserDAO {
    private DBConnectionPool connectionPool;

    public UserDAOImpl() {
        this.connectionPool = DBConnectionPool.getInstance();
    }

    @Override
    public boolean authenticateUser(String username, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = connectionPool.getConnection();
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            rs = stmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) connectionPool.returnConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean registerUser(String username, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = connectionPool.getConnection();
            String sql = "INSERT INTO users (username, password, score, wins, losses, registration_time) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setInt(3, 25000); // 預設分數
            stmt.setInt(4, 0);     // 勝場數
            stmt.setInt(5, 0);     // 敗場數
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) connectionPool.returnConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public User getUserByUsername(String username) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = connectionPool.getConnection();
            String sql = "SELECT * FROM users WHERE username = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setScore(rs.getInt("score"));
                user.setWins(rs.getInt("wins"));
                user.setLosses(rs.getInt("losses"));
                user.setRegistrationTime(rs.getTimestamp("registration_time").toLocalDateTime());
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) connectionPool.returnConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public void updateUserStats(User user, boolean isWin) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = connectionPool.getConnection();
            String sql = "UPDATE users SET wins = ?, losses = ? WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, isWin ? user.getWins() + 1 : user.getWins());
            stmt.setInt(2, isWin ? user.getLosses() : user.getLosses() + 1);
            stmt.setInt(3, user.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) connectionPool.returnConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateUserScore(User user, int scoreChange) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = connectionPool.getConnection();
            String sql = "UPDATE users SET score = ? WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, user.getScore() + scoreChange);
            stmt.setInt(2, user.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) connectionPool.returnConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void saveUser(User user) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = connectionPool.getConnection();
            String sql = "UPDATE users SET score = ?, wins = ?, losses = ? WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, user.getScore());
            stmt.setInt(2, user.getWins());
            stmt.setInt(3, user.getLosses());
            stmt.setInt(4, user.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) connectionPool.returnConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

package com.gtalent.dao;

import com.gtalent.model.User;
import java.sql.*;

public interface UserDAO {
    boolean authenticateUser(String username, String password);
    boolean registerUser(String username, String password);
    User getUserByUsername(String username);
    void updateUserStats(User user, boolean isWin);
    void updateUserScore(User user, int scoreChange);
    void saveUser(User user);
}

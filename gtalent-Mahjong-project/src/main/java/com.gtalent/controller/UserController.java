package com.gtalent.controller;

import com.gtalent.dao.UserDAO;
import com.gtalent.dao.UserDAOImpl;
import com.gtalent.model.User;
import com.gtalent.util.Validator;

import java.util.List;

public class UserController {
    private UserDAO userDAO;

    public UserController() {
        this.userDAO = new UserDAOImpl();
    }

    public boolean authenticateUser(String username, String password) {
        return userDAO.authenticateUser(username, password);
    }

    public boolean registerUser(String username, String password) {
        return userDAO.registerUser(username, password);
    }

    public User getUserByUsername(String username) {
        return userDAO.getUserByUsername(username);
    }

    public void updateUserStats(User user, boolean isWin) {
        userDAO.updateUserStats(user, isWin);
    }

    public void updateUserScore(User user, int scoreChange) {
        userDAO.updateUserScore(user, scoreChange);
    }

    public void saveUser(User user) {
        userDAO.saveUser(user);
    }

    public boolean canChi(List<String> playerHand, String discardedTile) {
        return Validator.checkEat(playerHand, discardedTile);
    }

    public boolean canPeng(List<String> playerHand, String discardedTile) {
        return Validator.checkPong(playerHand, discardedTile);
    }

    public boolean canGang(List<String> playerHand, String discardedTile) {
        return Validator.checkKong(playerHand, discardedTile);
    }

    public boolean canHu(List<String> playerHand, String discardedTile) {
        return Validator.checkWin(playerHand, discardedTile);
    }

    public List<List<String>> getChiCombinations(List<String> playerHand, String discardedTile) {
        return Validator.getChiCombinations(playerHand, discardedTile);
    }

    public ActionAvailability getActionAvailability(List<String> playerHand, String discardedTile) {
        return new ActionAvailability(
                canChi(playerHand, discardedTile),
                canPeng(playerHand, discardedTile),
                canGang(playerHand, discardedTile),
                canHu(playerHand, discardedTile)
        );
    }

    public static class ActionAvailability {
        private final boolean canChi;
        private final boolean canPeng;
        private final boolean canGang;
        private final boolean canHu;

        public ActionAvailability(boolean canChi, boolean canPeng, boolean canGang, boolean canHu) {
            this.canChi = canChi;
            this.canPeng = canPeng;
            this.canGang = canGang;
            this.canHu = canHu;
        }

        public boolean canChi() {
            return canChi;
        }

        public boolean canPeng() {
            return canPeng;
        }

        public boolean canGang() {
            return canGang;
        }

        public boolean canHu() {
            return canHu;
        }
    }
}

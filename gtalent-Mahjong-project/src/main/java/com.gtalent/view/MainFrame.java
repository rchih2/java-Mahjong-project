package com.gtalent.view;

import com.gtalent.controller.UserController;
import com.gtalent.model.User;
import com.gtalent.util.Validator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class MainFrame extends JFrame {
    private UserController userController;
    private User currentUser;

    // 遊戲資訊面板
    private JLabel gameStatusLabel;
    private JLabel remainingTilesLabel;
    private JLabel discardedTilesLabel;

    // 玩家積分顯示
    private JLabel playerScoreLabel;
    private JLabel eastScoreLabel;
    private JLabel southScoreLabel;
    private JLabel westScoreLabel;
    private JLabel northScoreLabel;

    // 中央出牌區
    private JLabel currentDiscardLabel;

    // 玩家手牌面板
    private JPanel playerHandPanel;
    private JPanel playerMeldPanel;
    private List<JButton> playerHandButtons;

    // 遊戲記錄面板
    private JTextArea logArea;

    // 按鈕控制
    private JButton chiButton;
    private JButton pengButton;
    private JButton gangButton;
    private JButton huButton;
    private JButton guoButton;
    private JButton tingButton;
    private JButton shuffleButton;

    // 遊戲資料
    private List<String> deck; // 牌堆
    private List<String> playerHand; // 玩家手牌
    private List<String> playerMelds; // 玩家副露
    private List<String>[] computerHands; // 電腦手牌 (4 個電腦玩家)
    private List<String> discardedTiles; // 已出牌
    private int currentPlayerIndex; // 當前玩家索引 (0=玩家, 1=東, 2=南, 3=西, 4=北)
    private boolean isPlayerTurn;
    private boolean waitingForPlayerAction;
    private boolean selectingChiTiles;
    private String lastDiscardTile;
    private int lastDiscardPlayerIndex;
    private List<String> selectedChiTiles;

    // 電腦玩家積分
    private int[] computerScores;

    private static final Color WAN_TILE_COLOR = new Color(236, 214, 214);
    private static final Color TONG_TILE_COLOR = new Color(213, 227, 217);
    private static final Color TIAO_TILE_COLOR = new Color(214, 222, 237);
    private static final Color HONOR_TILE_COLOR = new Color(232, 229, 220);
    private static final List<String> HONOR_ORDER = List.of("東", "南", "西", "北", "中", "發", "白");

    public MainFrame() {
        userController = new UserController();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        startNewGame();
    }

    private void initializeComponents() {
        setTitle("數字麻將單機版");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(900, 650));

        // 初始化遊戲資料
        deck = new ArrayList<>();
        playerHand = new ArrayList<>();
        playerMelds = new ArrayList<>();
        selectedChiTiles = new ArrayList<>();
        discardedTiles = new ArrayList<>();
        playerHandButtons = new ArrayList<>();
        computerHands = createComputerHands();
        computerScores = new int[5];

        for (int i = 1; i <= 4; i++) {
            computerHands[i] = new ArrayList<>();
            computerScores[i] = 25000;
        }

        // 初始化牌堆
        initializeDeck();

        // 遊戲狀態初始化
        currentPlayerIndex = 0;
        isPlayerTurn = true;
        waitingForPlayerAction = false;
        selectingChiTiles = false;
        lastDiscardTile = null;
        lastDiscardPlayerIndex = -1;
    }

    @SuppressWarnings("unchecked")
    private List<String>[] createComputerHands() {
        return (List<String>[]) new ArrayList[5];
    }

    private void initializeDeck() {
        // 建立數字牌（萬、筒、條）
        String[] suits = {"萬", "筒", "條"};
        for (String suit : suits) {
            for (int i = 1; i <= 9; i++) {
                for (int j = 0; j < 4; j++) { // 每張牌4張
                    deck.add(i + suit);
                }
            }
        }

        // 建立字牌（風牌、箭牌）
        String[] honors = {"東", "南", "西", "北", "中", "發", "白"};
        for (String honor : honors) {
            for (int j = 0; j < 4; j++) { // 每張牌4張
                deck.add(honor);
            }
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // 上方遊戲資訊面板
        JPanel topPanel = new JPanel(new GridLayout(1, 3));
        topPanel.setBorder(BorderFactory.createTitledBorder("遊戲資訊"));

        gameStatusLabel = new JLabel("遊戲狀態: 等待開始");
        remainingTilesLabel = new JLabel("剩餘牌數: " + deck.size());
        discardedTilesLabel = new JLabel("已出牌數: 0");

        topPanel.add(gameStatusLabel);
        topPanel.add(remainingTilesLabel);
        topPanel.add(discardedTilesLabel);

        // 左側電腦面板 (東、南)
        JPanel leftPanel = new JPanel(new GridLayout(2, 1));
        leftPanel.setBorder(BorderFactory.createTitledBorder("電腦玩家"));
        leftPanel.setPreferredSize(new Dimension(150, 0));

        eastScoreLabel = new JLabel("電腦玩家-東: 25000");
        southScoreLabel = new JLabel("電腦玩家-南: 25000");

        leftPanel.add(eastScoreLabel);
        leftPanel.add(southScoreLabel);

        // 中央出牌區
        JPanel centerPanel = new JPanel(new FlowLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("出牌區"));
        centerPanel.setPreferredSize(new Dimension(300, 150));

        currentDiscardLabel = new JLabel("當前打出的牌:", SwingConstants.CENTER);
        currentDiscardLabel.setFont(new Font("微軟正黑體", Font.BOLD, 24));
        currentDiscardLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        currentDiscardLabel.setPreferredSize(new Dimension(150, 100));
        currentDiscardLabel.setBackground(HONOR_TILE_COLOR);
        currentDiscardLabel.setOpaque(true);

        centerPanel.add(currentDiscardLabel);

        // 右側電腦面板 (西、北)
        JPanel rightPanel = new JPanel(new GridLayout(2, 1));
        rightPanel.setBorder(BorderFactory.createTitledBorder("電腦玩家"));
        rightPanel.setPreferredSize(new Dimension(150, 0));

        westScoreLabel = new JLabel("電腦玩家-西: 25000");
        northScoreLabel = new JLabel("電腦玩家-北: 25000");

        rightPanel.add(westScoreLabel);
        rightPanel.add(northScoreLabel);

        // 玩家積分顯示 (放在中間下方)
        JPanel scorePanel = new JPanel(new FlowLayout());
        playerScoreLabel = new JLabel("玩家: 25000");
        scorePanel.add(playerScoreLabel);

        // 底部按鈕列
        JPanel bottomPanel = new JPanel(new FlowLayout());
        chiButton = new JButton("吃牌");
        pengButton = new JButton("碰牌");
        gangButton = new JButton("槓牌");
        huButton = new JButton("胡牌");
        guoButton = new JButton("過牌");
        tingButton = new JButton("聽牌");
        shuffleButton = new JButton("重新洗牌");

        bottomPanel.add(chiButton);
        bottomPanel.add(pengButton);
        bottomPanel.add(gangButton);
        bottomPanel.add(huButton);
        bottomPanel.add(guoButton);
        bottomPanel.add(tingButton);
        bottomPanel.add(shuffleButton);

        // 玩家手牌區
        playerHandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 8));
        playerHandPanel.setBorder(BorderFactory.createTitledBorder("玩家手牌"));
        playerHandPanel.setPreferredSize(new Dimension(820, 92));
        JScrollPane handScrollPane = new JScrollPane(
                playerHandPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        handScrollPane.setPreferredSize(new Dimension(860, 120));

        playerMeldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        playerMeldPanel.setBorder(BorderFactory.createTitledBorder("玩家副露"));
        playerMeldPanel.setPreferredSize(new Dimension(860, 70));

        // 底部日誌面板
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("遊戲記錄"));
        logPanel.setPreferredSize(new Dimension(0, 150));

        logArea = new JTextArea(5, 30);
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        logPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.add(scorePanel, BorderLayout.NORTH);
        JPanel tileAreaPanel = new JPanel(new BorderLayout());
        tileAreaPanel.add(playerMeldPanel, BorderLayout.NORTH);
        tileAreaPanel.add(handScrollPane, BorderLayout.CENTER);
        bottomContainer.add(tileAreaPanel, BorderLayout.CENTER);
        bottomContainer.add(bottomPanel, BorderLayout.SOUTH);

        JPanel lowerPanel = new JPanel(new BorderLayout());
        lowerPanel.add(bottomContainer, BorderLayout.NORTH);
        lowerPanel.add(logPanel, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        mainPanel.add(lowerPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        pack();
        setLocationRelativeTo(null);
    }

    private void setupEventHandlers() {
        chiButton.addActionListener(e -> handleChiAction());
        pengButton.addActionListener(e -> handlePengAction());
        gangButton.addActionListener(e -> handleGangAction());
        huButton.addActionListener(e -> handleHuAction());
        guoButton.addActionListener(e -> handleGuoAction());
        tingButton.addActionListener(e -> handleTingAction());
        shuffleButton.addActionListener(e -> startNewGame());
    }

    private void startNewGame() {
        initializeGame();
        currentPlayerIndex = 0;
        isPlayerTurn = true;
        waitingForPlayerAction = false;
        selectingChiTiles = false;
        selectedChiTiles.clear();
        lastDiscardTile = null;
        lastDiscardPlayerIndex = -1;
        shuffleDeck();
        dealCards();
        updateUI();
        gameStatusLabel.setText("遊戲狀態: 玩家回合");
        updateActionButtons();
        logArea.append("=== 新遊戲開始 ===\n");
    }

    private void initializeGame() {
        deck.clear();
        playerHand.clear();
        playerMelds.clear();
        selectedChiTiles.clear();
        discardedTiles.clear();
        playerHandButtons.clear();

        for (int i = 1; i <= 4; i++) {
            computerHands[i].clear();
            computerScores[i] = 25000;
        }

        initializeDeck();
        playerHandPanel.removeAll();
        playerMeldPanel.removeAll();
        playerHandPanel.revalidate();
        playerHandPanel.repaint();
        playerMeldPanel.revalidate();
        playerMeldPanel.repaint();
        logArea.setText("");
    }

    private void shuffleDeck() {
        Collections.shuffle(deck);
    }

    private void dealCards() {
        playerHand.clear();
        for (int i = 0; i < 13; i++) {
            if (!deck.isEmpty()) {
                playerHand.add(deck.remove(0));
            }
        }

        // 發牌給電腦玩家
        for (int i = 1; i <= 4; i++) {
            computerHands[i].clear();
            for (int j = 0; j < 13; j++) {
                if (!deck.isEmpty()) {
                    computerHands[i].add(deck.remove(0));
                }
            }
        }

        updateUI();
    }

    private void updateUI() {
        if (waitingForPlayerAction) {
            gameStatusLabel.setText("遊戲狀態: 等待玩家反應");
        } else {
            gameStatusLabel.setText("遊戲狀態: " + (isPlayerTurn ? "玩家回合" : "電腦回合"));
        }
        remainingTilesLabel.setText("剩餘牌數: " + deck.size());
        discardedTilesLabel.setText("已出牌數: " + discardedTiles.size());

        eastScoreLabel.setText("電腦玩家-東: " + computerScores[1]);
        southScoreLabel.setText("電腦玩家-南: " + computerScores[2]);
        westScoreLabel.setText("電腦玩家-西: " + computerScores[3]);
        northScoreLabel.setText("電腦玩家-北: " + computerScores[4]);

        updatePlayerHandButtons();
        updatePlayerMeldPanel();
        updateActionButtons();
    }

    private void updatePlayerHandButtons() {
        playerHand.sort(Comparator.comparingInt(this::getTileSortOrder));
        playerHandPanel.removeAll();
        playerHandButtons.clear();

        if (playerHand != null) {
            for (String tile : playerHand) {
                JButton button = new JButton(tile);
                button.setFont(new Font("微軟正黑體", Font.BOLD, 16));
                button.setMargin(new Insets(2, 4, 2, 4));
                button.setPreferredSize(new Dimension(60, 56));
                button.setBackground(getTileColor(tile));
                button.setOpaque(true);
                button.setBorder(BorderFactory.createLineBorder(new Color(120, 120, 120)));
                button.setEnabled(selectingChiTiles || (isPlayerTurn && !waitingForPlayerAction));
                button.addActionListener(e -> handlePlayerHandButton(tile));
                playerHandPanel.add(button);
                playerHandButtons.add(button);
            }
        }

        playerHandPanel.revalidate();
        playerHandPanel.repaint();
    }

    private void updatePlayerMeldPanel() {
        playerMeldPanel.removeAll();

        for (String meld : playerMelds) {
            JLabel meldLabel = new JLabel(meld, SwingConstants.CENTER);
            meldLabel.setFont(new Font("微軟正黑體", Font.BOLD, 15));
            meldLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            meldLabel.setOpaque(true);
            meldLabel.setBackground(getTileColor(meld));
            meldLabel.setPreferredSize(new Dimension(160, 34));
            playerMeldPanel.add(meldLabel);
        }

        playerMeldPanel.revalidate();
        playerMeldPanel.repaint();
    }

    private void updateActionButtons() {
        if (selectingChiTiles) {
            chiButton.setText("取消吃牌");
            setActionButtonStates(true, false, false, false, true);
            tingButton.setEnabled(false);
            shuffleButton.setEnabled(true);
            return;
        }

        chiButton.setText("吃牌");
        boolean canRespond = waitingForPlayerAction && lastDiscardTile != null && lastDiscardPlayerIndex != 0;

        if (canRespond) {
            boolean canEat = Validator.canAttemptEat(lastDiscardTile);
            boolean canPong = Validator.checkPong(playerHand, lastDiscardTile);
            boolean canKong = Validator.checkKong(playerHand, lastDiscardTile);
            boolean canWin = Validator.checkWin(playerHand, lastDiscardTile);
            setActionButtonStates(canEat, canPong, canKong, canWin, true);
        } else {
            setActionButtonStates(false, false, false, isPlayerTurn && Validator.isWinningHand(playerHand), false);
        }

        tingButton.setEnabled(isPlayerTurn && !waitingForPlayerAction);
        shuffleButton.setEnabled(true);
    }

    private void setActionButtonStates(boolean canEat, boolean canPong, boolean canKong, boolean canWin, boolean canPass) {
        chiButton.setEnabled(canEat);
        pengButton.setEnabled(canPong);
        gangButton.setEnabled(canKong);
        huButton.setEnabled(canWin);
        guoButton.setEnabled(canPass);
    }

    private void handlePlayerHandButton(String tile) {
        if (selectingChiTiles) {
            handleChiTileSelection(tile);
        } else {
            handlePlayerDiscard(tile);
        }
    }

    private void handlePlayerDiscard(String tile) {
        if (!isPlayerTurn) return;

        playerHand.remove(tile);
        currentDiscardLabel.setText(tile);
        currentDiscardLabel.setBackground(getTileColor(tile));
        discardedTiles.add(tile);
        lastDiscardTile = tile;
        lastDiscardPlayerIndex = 0;
        waitingForPlayerAction = false;
        isPlayerTurn = false;
        gameStatusLabel.setText("遊戲狀態: 電腦回合");
        updateUI();

        logArea.append("[玩家] 打出了【" + tile + "】\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        updateActionButtons();

        startComputerTurnTimer();
    }

    private void startComputerTurnTimer() {
        Timer computerTurnTimer = new Timer(800, null);
        computerTurnTimer.addActionListener(e -> {
            computerTurnTimer.stop();
            handleComputerTurn();
        });
        computerTurnTimer.setRepeats(false);
        computerTurnTimer.start();
    }

    private void handleComputerTurn() {
        if (deck.isEmpty()) {
            gameStatusLabel.setText("遊戲狀態: 牌堆已盡");
            return;
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % 5;

        if (currentPlayerIndex == 0) {
            isPlayerTurn = true;
            waitingForPlayerAction = false;
            gameStatusLabel.setText("遊戲狀態: 玩家回合");

            if (!deck.isEmpty()) {
                String newTile = deck.remove(0);
                playerHand.add(newTile);
                updateUI();
                logArea.append("[玩家] 摸到了【" + newTile + "】\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }

            updateActionButtons();
        } else {
            if (!deck.isEmpty()) {
                String drawnTile = deck.remove(0);
                computerHands[currentPlayerIndex].add(drawnTile);
            }

            String computerTile = getComputerDiscard(currentPlayerIndex);
            currentDiscardLabel.setText(computerTile);
            currentDiscardLabel.setBackground(getTileColor(computerTile));
            discardedTiles.add(computerTile);
            lastDiscardTile = computerTile;
            lastDiscardPlayerIndex = currentPlayerIndex;
            waitingForPlayerAction = true;
            isPlayerTurn = false;
            updateUI();

            String playerName = getComputerName(currentPlayerIndex);
            logArea.append("[電腦-" + playerName + "] 打出了【" + computerTile + "】\n");
            logArea.append("請選擇：吃、碰、槓、胡，或按「過牌」繼續。\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }

    private String getComputerName(int index) {
        switch (index) {
            case 1: return "東";
            case 2: return "南";
            case 3: return "西";
            case 4: return "北";
            default: return "";
        }
    }

    private String getComputerDiscard(int computerIndex) {
        Random random = new Random();
        int index = random.nextInt(computerHands[computerIndex].size());
        return computerHands[computerIndex].remove(index);
    }

    private void handleChiAction() {
        if (selectingChiTiles) {
            cancelChiSelection();
            return;
        }
        if (!canRespondToDiscard()) {
            showCannotUseAction("吃牌");
            return;
        }
        if (!Validator.canAttemptEat(lastDiscardTile)) {
            JOptionPane.showMessageDialog(this, "字牌不能吃【" + lastDiscardTile + "】");
            return;
        }
        if (!Validator.canChi(playerHand, lastDiscardTile)) {
            JOptionPane.showMessageDialog(this, "目前不能吃【" + lastDiscardTile + "】");
            return;
        }

        selectingChiTiles = true;
        selectedChiTiles.clear();
        logArea.append("請從手牌中選擇 2 張牌來吃【" + lastDiscardTile + "】。\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        updateUI();
    }

    private void handleChiTileSelection(String tile) {
        selectedChiTiles.add(tile);
        logArea.append("已選擇吃牌用牌：" + String.join("、", selectedChiTiles) + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());

        if (selectedChiTiles.size() < 2) {
            return;
        }

        String firstTile = selectedChiTiles.get(0);
        String secondTile = selectedChiTiles.get(1);
        if (!Validator.isValidEatSelection(playerHand, lastDiscardTile, firstTile, secondTile)) {
            JOptionPane.showMessageDialog(this, "選擇的牌不能和【" + lastDiscardTile + "】組成順子");
            selectedChiTiles.clear();
            return;
        }

        selectingChiTiles = false;
        selectedChiTiles.clear();
        playerHand.remove(firstTile);
        playerHand.remove(secondTile);
        takeDiscardedTile("吃", firstTile + "、" + lastDiscardTile + "、" + secondTile);
    }

    private void cancelChiSelection() {
        selectingChiTiles = false;
        selectedChiTiles.clear();
        updateUI();
        logArea.append("[玩家] 取消吃牌\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void handlePengAction() {
        if (!canRespondToDiscard()) {
            showCannotUseAction("碰牌");
            return;
        }
        if (!userController.canPeng(playerHand, lastDiscardTile)) {
            JOptionPane.showMessageDialog(this, "目前不能碰【" + lastDiscardTile + "】");
            return;
        }

        removeTiles(lastDiscardTile, 2);
        takeDiscardedTile("碰", lastDiscardTile + "、" + lastDiscardTile + "、" + lastDiscardTile);
    }

    private void handleGangAction() {
        if (!canRespondToDiscard()) {
            showCannotUseAction("槓牌");
            return;
        }
        if (!userController.canGang(playerHand, lastDiscardTile)) {
            JOptionPane.showMessageDialog(this, "目前不能槓【" + lastDiscardTile + "】");
            return;
        }

        removeTiles(lastDiscardTile, 3);
        takeDiscardedTile("槓", lastDiscardTile + "、" + lastDiscardTile + "、" + lastDiscardTile + "、" + lastDiscardTile);
        if (!deck.isEmpty()) {
            String supplementTile = deck.remove(0);
            playerHand.add(supplementTile);
            logArea.append("[玩家] 槓後補牌【" + supplementTile + "】\n");
        }
        updateUI();
    }

    private void handleHuAction() {
        boolean canWin = canRespondToDiscard()
                ? userController.canHu(playerHand, lastDiscardTile)
                : Validator.isWinningHand(playerHand);

        if (canWin) {
            JOptionPane.showMessageDialog(this, "恭喜！你胡牌了！");
            logArea.append("[玩家] 胡牌！\n");
            if (currentUser != null) {
                userController.updateUserStats(currentUser, true);
            }
            disableGameControls();
        } else {
            JOptionPane.showMessageDialog(this, "目前不能胡牌");
        }
    }

    private void handleGuoAction() {
        if (!waitingForPlayerAction) {
            JOptionPane.showMessageDialog(this, "目前沒有可過的牌");
            return;
        }

        waitingForPlayerAction = false;
        gameStatusLabel.setText("遊戲狀態: 電腦回合");
        updateUI();
        logArea.append("[玩家] 過牌\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        startComputerTurnTimer();
    }

    private void handleTingAction() {
        List<String> suggestions = Validator.getTingPaiSuggestions(playerHand.toArray(new String[0]));
        if (suggestions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "目前沒有聽牌");
        } else {
            StringBuilder message = new StringBuilder("聽牌:\n");
            for (String tile : suggestions) {
                message.append(tile).append("\n");
            }
            JOptionPane.showMessageDialog(this, message.toString());
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            playerScoreLabel.setText("玩家: 25000 - " + user.getUsername());
        }
    }

    private boolean canRespondToDiscard() {
        return waitingForPlayerAction && lastDiscardTile != null && lastDiscardPlayerIndex != 0;
    }

    private void showCannotUseAction(String actionName) {
        JOptionPane.showMessageDialog(this, "目前不能" + actionName + "，請等電腦打出牌後再操作。");
    }

    private void removeTiles(String tile, int amount) {
        for (int i = 0; i < amount; i++) {
            playerHand.remove(tile);
        }
    }

    private void takeDiscardedTile(String actionName, String meldDescription) {
        if (!discardedTiles.isEmpty()) {
            discardedTiles.remove(discardedTiles.size() - 1);
        }
        playerMelds.add(actionName + ": " + meldDescription);
        currentDiscardLabel.setText(actionName + "牌");
        currentDiscardLabel.setBackground(HONOR_TILE_COLOR);
        waitingForPlayerAction = false;
        isPlayerTurn = true;

        logArea.append("[玩家] " + actionName + "了【" + lastDiscardTile + "】 (" + meldDescription + ")\n");
        logArea.append("請從手牌中選一張打出。\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());

        lastDiscardTile = null;
        lastDiscardPlayerIndex = -1;
        updateUI();
    }

    private void disableGameControls() {
        isPlayerTurn = false;
        waitingForPlayerAction = false;
        for (JButton button : playerHandButtons) {
            button.setEnabled(false);
        }
        chiButton.setEnabled(false);
        pengButton.setEnabled(false);
        gangButton.setEnabled(false);
        huButton.setEnabled(false);
        guoButton.setEnabled(false);
        tingButton.setEnabled(false);
    }

    private Color getTileColor(String tileText) {
        if (tileText == null) {
            return HONOR_TILE_COLOR;
        }
        if (tileText.contains("萬")) {
            return WAN_TILE_COLOR;
        }
        if (tileText.contains("筒")) {
            return TONG_TILE_COLOR;
        }
        if (tileText.contains("條")) {
            return TIAO_TILE_COLOR;
        }
        return HONOR_TILE_COLOR;
    }

    private int getTileSortOrder(String tile) {
        if (tile == null || tile.isBlank()) {
            return 999;
        }
        if (tile.endsWith("萬")) {
            return getNumberPart(tile) + 0;
        }
        if (tile.endsWith("筒")) {
            return getNumberPart(tile) + 20;
        }
        if (tile.endsWith("條")) {
            return getNumberPart(tile) + 40;
        }

        int honorIndex = HONOR_ORDER.indexOf(tile);
        return honorIndex >= 0 ? 100 + honorIndex : 999;
    }

    private int getNumberPart(String tile) {
        try {
            return Integer.parseInt(tile.substring(0, tile.length() - 1));
        } catch (NumberFormatException e) {
            return 99;
        }
    }
}

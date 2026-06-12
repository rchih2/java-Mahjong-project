package com.gtalent.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Validator {
    private static final String[] NUMBER_SUITS = {"萬", "筒", "條"};
    private static final String[] HONORS = {"東", "南", "西", "北", "中", "發", "白"};

    public static boolean checkPong(List<String> hand, String targetCard) {
        return countTile(hand, normalizeTile(targetCard)) >= 2;
    }

    public static boolean checkKong(List<String> hand, String targetCard) {
        return countTile(hand, normalizeTile(targetCard)) == 3;
    }

    public static boolean checkEat(List<String> hand, String targetCard) {
        return !getChiCombinations(hand, targetCard).isEmpty();
    }

    public static boolean canAttemptEat(String targetCard) {
        return parseNumberTile(targetCard) != null;
    }

    public static boolean isValidEatSelection(List<String> hand, String targetCard, String firstSelectedTile, String secondSelectedTile) {
        if (hand == null) {
            return false;
        }

        String target = normalizeTile(targetCard);
        String first = normalizeTile(firstSelectedTile);
        String second = normalizeTile(secondSelectedTile);

        TileInfo targetInfo = parseNumberTile(target);
        TileInfo firstInfo = parseNumberTile(first);
        TileInfo secondInfo = parseNumberTile(second);
        if (targetInfo == null || firstInfo == null || secondInfo == null) {
            return false;
        }
        if (!targetInfo.suit.equals(firstInfo.suit) || !targetInfo.suit.equals(secondInfo.suit)) {
            return false;
        }
        if (!containsTile(hand, first) || !containsTile(hand, second)) {
            return false;
        }
        if (first.equals(second) && countTile(hand, first) < 2) {
            return false;
        }

        List<Integer> numbers = new ArrayList<>();
        numbers.add(targetInfo.number);
        numbers.add(firstInfo.number);
        numbers.add(secondInfo.number);
        Collections.sort(numbers);

        return numbers.get(0) + 1 == numbers.get(1)
                && numbers.get(1) + 1 == numbers.get(2);
    }

    public static boolean checkWin(List<String> hand, String targetCard) {
        return canHu(hand, targetCard);
    }

    public static boolean canPeng(List<String> hand, String discardedTile) {
        return checkPong(hand, discardedTile);
    }

    public static boolean canGang(List<String> hand, String discardedTile) {
        return checkKong(hand, discardedTile);
    }

    public static boolean canChi(List<String> hand, String discardedTile) {
        return checkEat(hand, discardedTile);
    }

    public static boolean canHu(List<String> hand, String discardedTile) {
        String targetCard = normalizeTile(discardedTile);
        if (hand == null || targetCard == null) {
            return false;
        }

        List<String> tiles = new ArrayList<>(hand);
        tiles.add(targetCard);
        return isWinningHand(tiles);
    }

    public static List<List<String>> getChiCombinations(List<String> hand, String discardedTile) {
        List<List<String>> combinations = new ArrayList<>();
        String targetCard = normalizeTile(discardedTile);
        TileInfo discarded = parseNumberTile(targetCard);
        if (hand == null || discarded == null) {
            return combinations;
        }

        addChiCombinationIfPresent(combinations, hand, discarded, -2, -1);
        addChiCombinationIfPresent(combinations, hand, discarded, -1, 1);
        addChiCombinationIfPresent(combinations, hand, discarded, 1, 2);
        return combinations;
    }

    public static List<String> getFirstChiCombination(List<String> hand, String discardedTile) {
        List<List<String>> combinations = getChiCombinations(hand, discardedTile);
        if (combinations.isEmpty()) {
            return Collections.emptyList();
        }
        return combinations.get(0);
    }

    public static boolean isWinningHand(String[] hand) {
        if (hand == null) {
            return false;
        }

        List<String> tiles = new ArrayList<>();
        Collections.addAll(tiles, hand);
        return isWinningHand(tiles);
    }

    public static boolean isWinningHand(List<String> hand) {
        if (hand == null || hand.size() != 14) {
            return false;
        }

        Map<String, Integer> counts = buildCountMap(hand);
        for (String pairTile : new ArrayList<>(counts.keySet())) {
            if (counts.get(pairTile) < 2) {
                continue;
            }

            removeTile(counts, pairTile, 2);
            if (canFormAllMelds(counts)) {
                addTile(counts, pairTile, 2);
                return true;
            }
            addTile(counts, pairTile, 2);
        }

        return false;
    }

    public static List<String> getTingPaiSuggestions(String[] hand) {
        List<String> suggestions = new ArrayList<>();
        if (hand == null) {
            return suggestions;
        }

        List<String> currentHand = new ArrayList<>();
        Collections.addAll(currentHand, hand);

        for (String candidate : getAllTileTypes()) {
            List<String> testHand = new ArrayList<>(currentHand);
            testHand.add(candidate);
            if (isWinningHand(testHand)) {
                suggestions.add(candidate);
            }
        }

        return suggestions;
    }

    private static void addChiCombinationIfPresent(
            List<List<String>> combinations,
            List<String> hand,
            TileInfo discarded,
            int firstOffset,
            int secondOffset
    ) {
        int firstNumber = discarded.number + firstOffset;
        int secondNumber = discarded.number + secondOffset;
        if (firstNumber < 1 || firstNumber > 9 || secondNumber < 1 || secondNumber > 9) {
            return;
        }

        String firstTile = firstNumber + discarded.suit;
        String secondTile = secondNumber + discarded.suit;
        if (hand.contains(firstTile) && hand.contains(secondTile)) {
            List<String> combination = new ArrayList<>();
            combination.add(firstTile);
            combination.add(secondTile);
            combinations.add(combination);
        }
    }

    private static boolean canFormAllMelds(Map<String, Integer> counts) {
        String tile = findFirstRemainingTile(counts);
        if (tile == null) {
            return true;
        }

        if (counts.get(tile) >= 3) {
            removeTile(counts, tile, 3);
            if (canFormAllMelds(counts)) {
                addTile(counts, tile, 3);
                return true;
            }
            addTile(counts, tile, 3);
        }

        TileInfo tileInfo = parseNumberTile(tile);
        if (tileInfo != null && tileInfo.number <= 7) {
            String nextTile = (tileInfo.number + 1) + tileInfo.suit;
            String nextNextTile = (tileInfo.number + 2) + tileInfo.suit;
            if (getCount(counts, nextTile) > 0 && getCount(counts, nextNextTile) > 0) {
                removeTile(counts, tile, 1);
                removeTile(counts, nextTile, 1);
                removeTile(counts, nextNextTile, 1);
                if (canFormAllMelds(counts)) {
                    addTile(counts, tile, 1);
                    addTile(counts, nextTile, 1);
                    addTile(counts, nextNextTile, 1);
                    return true;
                }
                addTile(counts, tile, 1);
                addTile(counts, nextTile, 1);
                addTile(counts, nextNextTile, 1);
            }
        }

        return false;
    }

    private static String findFirstRemainingTile(Map<String, Integer> counts) {
        String first = null;
        for (String tile : counts.keySet()) {
            if (counts.get(tile) <= 0) {
                continue;
            }
            if (first == null || compareTiles(tile, first) < 0) {
                first = tile;
            }
        }
        return first;
    }

    private static int compareTiles(String first, String second) {
        return Integer.compare(tileOrder(first), tileOrder(second));
    }

    private static int tileOrder(String tile) {
        TileInfo tileInfo = parseNumberTile(tile);
        if (tileInfo != null) {
            int suitOffset = switch (tileInfo.suit) {
                case "萬" -> 0;
                case "筒" -> 10;
                case "條" -> 20;
                default -> 30;
            };
            return suitOffset + tileInfo.number;
        }

        for (int i = 0; i < HONORS.length; i++) {
            if (HONORS[i].equals(tile)) {
                return 100 + i;
            }
        }
        return 999;
    }

    private static Map<String, Integer> buildCountMap(List<String> tiles) {
        Map<String, Integer> counts = new HashMap<>();
        for (String tile : tiles) {
            counts.put(tile, counts.getOrDefault(tile, 0) + 1);
        }
        return counts;
    }

    private static int countTile(List<String> hand, String tile) {
        String targetTile = normalizeTile(tile);
        if (hand == null || targetTile == null) {
            return 0;
        }

        int count = 0;
        for (String handTile : hand) {
            if (targetTile.equals(normalizeTile(handTile))) {
                count++;
            }
        }
        return count;
    }

    private static boolean containsTile(List<String> hand, String tile) {
        String targetTile = normalizeTile(tile);
        if (targetTile == null) {
            return false;
        }

        for (String handTile : hand) {
            if (targetTile.equals(normalizeTile(handTile))) {
                return true;
            }
        }
        return false;
    }

    private static int getCount(Map<String, Integer> counts, String tile) {
        return counts.getOrDefault(tile, 0);
    }

    private static void removeTile(Map<String, Integer> counts, String tile, int amount) {
        int newCount = counts.getOrDefault(tile, 0) - amount;
        if (newCount <= 0) {
            counts.remove(tile);
        } else {
            counts.put(tile, newCount);
        }
    }

    private static void addTile(Map<String, Integer> counts, String tile, int amount) {
        counts.put(tile, counts.getOrDefault(tile, 0) + amount);
    }

    private static List<String> getAllTileTypes() {
        List<String> tiles = new ArrayList<>();
        for (String suit : NUMBER_SUITS) {
            for (int number = 1; number <= 9; number++) {
                tiles.add(number + suit);
            }
        }
        Collections.addAll(tiles, HONORS);
        return tiles;
    }

    private static TileInfo parseNumberTile(String tile) {
        String normalizedTile = normalizeTile(tile);
        if (normalizedTile == null || normalizedTile.length() < 2) {
            return null;
        }

        String suit = normalizedTile.substring(normalizedTile.length() - 1);
        if (!"萬".equals(suit) && !"筒".equals(suit) && !"條".equals(suit)) {
            return null;
        }

        try {
            int number = Integer.parseInt(normalizedTile.substring(0, normalizedTile.length() - 1));
            if (number < 1 || number > 9) {
                return null;
            }
            return new TileInfo(number, suit);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String normalizeTile(String tile) {
        if (tile == null) {
            return null;
        }

        String normalized = tile
                .replace("【", "")
                .replace("】", "")
                .trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static class TileInfo {
        private final int number;
        private final String suit;

        private TileInfo(int number, String suit) {
            this.number = number;
            this.suit = suit;
        }
    }
}

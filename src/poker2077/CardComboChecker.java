package poker2077;

import poker2077.ent.Card;
import poker2077.ent.CardRank;
import poker2077.ent.CardType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Надеюсь не надо объяснять зачем нужен этот статичный класс
public class CardComboChecker {
    static int checkComboValue(List<Card> hand, List<Card> table) {
        List<Card> cards = new ArrayList<>();
        cards.addAll(hand);
        cards.addAll(table);
        // We need to send copies
        int val = 0;
        val = pComboRoyalFlush(new ArrayList<>(cards));
        if (val != 0) return val;
        val = pComboStraightFlush(new ArrayList<>(cards));
        if (val != 0) return val;
        val = pComboFourOfKind(new ArrayList<>(cards));
        if (val != 0) return val;
        val = pComboFullHouse(new ArrayList<>(cards));
        if (val != 0) return val;
        val = pComboFlush(new ArrayList<>(cards));
        if (val != 0) return val;
        val = pComboStraight(new ArrayList<>(cards));
        if (val != 0) return val;
        val = pComboSet(new ArrayList<>(cards));
        if (val != 0) return val;
        // Either 2-3 or 0
        return pComboPair(new ArrayList<>(cards));
    }

    static int pComboRoyalFlush(List<Card> cards) {
        cards.sort(Comparator.comparingInt(c -> c.getRank().ordinal()));
        if (cards.get(0).getRank() != CardRank.Ace) {
            // первый туз
            return 0;
        }
        // Это стрит с тузом в главе
        if (pComboStraightFlush(cards) == 0) {
            return 0;

        }
        return 10; // weight
    }

    static int pComboStraightFlush(List<Card> cards) {
        cards.sort(Comparator.comparingInt(c -> c.getRank().ordinal()));
        for (int i = 0; i < cards.size() - 1; i++) {
            // should be subsequent rank
            if (cards.get(i).getRank().ordinal() + 1 != cards.get(i + 1).getRank().ordinal()) {
                return 0;
            }
            // and should be same type
            if (cards.get(i).getType() != cards.get(i + 1).getType()) {
                return 0;
            }
        }
        return 9; // weight
    }

    static int pComboFourOfKind(List<Card> cards) {
        Map<CardType, List<Card>> sortedMap = cards.stream().collect(Collectors.groupingBy(Card::getType));
        for(var entry : sortedMap.entrySet()) {
            if (entry.getValue().size() == 4) {
                return 8; // weight
            }
        }
        return 0;
    }

    static int pComboFullHouse(List<Card> cards) {
        Map<CardRank, List<Card>> sortedMap = cards.stream().collect(Collectors.groupingBy(Card::getRank));
        if (sortedMap.size() == 2) {
            for(var entry : sortedMap.entrySet()) {
                // всего два типа карт и один из них тройка
                if (entry.getValue().size() == 3) {
                    return 7; // weight
                }
            }
        }
        return 0;
    }
    static int pComboFlush(List<Card> cards) {
        Map<CardType, List<Card>> sortedMap = cards.stream().collect(Collectors.groupingBy(Card::getType));
        // Либо все одной масти, либо неважно
        if (sortedMap.size() == 1) {
            return 6; // weight
        }
        return 0;
    }

    static int pComboStraight(List<Card> cards) {
        cards.sort(Comparator.comparingInt(c -> c.getRank().ordinal()));
        for (int i = 0; i < cards.size() - 1; i++) {
            if (cards.get(i).getRank().ordinal() + 1 != cards.get(i + 1).getRank().ordinal()) {
                return 0;
            }
        }
        return 5; // weight
    }

    static int pComboSet(List<Card> cards) {
        Map<CardRank, List<Card>> sortedMap = cards.stream().collect(Collectors.groupingBy(Card::getRank));
        for(var entry : sortedMap.entrySet()) {
            if (entry.getValue().size() == 3) {
                return 4; // weight
            }
        }
        return 0;
    }

    static int pComboPair(List<Card> cards) {
        int pairs = 0;
        boolean stopper = false;
        for (int i = 0; i < cards.size(); i++) {
            for (int j = i + 1; j < cards.size(); j++) {
                if(i==j) continue;
                if (cards.get(i).getRank() == cards.get(j).getRank()) {
                    var c1 = cards.get(i);
                    var c2 = cards.get(j);
                    pairs++;
                    cards.remove(c1);
                    cards.remove(c2);
                    stopper = true;
                    break;
                }
            }
            if (stopper) break;
        }
        if (pairs == 0)
            return pairs;
        // Check again for another pair (ex. j-j and 9-9)
        int newp = pComboPair(cards);
        if (newp > 0) {
            pairs += newp;
        }

        return pairs+1; // because 2 or 3 (1=high)
    }

    static CardRank pGetComboHighCard(List<Card> cards) {
        cards.sort(Comparator.comparingInt(c -> c.getRank().ordinal()));
        return cards.get(0).getRank();
    }

}

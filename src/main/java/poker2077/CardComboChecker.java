package poker2077;

import poker2077.ent.Card;
import poker2077.ent.CardRank;
import poker2077.ent.CardType;
import poker2077.ent.Combo;

import java.util.*;
import java.util.stream.Collectors;

// Надеюсь не надо объяснять зачем нужен этот статичный класс
public class CardComboChecker {
    static Combo checkComboValue(List<Card> hand, List<Card> table) {
        List<Card> cards = new ArrayList<>();
        cards.addAll(hand);
        cards.addAll(table);
        // We need to send copies
        Combo val = Combo.None;
        val = pComboRoyalFlush(new ArrayList<>(cards));
        if (val.ordinal() != 0) return val;
        val = pComboStraightFlush(new ArrayList<>(cards));
        if (val.ordinal() != 0) return val;
        val = pComboFourOfKind(new ArrayList<>(cards));
        if (val.ordinal() != 0) return val;
        val = pComboFullHouse(new ArrayList<>(cards));
        if (val.ordinal() != 0) return val;
        val = pComboFlush(new ArrayList<>(cards));
        if (val.ordinal() != 0) return val;
        val = pComboStraight(new ArrayList<>(cards));
        if (val.ordinal() != 0) return val;
        val = pComboSet(new ArrayList<>(cards));
        if (val.ordinal() != 0) return val;
        // Either 2-3 or 0
        return pComboPair(new ArrayList<>(cards));
    }

    static Combo pComboRoyalFlush(List<Card> cards) {
        cards.sort(Comparator.comparingInt(c -> c.getRank().ordinal()));
        if (cards.get(0).getRank() != CardRank.Ace) {
            // первый туз
            return Combo.None;
        }
        // Это стрит с тузом в главе
        return pComboStraightFlush(cards)==Combo.None
                ?Combo.None
                :Combo.RoyalFlush; // weight
    }

    static Combo pComboStraightFlush(List<Card> cards) {
        cards.sort(Comparator.comparingInt(c -> c.getRank().ordinal())); // я опять забыл почему так сделал
        for (int i = 0; i < cards.size() - 1; i++) {
            // should be subsequent rank
            if (cards.get(i).getRank().ordinal() + 1 != cards.get(i + 1).getRank().ordinal()) {
                return Combo.None;
            }
            // and should be same type
            if (cards.get(i).getType() != cards.get(i + 1).getType()) {
                return Combo.None;
            }
        }
        return Combo.StraithFlush; // weight
    }

    static Combo pComboFourOfKind(List<Card> cards) {
        Map<CardRank, List<Card>> sortedMap = cards.stream().collect(Collectors.groupingBy(Card::getRank));
        for(var entry : sortedMap.entrySet()) {
            if (entry.getValue().size() == 4) {
                return Combo.FourOfKind; // weight
            }
        }
        return Combo.None;
    }

    static Combo pComboFullHouse(List<Card> cards) {
        Map<CardRank, List<Card>> sortedMap = cards.stream().collect(Collectors.groupingBy(Card::getRank));
        if (sortedMap.size() == 2) {
            for(var entry : sortedMap.entrySet()) {
                // всего два типа карт и один из них тройка
                if (entry.getValue().size() == 3) {
                    return Combo.FullHouse; // weight
                }
            }
        }
        return Combo.None;
    }
    static Combo pComboFlush(List<Card> cards) {
        Map<CardType, List<Card>> sortedMap = cards.stream().collect(Collectors.groupingBy(Card::getType));
        // Либо все одной масти, либо неважно
        if (sortedMap.size() == 1) {
            return Combo.Flush; // weight
        }
        return Combo.None;
    }

    static Combo pComboStraight(List<Card> cards) {
        cards.sort(Comparator.comparingInt(c -> c.getRank().ordinal()));
        for (int i = 0; i < cards.size() - 1; i++) {
            if (cards.get(i).getRank().ordinal() + 1 != cards.get(i + 1).getRank().ordinal()) {
                return Combo.None;
            }
        }
        return Combo.Straight; // weight
    }

    static Combo pComboSet(List<Card> cards) {
        Map<CardRank, List<Card>> sortedMap = cards.stream().collect(Collectors.groupingBy(Card::getRank));
        for(var entry : sortedMap.entrySet()) {
            if (entry.getValue().size() == 3) {
                return Combo.Set; // weight
            }
        }
        return Combo.None;
    }

    static Combo pComboPair(List<Card> cards) {
        // Так, еще раз. Пар может быть 1 или 2. так что
        Set<CardRank> hasRank = new HashSet<>();
        int pairs = 0;
        for (int i=0; i<cards.size(); i++) {
            // Каждая карта существует или нет
            if(hasRank.contains(cards.get(i).getRank())) {
                continue;
            }
            for (int j=i+1; j<cards.size(); j++) {
                // И если мы найдем еще такую карту, то это пара. А 2 пары двоек у нас не будет, так как это каре и отфильтруется там же
                if (cards.get(i).getRank() == cards.get(j).getRank()) {
                    pairs++;
                    hasRank.add(cards.get(i).getRank());
                    break;
                }
            }
        }
        // А теперь для тупых вроде меня. Карт 7, пар поместится 3, но тогда это уже другое и у нас или одна пара, или нет

        if (pairs>1)
            return Combo.DoublePair;
        if (pairs>0)
            return Combo.Pair;
        return Combo.None;
    }

    static CardRank pGetComboHighCard(List<Card> cards) {
        cards.sort(Comparator.comparingInt(c -> c.getRank().ordinal()));
        return cards.get(0).getRank();
    }

}

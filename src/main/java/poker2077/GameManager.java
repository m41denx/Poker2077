package poker2077;

import poker2077.ApiObjects.*;
import poker2077.ent.*;

import java.util.*;
import java.util.stream.Collectors;


// Место, где шизофрения встречает азарт
public class GameManager {

    protected Table table;
    protected boolean isActive;

    // Почему мап дерево
    protected Map<String, IPlayerManager> players = new LinkedHashMap<>(); // для аутентификации UUID -> Юзер

    private List<Event> evtLoop = new LinkedList<>(); // все события, ботам отдаются только последнее
    private String currentPlayer = ""; // UUID текущего игрока (у нас фронт асинхронный, так что это единственный способ проверять кто ходит)


    GameManager() {
        table = new Table();
    }

    private boolean authPlayer(String uuid) {
        return this.players.containsKey(uuid);
    }

    private void nextPlayer() {
        // Это подобие одного тика в играх
        if (this.players.size() == 1) {
            return;
        }

        // Наш любимый onLoop
        for(var p: this.players.values()) {
            p.onLoop(this.evtLoop.get(this.evtLoop.size()-1), this);
        }

        // One day I was born, at the age of four
        var pIter = players.keySet().iterator();
        while (pIter.hasNext()) {
            var p = pIter.next();
            if (p.equals(this.currentPlayer)) {
                if(!pIter.hasNext()) {
                    pIter = players.keySet().iterator(); // Ну конечно, это же намного проще и понятнее чем листы с индексами
                }
                this.currentPlayer = pIter.next();
                break;
            }
        }

        // First el players.keySet().stream().toList().get(0);

        var curP = this.players.get(this.currentPlayer);
//        System.out.println("Current player: " + curP.getName());
        // Хост начинает, хост и заканчивает: если после полного круга у нас на столе 5 карт, то заканчиваем игру
        // My momma asked me a question: wth are those?
        if (Objects.equals(this.currentPlayer, players.keySet().stream().toList().get(0))) {
            if (this.table.getFlow().size()==5) {
                endGame();
                return;
            }
            if (!isActive) {
                // Выкладываем флоп только после начальных ставок, иначе грустно
                this.table.addFlow();
                this.table.addFlow();
                this.table.addFlow();
                isActive = true;
            } else {
                // иначе, извольте терн
                this.table.addFlow();
            }
        }

        // Если игрок вышел, то пинаем следующего
        if (curP.isFolded()) {
            nextPlayer();
        }else{
            // Боты бип буп
            curP.automate(this);
        }
    }

    public static String microPrinter(List<Card> l) {
        return l.stream().map(Card::toString).collect(Collectors.joining(" "));
    }

    private void endGame() {
        // Пока мы считаем, никто не играет
        this.currentPlayer = "";
        emitEvent(new Event(EventType.CALL, "Конец игры. Подсчет карт...", 0));
        IPlayerManager bestPlayer = players.values().stream().findFirst().get(); // Да мне плевать на isPresent()
        // Я удивлюсь если оно заработает
        Combo bestValue = Combo.None;
        for(var p: this.players.values()) {
            // если ты вышел, то и считать нечего
            if (p.isFolded())
                continue;
            // Сейчас мы узнаем что за комбо у игрока
            Combo val = CardComboChecker.checkComboValue(p.peek(), table.getFlow());
            if (val!=Combo.None) {
                // Если комбо есть, то это стоит отпраздновать
                String msg = p.getName() + " - " + val + "\n" + microPrinter(p.peek());
                emitEvent(new Event(EventType.RAISE, msg, val.ordinal()));
            }
            if (val.ordinal()>bestValue.ordinal()) {
                // Если комбо лучше, то логично что лучше
                bestValue = val;
                bestPlayer = p;
            } else if (val==bestValue) {
                // Если комбо одинаковые у игроков, то ищем у кого старшая карта
                var myCard = CardComboChecker.pGetComboHighCard(new ArrayList<>(p.peek()));
                var bestCard = CardComboChecker.pGetComboHighCard(new ArrayList<>(bestPlayer.peek()));

                // Так как enum идет от туза до 1, то id туза самое маленькое
                if (myCard.ordinal()<bestCard.ordinal()) {
                    bestPlayer = p;
                }
            }
        }
        emitEvent(new Event(EventType.CALL, "Конец игры. Победил " + bestPlayer.getName(), 0));
        bestPlayer.setBank(bestPlayer.getBank()+table.getBankPool()); // Честные выплаты
        table = new Table(); // Сброс стола и подготовка к новой игре
        for(var pl: players.values()) {
            pl.reset();
            pl.giveCard(table.popDeck());
            pl.giveCard(table.popDeck());
        }
        isActive = false;
        currentPlayer = players.keySet().stream().toList().get(0); // Первый опять хост, но если он банкврот, то следующий
        if (players.get(currentPlayer).isFolded()) {
            nextPlayer();
        }
    }


    public void emitEvent(Event e) {
        evtLoop.add(e);
    }
    //region API

    public GenericCtx raise(String uuid, long sum) {
        if (!this.authPlayer(uuid))
            return new GenericCtx(false, "Вы не в игре");
        if (!Objects.equals(this.currentPlayer, uuid))
            return new GenericCtx(false, "Не ваш ход");
        var player = this.players.get(uuid);
        if (sum==0) {
            return new GenericCtx(false, "Укажите ненулевую ставку");
        }
        if (player.isFolded())
            return new GenericCtx(false, "Вы уже вышли (фолд)");
        if (sum>player.getBank())
            return new GenericCtx(false, "Недостаточно средств");
        //
        this.table.setBankPool(this.table.getBankPool()+sum);
        this.table.setDeposit(this.table.getDeposit()+sum);
        player.setBank(player.getBank()-sum);
        player.setDeposit(player.getDeposit()+sum);
        this.emitEvent(new Event(EventType.RAISE,player.getName()+": рейз (+"+sum+")", sum));
        this.nextPlayer();
        return new GenericCtx(true, "");
    }

    public GenericCtx call(String uuid) {
        if (!this.authPlayer(uuid))
            return new GenericCtx(false, "Вы не в игре");
        if (!Objects.equals(this.currentPlayer, uuid))
            return new GenericCtx(false, "Не ваш ход");
        var player = this.players.get(uuid);
        if (table.getDeposit()==0) {
            return new GenericCtx(false, "Сначала надо сделать депозит");
        }
        if (player.isFolded())
            return new GenericCtx(false, "Вы уже вышли (фолд)");
        long delta = table.getDeposit() - player.getDeposit();
        if (delta>player.getBank()) {
            this.fold(uuid);
            return new GenericCtx(false, "Недостаточно средств: Автофолд");
        }
        this.table.setBankPool(this.table.getBankPool()+delta);
        player.setBank(player.getBank()-delta);
        player.setDeposit(player.getDeposit()+delta);
        this.emitEvent(new Event(EventType.CALL,player.getName()+": колл", 0));
        this.nextPlayer();
        return new GenericCtx(true, "");
    }


    public GenericCtx fold(String uuid) {
        if (!this.authPlayer(uuid))
            return new GenericCtx(false, "Вы не в игре");
        if (!Objects.equals(this.currentPlayer, uuid))
            return new GenericCtx(false, "Не ваш ход");
        var player = this.players.get(uuid);
        player.fold();
        this.emitEvent(new Event(EventType.FOLD, player.getName()+": сделал фолд", 0));
        this.nextPlayer();
        return new GenericCtx(true, "");
    }

    public StatusCtx getStatus() {
        return new StatusCtx(this.isActive, this.players.size());
    }


    //newGame initializes lobby via first player as a host
    public GenericCtx newGame(String uuid) {
        if (this.players.size()>0) {
            return new GenericCtx(false, "Игра уже началась");
        }
        PlayerManager p = new PlayerManager("Player 1", uuid, 1000);
        p.giveCard(table.popDeck());
        p.giveCard(table.popDeck());
        this.players.put(uuid, p);
        currentPlayer = uuid;
        return new GenericCtx(true, "");
    }


    public GenericCtx joinBot(String uuid)  {
        if (!authPlayer(uuid) || !players.keySet().stream().toList().get(0).equals(uuid)) {
            return new GenericCtx(false, "Вы не админ");
        }
        if (isActive) {
            return new GenericCtx(false, "Игра уже началась");
        }
        if (this.players.size()>=8) {
            return new GenericCtx(false, "В лобби уже 8 игроков");
        }
        String botuuid = UUID.randomUUID().toString();
        var p = new AIPlayerManager(String.format("Bot %d", this.players.size()+1), botuuid, 1000);
        p.giveCard(table.popDeck());
        p.giveCard(table.popDeck());
        this.players.put(botuuid, p);
        return new GenericCtx(true, "");
    }
    public GenericCtx joinGame(String uuid) {
        if (authPlayer(uuid)) {
            return new GenericCtx(true, "");
        }
        if (isActive) {
            return new GenericCtx(false, "Игра уже началась");
        }
        if (this.players.size()>=8) {
            return new GenericCtx(false, "В лобби уже 8 игроков");
        }
        var p = new PlayerManager(String.format("Player %d", this.players.size()+1), uuid, 1000);
        p.giveCard(table.popDeck());
        p.giveCard(table.popDeck());
        this.players.put(uuid, p);
        return new GenericCtx(true, "");
    }

    public FrameCtx getFrame(String uuid) {
        if(!authPlayer(uuid)) {
            return new FrameCtx(null, new ArrayList<>());
        }
        TableCtx table = new TableCtx(this.table.getBankPool(),this.table.getDeposit(),this.table.getFlow());
        FrameCtx frame = new FrameCtx(table, this.evtLoop);
        for (var p: this.players.entrySet()) {
            String puuid = p.getKey();
            IPlayerManager player = p.getValue();
            PlayerCtx ctx = new PlayerCtx(player.getName(), player.getBank(), player.getDeposit(),
                    player.isFolded(), puuid.equals(this.currentPlayer));
            if (Objects.equals(puuid, uuid)) {
                frame.setCurrentPlayer(ctx, player.peek());
                frame.setAdmin(Objects.equals(uuid, players.keySet().stream().toList().get(0)));
            }else{
                frame.addPlayer(ctx);
            }
        }
        return frame;
    }

    public GenericCtx terminate(String uuid) {
        if(!authPlayer(uuid) || !players.keySet().stream().toList().get(0).equals(uuid)) {
            return new GenericCtx(false, "Вы не админ");
        }
        this.isActive = false;
        this.table = new Table();
        this.players = new TreeMap<>();
        this.currentPlayer = "";
        this.evtLoop = new ArrayList<>();
        return new GenericCtx(true, "");
    }

    //endregion
}

package poker2077;

import poker2077.ApiObjects.*;
import poker2077.ent.Event;
import poker2077.ent.Table;

import java.util.*;


// Место, где шизофрения встречает азарт
public class GameManager {

    final static String[] comboNames = {
            "-",
            "Старшая карта",
            "Пара",
            "Две пары",
            "Тройка",
            "Стрит",
            "Флэш",
            "Фулл Хаус",
            "Каре",
            "Стрит-флэш",
            "Флэш-рояль",
            "11 не существует, но если будет супер-редкий баг..."
    }; // Это для уведомлений
    protected Table table;
    protected boolean isActive;
    protected Map<String, PlayerManagerI> players = new HashMap<>(); // для аутентификации UUID -> Юзер

    protected List<String> playerList = new ArrayList<>(); // список UUID для соблюдения порядка ходов (Map же жмыхнутый)

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
        if (this.playerList.size() == 1) {
            return;
        }

        // Наш любимый onLoop
        for(var p: this.players.values()) {
            p.onLoop(this.evtLoop.get(this.evtLoop.size()-1), this);
        }

        // Берем следующего игрока из списка (циклично)
        this.currentPlayer = this.playerList.get((this.playerList.indexOf(this.currentPlayer) + 1) % this.playerList.size());
        var curP = this.players.get(this.currentPlayer);
        System.out.println("Current player: " + curP.getName());
        // Хост начинает, хост и заканчивает: если после полного круга у нас на столе 5 карт, то заканчиваем игру
        if (Objects.equals(this.currentPlayer, this.playerList.get(0))) {
            if (this.table.getFlow().size()==5) {
                endGame();
                return;
            }
            // иначе, извольте терн
            this.table.addFlow();
        }

        // Если игрок вышел, то пинаем следующего
        if (curP.isFolded()) {
            nextPlayer();
        }else{
            // Боты бип буп
            curP.automate(this);
        }
    }

    private void endGame() {
        // Пока мы считаем, никто не играет
        this.currentPlayer = "";
        emitEvent(new Event(Event.EventType.CALL, "Конец игры. Подсчет карт...", 0));
        String bestPlayerUUID = "";
        int bestValue = 0;
        for(var puuid: playerList) {
            var p = players.get(puuid);
            // если ты вышел, то и считать нечего
            if (p.isFolded())
                continue;
            // Сейчас мы узнаем что за комбо у игрока
            int val = CardComboChecker.checkComboValue(p.peek(), table.getFlow());
            if (val>0) {
                // Если комбо есть, то это стоит отпраздновать
                emitEvent(new Event(Event.EventType.RAISE, p.getName() + " - " + comboNames[val], val));
            }
            if (val>bestValue) {
                // Если комбо лучше, то логично что лучше
                bestValue = val;
                bestPlayerUUID = puuid;
            } else if (val==bestValue) {
                // Если комбо одинаковые у игроков, то ищем у кого старшая карта
                var myCard = CardComboChecker.pGetComboHighCard(new ArrayList<>(p.peek()));
                var bestCard = CardComboChecker.pGetComboHighCard(new ArrayList<>(players.get(bestPlayerUUID).peek()));

                // Так как enum идет от туза до 1, то id туза самое маленькое
                if (myCard.ordinal()<bestCard.ordinal()) {
                    bestPlayerUUID = puuid;
                }
            }
        }
        var p = players.get(bestPlayerUUID);
        emitEvent(new Event(Event.EventType.CALL, "Конец игры. Победил " + p.getName(), 0));
        p.setBank(p.getBank()+table.getBankPool()); // Честные выплаты
        table = new Table(); // Сброс стола и подготовка к новой игре
        for(var pl: players.values()) {
            pl.reset();
            pl.giveCard(table.popDeck());
            pl.giveCard(table.popDeck());
        }
        isActive = false;
        currentPlayer = playerList.get(0); // Первый опять хост, но если он банкврот, то следующий
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
        if (player.isFolded())
            return new GenericCtx(false, "Вы уже вышли (фолд)");
        if (sum>player.getBank())
            return new GenericCtx(false, "Недостаточно средств");
        if (this.table.getDeposit()==0) {
            // Если депозит = 0, то это первый ход хоста. Поэтому помечаем игру как активную и выкладываем флоп
            isActive = true;
            this.table.addFlow();
            this.table.addFlow();
            this.table.addFlow();
        }
        this.table.setBankPool(this.table.getBankPool()+sum);
        this.table.setDeposit(this.table.getDeposit()+sum);
        player.setBank(player.getBank()-sum);
        player.setDeposit(player.getDeposit()+sum);
        this.emitEvent(new Event(Event.EventType.RAISE,player.getName()+": рейз (+"+sum+")", sum));
        this.nextPlayer();
        return new GenericCtx(true, "");
    }

    public GenericCtx call(String uuid) {
        if (!this.authPlayer(uuid))
            return new GenericCtx(false, "Вы не в игре");
        if (!Objects.equals(this.currentPlayer, uuid))
            return new GenericCtx(false, "Не ваш ход");
        var player = this.players.get(uuid);
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
        this.emitEvent(new Event(Event.EventType.CALL,player.getName()+": колл", 0));
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
        this.emitEvent(new Event(Event.EventType.FOLD, player.getName()+": сделал фолд", 0));
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
        this.playerList.add(uuid);
        currentPlayer = uuid;
        return new GenericCtx(true, "");
    }


    public GenericCtx joinBot(String uuid)  {
        if (!authPlayer(uuid) || !playerList.get(0).equals(uuid)) {
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
        this.playerList.add(botuuid);
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
        this.playerList.add(uuid);
        return new GenericCtx(true, "");
    }

    public FrameCtx getFrame(String uuid) {
        if(!authPlayer(uuid)) {
            return new FrameCtx(null, new ArrayList<>());
        }
        TableCtx table = new TableCtx(this.table.getBankPool(),this.table.getDeposit(),this.table.getFlow());
        FrameCtx frame = new FrameCtx(table, this.evtLoop);
        for (var puuid: this.playerList) {
            PlayerManagerI player = this.players.get(puuid);
            PlayerCtx ctx = new PlayerCtx(player.getName(), player.getBank(), player.getDeposit(), player.isFolded(), puuid==this.currentPlayer);
            if (Objects.equals(puuid, uuid)) {
                frame.setCurrentPlayer(ctx, player.peek());
                frame.setAdmin(Objects.equals(uuid, playerList.get(0)));
            }else{
                frame.addPlayer(ctx);
            }
        }
        return frame;
    }

    public GenericCtx terminate(String uuid) {
        if(!authPlayer(uuid) || !playerList.get(0).equals(uuid)) {
            return new GenericCtx(false, "Вы не админ");
        }
        this.isActive = false;
        this.table = new Table();
        this.playerList = new ArrayList<>();
        this.players = new HashMap<>();
        this.currentPlayer = "";
        this.evtLoop = new ArrayList<>();
        return new GenericCtx(true, "");
    }

    //endregion
}

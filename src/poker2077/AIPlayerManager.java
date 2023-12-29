package poker2077;

import poker2077.ent.Event;

public class AIPlayerManager extends PlayerManager {
    private double stress; //0-1

    public AIPlayerManager(String name, String uuid, long bank) {
        super(name, uuid, bank);
        this.stress = 0.25;
    }

    @Override
    public void reset() {
        super.reset();
        stress = 0.25;
    }

    @Override
    public void onLoop(Event e, GameManager mgr) {
        // onLoop вызывается при каждом ходе любого игрока, чтобы корректировать стресс бота
        if (this.isFolded())
            return;
        switch (e.type) {
            case FOLD:
                stress-=0.1;
                break;
            case RAISE:
                stress += 0.5*((double) e.amount/mgr.table.getDeposit()); // 1 = 0.5*2
                break;
        }
    }

    @Override
    public void automate(GameManager mgr) {
        // Automobile: вызывается при каждом ходе бота, чтобы бот ходил
        if (this.isFolded())
            return;
        if (stress>=1) {
            // Если нервы сдали, то лучше выйти
            mgr.fold(this.state.uuid);
            return;
        }
        if (stress<=0.2) {
            // Если все фолдятся, то почему бы не сделать что-то тупое и не внести 20% своего депозита?
            mgr.raise(this.state.uuid, Math.round(this.state.bank*0.2));
            return;
        }
        // Обстановка нейтральная, чек
        mgr.call(this.state.uuid);
    }
}

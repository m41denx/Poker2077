# Poker2077

Инструкция:
- Запустить
- Перейти в http://localhost:8000
- Создать лобби
- Добавить ботов или подождать когда присоединятся другие игроки (уже не localhost, а ip вашего пк)
- Играть и выигрывать
- Если игрок ходит, то его имя становится зеленым
- Если игрок вышел из игры (фолд), то фон его карточки становится красным
- Игру завершить может только хост (если вы потеряете сессию хоста, то тут поможет только перезапуск программы).

Баги:
- Так как состояние на фронте обновляется каждую секунду и пересоздается DOM, то фокус с поля рейза постоянно слетает
- Игроки не покидают игру, они просто навсегда остаются в состоянии фолда
- Если вы останетесь один, то будете играть сами с собой до бесконечности
- Если игрок ушел, то игра "застрянет", так как игроки не могут быть удалены
- В конце раунда карты на руках других игроков не показываются: почему у бота каре, можно лишь гадать (исправляемо)
- Если вы забыли прочитать уведомления (особенно в конце игры когда их много), то перезагрузите страницу

---
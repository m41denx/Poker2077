const m = {
    h: "❤️",
    d: "♦️",
    c: "♣️",
    s: "♠️"
}

let loopid = 0;
let amount = 0
const getStatus = async () => {
    const statusBlk = hget("status")
    let status = await fetch("/status").then(r=>r.json())
    let pstfx = `<p>Игроков: ${status.players||0}</p>
        <a class="px-4 py-2 bg-blue-600 rounded-lg cursor-pointer hover:bg-blue-700" onClick="joinGame()">Присоединиться</a>`
    if (status.players==0) {
        pstfx = `<a class="px-4 py-2 bg-green-600 rounded-lg cursor-pointer hover:bg-green-700" onclick="newGame()">Создать лобби</a>`
    }
    statusBlk.innerHTML = `<p class="text-3xl mb-4">Poker 2077</p>${pstfx}`
}


const newGame = async ()=> {
    let v = await fetch("/new").then(r=>r.json())
    if(v.success)
        window.location.href ='./game.html'
    else
        alert(v.error)
}

const joinGame = async ()=> {
    let v = await fetch("/join").then(r=>r.json())
    if(v.success)
        window.location.href ='./game.html'
    else
        alert(v.error)
}

const createNotification = (msg, timeout=5000) => {
    let popper = hget("popper")
    let blk = document.createElement("div")
    blk.className = "bg-black bg-opacity-70 rounded-lg p-2 text-white"
    blk.innerText = msg
    popper.appendChild(blk)
    setTimeout(()=>popper.removeChild(blk), timeout)
}


// Board
const getFrame = async ()=>{
    let v = await fetch("/getframe").then(r=>r.json())
    if(!v.currentPlayer)
        window.location.href="/"
    hget("players").innerHTML=''
    fillSelf(v.currentPlayer, v.isAdmin)
    fillPlayers(v.players)
    fillHand(v.hand)
    setFlow(v.table.flow)
    setStakes(v.table)
    readEventLoop(v.evtLoop)
    setTimeout(getFrame, 1000)
}

const readEventLoop = (loop) => {
    uloop = loop.slice(loopid)
    loopid = loop.length
    uloop.forEach((e)=> {
        createNotification(e.text)
    })
}

const endGame = async () => {
    let v = await fetch("/end").then(r=>r.json())
    if(!v.success)
        createNotification(v.error)
    else
        createNotification("Игра завершена")
}

const addBot = async () => {
    let v = await fetch("/joinbot").then(r=>r.json())
    if(!v.success)
        createNotification(v.error)
    else
        createNotification("Бот добавлен")
}

const doFold = async () => {
    let v = await fetch("/fold").then(r=>r.json())
    if(!v.success)
        createNotification(v.error)
    else
        createNotification("Фолд...")
}

const doCall = async () => {
    let v = await fetch("/call").then(r=>r.json())
    if(!v.success)
        createNotification(v.error)
    else
        createNotification("Колл...")
}

const doRaise = async () => {
    let val = parseInt(hget("ramount").value)
    if(!val) {
        createNotification("Выберите ненулевую ставку")
        return

    }
    let v = await fetch(`/raise?amount=${val}`).then(r=>r.json())
    if(!v.success)
        createNotification(v.error)
    else
        createNotification("Рейз...")
}


const fillHand = (hand) => {
    if(hand.length==0) return
    let handy = hget("hand")
    handy.innerHTML = ""
    handy.innerHTML+=smallCardGen(m[hand[0].type], hand[0].rank, -12)
    handy.innerHTML+=smallCardGen(m[hand[1].type], hand[1].rank, 12)
}

const setFlow = (flow) => {
    let flowBlk = hget("flow")
    flowBlk.innerHTML = ""
    for(let card of flow) {
        flowBlk.innerHTML+=smallCardGen(m[card.type], card.rank, 0, 0, false)
    }
}

const setStakes = (table) => {
    let stakesBlk = hget("stakes")
    stakesBlk.innerText = `Банк: ${table.pool} | Макс. ставка: ${table.deposit}`
}

const smallCardGen = (sym, val, rot=0, mb=0, hover=true) => {
    rot=(rot>=0?"rotate-"+rot:"-rotate-"+(rot*-1))
    return `<div class="bg-white rounded-xl p-2 w-32 h-48 relative flex justify-center items-center border-solid border-black border-2 transition-all duration-300  ${rot} -translate-y-${mb} cursor-pointer ${hover&&`hover:-translate-y-${mb+4} hover:w-[9rem] hover:h-[13.5rem]`} drop-shadow-2xl">
    <span class="text-xl absolute top-2 left-2">${val}</span>
    <span class="text-xl absolute bottom-2 right-2 rotate-180">${val}</span>
    
    <div class="relative">
      <span class="text-2xl">${sym}</span>
      <span class="text-2xl absolute top-0 left-0 ${hover&&"animate-ping"}">${sym}</span>
    </div>
  </div>`
}

const fillSelf = (player, isAdmin=false) => {
    let plh = hget("players")
    hget("bal").innerText = player.balance
    if(isAdmin) {
        plh.innerHTML+= `<div class="text-white bottom-8 left-8 p-2 absolute flex flex-col w-fit items-center gap-2 bg-black bg-opacity-50 rounded-xl border-solid border-[1px] border-slate-400">
        <span class="text-lg">Админ панель</span>
        <div class="flex gap-2 items-center">
            <p class="px-8 py-2 rounded-lg px bg-blue-500 flex justify-center cursor-pointer hover:bg-blue-700" onclick="addBot()">Добавить бота</p>
            <p class="px-8 py-2 rounded-lg px bg-red-500 flex justify-center cursor-pointer hover:bg-red-700" onclick="endGame()">Закончить игру</p>
        </div>
    </div>`
    }
}


const fillPlayers = (players)=>{
    let pos = [
        "bottom-40 left-24",
        "top-64 left-16",
        "top-24 left-64",
        "top-16 left-[50%] -translate-x-[50%]",
        "top-24 right-64",
        "top-64 right-16",
        "bottom-40 right-24",
    ]
    let plh = hget("players");
    for(let i=0;i<players.length;i++) {
        plh.innerHTML += `
    <div class="${pos[i]} p-2 pr-4 absolute flex w-fit items-center gap-4 ${players[i].folded?"bg-red-700":"bg-black"} bg-opacity-50 rounded-xl border-solid border-[1px] border-slate-400">
        <img src="img/user.png" class="h-24" />
        <div class="text-white">
            <p class="text-2xl ${players[i].isActive&&"text-green-500"}">${players[i].name}</p>
            <p class="ml-4">Банк: $<span>${players[i].balance}</span></p>
            <p class="ml-4">Депозит: $<span>${players[i].deposit}</span></p>
        </div>
    </div>`
    }
}



const hget = (el) => {
    return document.getElementById(el)
}
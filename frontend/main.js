var COLLAPSED = true
const box = document.getElementById("box")
const hand = document.getElementById("hand")
const combos = document.getElementById("combos")
const syms = ["❤️","♦️","♣️","♠️"]
const vals = ["A","K","Q","J","10","9","8","7","6","5","4","3","2","1"]

const collapse = () => {
    if(COLLAPSED) {
        combos.style.height="auto"
    }else{
        combos.style.height="2rem"
    }
    COLLAPSED=!COLLAPSED
}

const cardGen = (sym, val) => {
    return `<div class="bg-white rounded-xl p-2 w-64 h-96 relative flex justify-center items-center border-solid border-black border-2 transition-all duration-300 hover:-translate-y-4 hover:-translate-x-4 drop-shadow-2-xl">
    <span class="text-3xl absolute top-2 left-2">${val}</span>
    <span class="text-3xl absolute bottom-2 right-2 rotate-180">${val}</span>
    
    <span class="text-6xl">${sym}</span>
  </div>`
}


const nanoCardGen = (sym, val, muted=false) => {
    let sz= (true?"w-8 h-12":"w-12 h-16")
    return `<div class="bg-white rounded-md ${sz} relative flex justify-center items-center border-solid border-black border-[1px] ${muted&&"opacity-60"}">
    <span class="text-sm absolute top-0 left-1">${val}</span>
    <span class="text-sm absolute bottom-0 right-1 rotate-180">${val}</span>
    
    <span class="text-lg">${sym}</span>
  </div>`
}


const genCombo = (name, combos) => {
    let data = combos.join(`<span class="text-xl text-white">+</span>`)
    return `<div class="my-1 flex items-center gap-1"><span class="text-white text-lg mr-auto">${name}:</span>${data}</div>`
}



combos.innerHTML+=genCombo("Флэш-Рояль",[
    nanoCardGen("♦️", "10"),
    nanoCardGen("♦️", "J"),
    nanoCardGen("♦️", "Q"),
    nanoCardGen("♦️", "K"),
    nanoCardGen("♦️", "A"),
])
combos.innerHTML+=genCombo("Стрит-Флэш",[
    nanoCardGen("♣️", "3"),
    nanoCardGen("♣️", "4"),
    nanoCardGen("♣️", "5"),
    nanoCardGen("♣️", "6"),
    nanoCardGen("♣️", "7"),
])
combos.innerHTML+=genCombo("Каре",[
    nanoCardGen("♦️", "A"),
    nanoCardGen("❤️", "A"),
    nanoCardGen("♠️", "A"),
    nanoCardGen("♣️", "A"),
    nanoCardGen("❤️", "3"),
])
combos.innerHTML+=genCombo("Фулл хаус",[
    nanoCardGen("♠️", "Q"),
    nanoCardGen("❤️", "Q"),
    nanoCardGen("♦️", "Q"),
    nanoCardGen("♠️", "6"),
    nanoCardGen("♣️", "6"),
])
combos.innerHTML+=genCombo("Флэш",[
    nanoCardGen("♦️", "3"),
    nanoCardGen("♦️", "A"),
    nanoCardGen("♦️", "8"),
    nanoCardGen("♦️", "10"),
    nanoCardGen("♦️", "J"),
])
combos.innerHTML+=genCombo("Стрит",[
    nanoCardGen("♠️", "3"),
    nanoCardGen("❤️", "4"),
    nanoCardGen("♦️", "5"),
    nanoCardGen("♣️", "6"),
    nanoCardGen("❤️", "7"),
])
combos.innerHTML+=genCombo("Тройка (сет)",[
    nanoCardGen("♣️", "3", true),
    nanoCardGen("♠️", "9", ),
    nanoCardGen("❤️", "9"),
    nanoCardGen("♦️", "9"),
    nanoCardGen("♣️", "5", true),
])
combos.innerHTML+=genCombo("Две пары",[
    nanoCardGen("♣️", "3", true),
    nanoCardGen("♠️", "9", true),
    nanoCardGen("❤️", "9"),
    nanoCardGen("♦️", "5"),
    nanoCardGen("♣️", "5", true),
])
combos.innerHTML+=genCombo("Пара",[
    nanoCardGen("♠️", "9", true),
    nanoCardGen("❤️", "10", true),
    nanoCardGen("♣️", "6"),
    nanoCardGen("♣️", "6"),
    nanoCardGen("♦️", "2", true),
])
combos.innerHTML+=genCombo("Старшая карта",[
    nanoCardGen("♦️", "4", true),
    nanoCardGen("♣️", "6", true),
    nanoCardGen("♦️", "K"),
    nanoCardGen("❤️", "8", true),
    nanoCardGen("♣️", "3", true),
])
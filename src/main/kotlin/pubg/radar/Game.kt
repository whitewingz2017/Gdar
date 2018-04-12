package pubg.radar

import pubg.radar.sniffer.Sniffer
import pubg.radar.ui.GLMap
import java.util.Collections.newSetFromMap
import java.util.concurrent.ConcurrentHashMap

const val mapWidth = 819200f

var gameStarted = false
var isErangel = true
var haveEncryptionToken = false
var EncryptionToken = ByteArray(24)

interface GameListener {
    fun onGameStart() {}
    fun onGameOver()
}

private val gameListeners = newSetFromMap(ConcurrentHashMap<GameListener, Boolean>())

fun register(gameListener : GameListener)
{
  gameListeners.add(gameListener)
}

fun deregister(gameListener: GameListener) {
    gameListeners.remove(gameListener)
}

fun ForceRestart() {
    val map = isErangel
    gameOver()
    isErangel = map
    gameStart()
}

fun gameStart() {
    println("New Game is Starting")
    gameStarted = true
    gameListeners.forEach { it.onGameStart() }
}

fun gameOver() {
  gameStarted = false
  haveEncryptionToken = false
  EncryptionToken.fill(0)
  gameListeners.forEach { it.onGameOver() }
}

lateinit var Args: Array<String>
fun main(args: Array<String>) {
    Args = args
    when {
        args.size < 3 -> {
            println("usage: <ip> <sniff option> <gaming pc>")
            System.exit(-1)

        }
        args.size > 3 -> {

            println("Loading PCAP File.")

            Sniffer.sniffLocationOffline()
            val ui = GLMap()
            ui.show()
        }
        else -> {
            Sniffer.sniffLocationOnline()
            val ui = GLMap()
            ui.show()
        }
    }
}
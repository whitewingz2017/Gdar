package pubg.radar.struct.cmd

import pubg.radar.GameListener
import pubg.radar.deserializer.ROLE_MAX
import pubg.radar.register
import pubg.radar.struct.Actor
import pubg.radar.struct.Bunch
import pubg.radar.struct.NetGuidCacheObject
import pubg.radar.struct.NetworkGUID
import pubg.radar.struct.cmd.CMD.propertyBool
import pubg.radar.struct.cmd.CMD.propertyByte
import pubg.radar.struct.cmd.CMD.propertyFloat
import pubg.radar.struct.cmd.CMD.propertyInt
import pubg.radar.struct.cmd.CMD.propertyNetId
import pubg.radar.struct.cmd.CMD.propertyObject
import pubg.radar.struct.cmd.CMD.propertyString
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

object PlayerStateCMD : GameListener {
    init {
        register(this)
    }

    override fun onGameOver() {
        playerNames.clear()
        playerNumKills.clear()
		uniqueIds.clear()
        teamNumbers.clear()
        attacks.clear()
		//selfSpectated = NetworkGUID(0)
        selfID = NetworkGUID(0)
        selfStateID = NetworkGUID(0)
    }

    val playerNames = ConcurrentHashMap<NetworkGUID, String>()
    val playerNumKills = ConcurrentHashMap<NetworkGUID, Int>()
	val playerSpec = ConcurrentHashMap<NetworkGUID, Int>()
    val uniqueIds = ConcurrentHashMap<String, NetworkGUID>()
    val teamNumbers = ConcurrentHashMap<NetworkGUID, Int>()
    val attacks = ConcurrentLinkedQueue<Pair<NetworkGUID, NetworkGUID>>()//A -> B
    var selfID = NetworkGUID(0)
    var selfStateID = NetworkGUID(0)
    var selfSpectated = 0
    

    fun process(actor: Actor, bunch: Bunch, repObj: NetGuidCacheObject?, waitingHandle: Int, data: HashMap<String, Any?>): Boolean {
        with(bunch) {
            when (waitingHandle) {
			    7 -> {
                    val result = readBit()
                }
                8 -> {
                    val bHidden = readBit()
//          println("bHidden=$bHidden")
                }
                9 -> {
                    val bReplicateMovement = readBit()
//          println("bHidden=$bReplicateMovement")
                }
                10 -> {
                    val bTearOff = readBit()
//          println("bHidden=$bTearOff")
                }
				12 -> {
                    val (ownerGUID, owner) = propertyObject()
                }
                13 -> {
                    val role = readInt(ROLE_MAX)
                    val b = role
                }
				14 -> {
                    val result = readBit()
					}
                15 -> {
                    val result = propertyInt()
					}
				16 -> {
                    val bFromPreviousLevel = propertyBool()
//          println("${actor.netGUID} bFromPreviousLevel=$bFromPreviousLevel")
                }
                17 -> {
                    val isABot = propertyBool()
//          println("${actor.netGUID} isABot=$isABot")
                }
                18 -> {
                    val bIsInactive = propertyBool()
//          println("${actor.netGUID} bIsInactive=$bIsInactive")
                }
                19 -> {
                    val bIsSpectator = propertyBool()
//          println("${actor.netGUID} bIsSpectator=$bIsSpectator")
                }
                20 -> {
                    val bOnlySpectator = propertyBool()
//          println("${actor.netGUID} bOnlySpectator=$bOnlySpectator")
                }
				21 -> {
                    val ping = propertyByte()
                }
                22 -> {
                    val playerID = propertyInt()
//          println("${actor.netGUID} playerID=$playerID")
                }
				23 -> {
                    val name = propertyString()
                    playerNames[actor.netGUID] = name
                      println("${actor.netGUID} playerID=$name")
                }
                24 -> {
                    val score = propertyFloat()
                }
                25 -> {
                    val StartTime = propertyInt()
          println("25 ${actor.netGUID} StartTime=$StartTime")
                }
                26 -> {
                    val uniqueId = propertyNetId()
                    uniqueIds[uniqueId] = actor.netGUID
          println("26 ${playerNames[actor.netGUID]}${actor.netGUID} uniqueId=$uniqueId")
                }
				 27 -> {
                    val AccountId = propertyString()
//          println("${actor.netGUID} AccountId=$AccountId")
                }
				28 -> {
                    val bIsInAircraft = propertyBool()
                } 
				29 -> {
                    val bIsZombie = propertyBool()
                }
				30 -> {
                    val currentAttackerPlayerNetId = propertyString()
                    attacks.add(Pair(uniqueIds[currentAttackerPlayerNetId]!!, actor.netGUID))
                }
				31 -> {//LastHitTime
                    val lastHitTime = propertyFloat()
                }
				33 -> {
                    val ObserverAuthorityType = readInt(4)
                }
                34 -> {
                    val scoreByDamage = propertyFloat()
                }
                35 -> {
                    val ScoreByKill = propertyFloat()
                }
                36 -> {
                    val ScoreByRanking = propertyFloat()
                }
                37 -> {
                    val ScoreFactor = propertyFloat()
                }
                38 -> {
                    val NumKills = propertyInt()
                    playerNumKills[actor.netGUID] = NumKills
					//println("NUM KILLS: $NumKills")
                }
				39 -> {
                    val HeadShots = propertyInt()
                }
				40 -> {
                    val LongestDistanceKill = propertyFloat()
                }
				41 -> {
                    val TotalGivenDamages = propertyFloat()
                }
				42 ->
				{
                    val TotalMovedDistanceMeter = propertyFloat()
                    selfStateID = actor.netGUID//only self will get this update
//          selfID = actor.netGUID//only self will get this update
                }
				43 -> {		//indicate player's death
                    val Ranking = propertyInt()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} Ranking=$Ranking")
                }
                44   ->
				{//TArray<struct FReplicatedCastableItem> ReplicatedCastableItems
					return false
				}
				45 ->
				{//ReplicatedEquipableItems
                    return false
                }
				46 -> {
                    val ReportToken = propertyString()
                }
                47 -> {
                    val teamNumber = readInt(100)
                    teamNumbers[actor.netGUID] = teamNumber
					println("48 TNum: $teamNumber")
                }
				else -> return false
            }
        }
        return true
    }
}

package wumo.pubg.struct.cmd

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import pubg.radar.GameListener
import pubg.radar.bugln
import pubg.radar.register
import pubg.radar.struct.Actor
import pubg.radar.struct.Bunch
import pubg.radar.struct.NetGuidCacheObject
import pubg.radar.struct.NetworkGUID
import pubg.radar.struct.cmd.CMD.propertyString
import pubg.radar.struct.cmd.CMD.propertyVector100
import java.util.concurrent.ConcurrentHashMap

object TeamCMD : GameListener {
    val team = ConcurrentHashMap<String, String>()
    val mapMakersByName = ConcurrentHashMap<NetworkGUID, Vector3>()
	val teamMemberNumber = ConcurrentHashMap<NetworkGUID, Int>()
	val teamNumber = ConcurrentHashMap<NetworkGUID, Int>()
	val teamNumberss = ConcurrentHashMap<NetworkGUID, Int>()
	val playerNames = ConcurrentHashMap<String, String>()


    init {
        register(this)
    }

    override fun onGameOver() {
        team.clear()
    }

    fun process(actor: Actor, bunch: Bunch, repObj: NetGuidCacheObject?, waitingHandle: Int, data: HashMap<String, Any?>): Boolean {
        with(bunch) {
            //      println("${actor.netGUID} $waitingHandle")
            when (waitingHandle) {
                5 -> {
                    val (netGUID, obj) = readObject()
                    actor.owner = if (netGUID.isValid()) netGUID else null
                    bugln { " owner: [$netGUID] $obj ---------> beOwned:$actor" }
                }
				26 -> {
                    val markerLocation = propertyVector100()
                    mapMakersByName[actor.netGUID] = markerLocation
					println("TMark: $mapMakersByName")
                }
				27   ->
					{//MemberNumber
					val teamNumber = readInt(15)
					teamNumberss[actor.netGUID] = teamNumber
					//println("${playerNames}${actor.netGUID} TeamNumber=$teamNumber \n")
					}
                28 -> {
                    val playerLocation = propertyVector100()
                }
				29 -> {
                    val name = propertyString()
					team[name] = name
					playerNames[name] = name
					//query(name)
					println("$(actor.netGUID} playerID=$name $teamNumber")
					}
                30 -> {
                    val playerRotation = readRotationShort()
                }
                
                
				
				32 ->
				{
				//SquadNumber
					var SquadMemberIndex = propertyInt()
					println("32 SID: $SquadMemberIndex")
					}
                else -> return false
            }
            return true
        }
    }
}
package pubg.radar.struct.cmd

import com.badlogic.gdx.math.Vector2
import pubg.radar.GameListener
import pubg.radar.register
import pubg.radar.struct.Actor
import pubg.radar.struct.Bunch
import pubg.radar.struct.NetGuidCacheObject
import pubg.radar.struct.cmd.CMD.propertyBool
import pubg.radar.struct.cmd.CMD.propertyByte
import pubg.radar.struct.cmd.CMD.propertyFloat
import pubg.radar.struct.cmd.CMD.propertyInt
import pubg.radar.struct.cmd.CMD.propertyName
import pubg.radar.struct.cmd.CMD.propertyObject
import pubg.radar.struct.cmd.CMD.propertyString
import pubg.radar.struct.cmd.CMD.propertyVector


object GameStateCMD: GameListener {
    init {
        register(this)
    }

    override fun onGameOver() {
        SafetyZonePosition.setZero()
        SafetyZoneRadius = 0f
        SafetyZoneBeginPosition.setZero()
        SafetyZoneBeginRadius = 0f
        PoisonGasWarningPosition.setZero()
        PoisonGasWarningRadius = 0f
        RedZonePosition.setZero()
        RedZoneRadius = 0f
        TotalWarningDuration = 0f
        ElapsedWarningDuration = 0f
        TotalReleaseDuration = 0f
        ElapsedReleaseDuration = 0f
        NumJoinPlayers = 0
        NumAlivePlayers = 0
        NumAliveTeams = 0
		bCanKillerSpectate = 0
        RemainingTime = 0
        MatchElapsedMinutes = 0
        isTeamMatch=false
    }

    var isTeamMatch=false
    var TotalWarningDuration = 0f
    var ElapsedWarningDuration = 0f
    var RemainingTime = 0
    var MatchElapsedMinutes = 0
    val SafetyZonePosition = Vector2()
    var SafetyZoneRadius = 0f
    val SafetyZoneBeginPosition = Vector2()
    var SafetyZoneBeginRadius = 0f
    val PoisonGasWarningPosition = Vector2()
    var PoisonGasWarningRadius = 0f
    val RedZonePosition = Vector2()
    var RedZoneRadius = 0f
    var TotalReleaseDuration = 0f
    var ElapsedReleaseDuration = 0f
    var NumJoinPlayers = 0
    var NumAlivePlayers = 0
    var NumAliveTeams = 0
	var bCanKillerSpectate = 0

    fun process(actor: Actor, bunch: Bunch, repObj: NetGuidCacheObject?, waitingHandle: Int, data: HashMap<String, Any?>): Boolean {
        try {
            with(bunch) {
                when (waitingHandle) {
					16 -> {
                        val bReplicatedHasBegunPlay = propertyBool()
                        val b = bReplicatedHasBegunPlay
                    }
                    17 -> {
                        val GameModeClass = propertyObject()
                        val b = GameModeClass
                    }
					18 -> {
                        val ReplicatedWorldTimeSeconds = propertyFloat()
                        val b = ReplicatedWorldTimeSeconds
                    }
                    19 -> {
                        val SpectatorClass = propertyObject()
                        val b = SpectatorClass
                    }
                     20 -> {
                        val ElapsedTime = propertyInt()
                        val b = ElapsedTime
                    }
                    21 -> {
                        val MatchState = propertyName()
                        val b = MatchState
                    }
					22   ->
					{//new for 3.7.27.18
						val bCanKillerSpectate = propertyBool()
					}
					23 ->
					{
						val bCanShowLastCircleMark = propertyBool()
					}
					24 -> propertyBool() //bIsCustomGame
					25 ->
					{
						val bIsGasRelease = propertyBool()
					}
					26 -> propertyBool() //bIsTeamElimination
					27   ->
					{
						isTeamMatch = propertyBool()
					}
					28 ->
					{
						val bIsWarMode = propertyBool()
					}
					29 -> propertyBool() //bIsWinnerZombieTeam
					30 -> propertyBool() //bIsWorkingBlueZone
					31 ->
					{
						val bIsZombieMode = propertyBool()
					}
					32 ->
					{
						val bShowAircraftRoute = propertyBool()
					}
					33 ->
					{
						val bShowLastCircleMark = propertyBool()
					}
					34 ->
					{
						val bTimerPaused = propertyBool()
						val b = bTimerPaused
					}
					35 -> propertyBool() //bUseWarRoyaleBluezone
					36 ->
					{
						val bUseXboxUnauthorizedDevice = propertyBool()
					}
					37 -> propertyBool() //bUsingSquadInTeam
					38 ->
					{
						ElapsedReleaseDuration = propertyFloat()
						val b = ElapsedReleaseDuration
					}
					39 ->
					{
						ElapsedWarningDuration = propertyFloat()
					}
					40 ->
					{
						val GoalScore = propertyInt()
					}
					/*41   ->
					{
						val LastCirclePosition = readVector2D()
						val b = LastCirclePosition
					}*/
					42 -> {
                        MatchElapsedMinutes = propertyInt()
                    }
					43 -> {
                        val MatchElapsedTimeSec = propertyBool()
                    }
					44 -> {
                        val MatchId = propertyString()
                        val b = MatchId
                    }
					45 -> {
                        val MatchShortGuid = propertyString()
                        val b = MatchShortGuid
                    }
					46   ->
					{
						val MatchStartType = propertyByte()
					}
					47   -> propertyFloat() //NextRespawnTimeSeconds 
					48   ->
					{
						NumAlivePlayers = propertyInt()
//        				  println(NumAlivePlayers)
					}
					49   ->
					{
						NumAliveTeams = propertyInt()
					}
					50   ->
					{
						val NumAliveZombiePlayers = propertyInt()
						val b = NumAliveZombiePlayers
					}
					51   ->
					{
						NumJoinPlayers = propertyInt()
					}
                    52   ->
					{
						val NumStartPlayers = propertyInt()
						val b = NumStartPlayers
					}
                    53   ->
					{
						val NumStartTeams = propertyInt()
						val b = NumStartTeams
					}
					54   ->
					{
						val NumTeams = propertyInt()
						val b = NumTeams
					}
					55   ->
					{
						val pos = propertyVector()
						PoisonGasWarningPosition.set(pos.x, pos.y)
					}
					56   ->
					{
						PoisonGasWarningRadius = propertyFloat()
					}
					57   ->
					{
						val pos = propertyVector()
						RedZonePosition.set(pos.x, pos.y)

						val b = RedZonePosition
					}
					58   ->
					{
						RedZoneRadius = propertyFloat()
						val b = RedZoneRadius
					}
                    59 -> {
                        RemainingTime = propertyInt()
                    }
					60   ->
					{
						val pos = propertyVector()
						SafetyZoneBeginPosition.set(pos.x, pos.y)
					}
					61   ->
					{
						SafetyZoneBeginRadius = propertyFloat()
					}
					62   ->
					{
						val pos = propertyVector()
						SafetyZonePosition.set(pos.x, pos.y)
					}
					63   ->
					{
						SafetyZoneRadius = propertyFloat()
					}
					64   -> readUInt16() //teamIds
					65 	 -> readUInt16() //TeamIndices
					66   -> readUInt16() //TeamLeaderNames
					67   -> readUInt16() //TeamScore
					68   -> propertyFloat() //TimeLimitSeconds
					69   ->
					{
						TotalReleaseDuration = propertyFloat()
						val b = TotalReleaseDuration
					}
					
					70   ->
					{
						TotalWarningDuration = propertyFloat()
					}
					
                    else -> return ActorCMD.process(actor, bunch, repObj, waitingHandle, data)
                }
                return true
            }
        }
        catch (e: Exception){ println("GameState is throwing somewhere: $e ${e.stackTrace} ${e.message}") }
        return false
    }
}
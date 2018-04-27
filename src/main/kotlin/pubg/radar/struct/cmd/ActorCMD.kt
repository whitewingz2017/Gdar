package pubg.radar.struct.cmd

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import pubg.radar.GameListener
import pubg.radar.bugln
import pubg.radar.deserializer.ROLE_MAX
import pubg.radar.deserializer.channel.ActorChannel.Companion.actors
import pubg.radar.deserializer.channel.ActorChannel.Companion.airDropLocation
import pubg.radar.deserializer.channel.ActorChannel.Companion.visualActors
import pubg.radar.deserializer.shortRotationScale
import pubg.radar.register
import pubg.radar.struct.Actor
import pubg.radar.struct.Archetype.*
import pubg.radar.struct.Bunch
import pubg.radar.struct.NetGUIDCache.Companion.guidCache
import pubg.radar.struct.NetGuidCacheObject
import pubg.radar.struct.NetworkGUID
import pubg.radar.struct.cmd.CMD.propertyBool
import pubg.radar.struct.cmd.CMD.propertyByte
import pubg.radar.struct.cmd.CMD.propertyFloat
import pubg.radar.struct.cmd.CMD.propertyInt
import pubg.radar.struct.cmd.CMD.propertyName
import pubg.radar.struct.cmd.CMD.propertyObject
import pubg.radar.struct.cmd.CMD.propertyVector
import pubg.radar.struct.cmd.CMD.propertyVector10
import pubg.radar.struct.cmd.CMD.propertyVector100
import pubg.radar.struct.cmd.CMD.propertyVectorNormal
import pubg.radar.struct.cmd.CMD.propertyVectorQ
import pubg.radar.struct.cmd.CMD.repMovement
import pubg.radar.struct.cmd.PlayerStateCMD.selfID
import pubg.radar.struct.cmd.PlayerStateCMD.playerNames
//import pubg.radar.struct.cmd.PlayerStateCMD.selfSpectated
import java.util.concurrent.ConcurrentHashMap

var selfDirection = 0f
val selfCoords = Vector3()
var selfAttachTo: Actor? = null
var selfSpectatedCount = 0


object ActorCMD : GameListener {
    init {
        register(this)
    }

    override fun onGameOver() {
        actorWithPlayerState.clear()
        playerStateToActor.clear()
        actorHealth.clear()
		playerSpectatedCounts.clear()
		//selfSpectated.clear()
		actorDowned.clear()
		actorBeingRevived.clear()
    }

    val actorWithPlayerState = ConcurrentHashMap<NetworkGUID, NetworkGUID>()
    val playerStateToActor = ConcurrentHashMap<NetworkGUID, NetworkGUID>()
    val actorHealth = ConcurrentHashMap<NetworkGUID, Float>()
	val playerSpectatedCounts = ConcurrentHashMap<NetworkGUID, Int>()
    val actorDowned = ConcurrentHashMap<NetworkGUID, Boolean>()
    val actorBeingRevived = ConcurrentHashMap<NetworkGUID, Boolean>()
	val actorAims = ConcurrentHashMap<NetworkGUID, Boolean>()

    fun process(actor: Actor, bunch: Bunch, repObj: NetGuidCacheObject?, waitingHandle: Int, data: HashMap<String, Any?>): Boolean {
        with(bunch) {
				//actorDowned[actor.netGUID] = false
				//actorBeingRevived[actor.netGUID] = false
            when (waitingHandle) {
				1 -> {
                    val (attachComponnent, attachName) = bunch.readObject()
                }
				2 -> {
                    val (a, obj) = readObject()
                    val attachTo = if (a.isValid()) {
                        actors[a]?.attachChildren?.put(actor.netGUID, actor.netGUID)
                        a
                    } else null
                    if (actor.attachParent != null)
                        actors[actor.attachParent!!]?.attachChildren?.remove(actor.netGUID)
                    actor.attachParent = attachTo
                    if (actor.netGUID == selfID) {
                        selfAttachTo = if (attachTo != null)
                            actors[actor.attachParent!!]
                        else
                            null
                    }
                    bugln { ",attachTo [$actor---------> $a ${guidCache.getObjectFromNetGUID(a)} ${actors[a]}" }
                }
				3 -> {
                    val attachSocket = propertyName()
                }
				4 -> {
                    val locationOffset = propertyVector100()
                    if (actor.Type == DroopedItemGroup) {
                        bugln { "${actor.location} locationOffset $locationOffset" }
                    }
                    bugln { ",attachLocation $actor ----------> $locationOffset" }
                }
                5 -> propertyVector100()
                6 -> readRotationShort()
                8 -> if (readBit()) {//bHidden
                    visualActors.remove(actor.netGUID)
                    bugln { ",bHidden id$actor" }
                }
                9 -> if (!readBit()) {// bReplicateMovement
                    if (!actor.isVehicle) {
                        visualActors.remove(actor.netGUID)
                    }
                    bugln { ",!bReplicateMovement id$actor " }
                }
                10 -> if (readBit()) {//bTearOff
                    visualActors.remove(actor.netGUID)
                    bugln { ",bTearOff id$actor" }
                }

                12 -> {
                    val (netGUID, obj) = readObject()
                    actor.owner = if (netGUID.isValid()) netGUID else null
                    bugln { " owner: [$netGUID] $obj ---------> beOwned:$actor" }
                }
				13 -> {
                    val role = readInt(ROLE_MAX)
                    val b = role
                }
                14 -> {
                    repMovement(actor)
                    with(actor) {
                        when (Type) {
                            AirDrop -> airDropLocation[netGUID] = location
                            Other -> {
                            }
                            else -> visualActors[netGUID] = this
                        }
                    }
                }
                15 -> {
                    readInt(ROLE_MAX)
                }
        
                16 -> propertyObject()
                17 -> {
                    val (playerStateGUID, playerState) = propertyObject()
                    if (playerStateGUID.isValid()) {
                        actorWithPlayerState[actor.netGUID] = playerStateGUID
                        playerStateToActor[playerStateGUID] = actor.netGUID

                    }
                }
                18 -> {//RemoteViewPitch 2
                    val result = readUInt16() * shortRotationScale//pitch
                }
                
            //ACharacter
                19 -> {
                    val result = propertyFloat()
                }
                20 -> {
                    val result = readBit()
                }
                21 -> {
                    val result = readInt32()
                }
                22 -> {
					val result = propertyFloat()
                    //val Rotation = readRotationShort()
                }//propertyRotator()
                23 -> {
                    val result = propertyName()
                }
                24 -> {
                    val Rotation = readRotationShort()
                }
                25 -> {
                    val result = propertyByte()
                }
                26 -> {
                    val result = readBit()
                }
                28 -> {
                    val result = propertyByte()
                }
                29 -> {
                    val result = propertyBool()
                }
                30 -> {
                    val result = propertyFloat()
                }
				31 -> {
                    val ReplicatedServerLastTransformUpdateTimeStamp = propertyFloat()
                }
				//struct FRepRootMotionMontage RepRootMotion;
                32 -> {
                    val result = propertyVector100()
                }
                33 -> {
                    val result = propertyObject()
                }
				34 -> {//player
                    val bHasAdditiveSources = readBit()
                    val bHasOverrideSources = readBit()
                    val lastPreAdditiveVelocity = propertyVector10()
                    val bIsAdditiveVelocityApplied = readBit()
                    val flags = readUInt8()
                }
                35 -> {
                    val result = propertyVector100()
                }
                36 -> {
                    val result = readRotationShort()
                }//propertyRotator()
                37 -> {
                    val result = propertyObject()
                }
                38 -> {
                    val result = propertyName()
                }
                39 -> {
                    val result = propertyBool()
                }
                40 -> {
                    val result = propertyBool()
                }
                42 -> {
                    val result = propertyVector10()
                }
                43 -> {
                    val result = propertyVector10()
                }
            //AMutableCharacter
                44 -> {
                    val arrayNum = readUInt16()
                    var index = readIntPacked()
                    while (index != 0) {
                        val value = readUInt8()
                        index = readIntPacked()
                    }
                }
				45 -> {
                    val AimOffsets = propertyVectorNormal()
                    val b = AimOffsets
                }
				46 -> {
                    val result = propertyInt()
                }
                47 -> {
                    val result = propertyFloat()
                }
                48 -> {
                    val bIsAimingRemote = propertyBool()
                }
                49 -> {
                    val result = propertyObject()
                }
				51 -> {
                    val result = propertyBool()
                }
                52 -> {
                    val bIsDowned=propertyBool()
                    actorDowned[actor.netGUID] = bIsDowned
					println("52: ${actor.netGUID} $bIsDowned")
                }
                53 -> {
                    val result = propertyBool()
                }
				56 -> {
				val (id, team) = propertyObject()
                    //val ActualDamage = propertyFloat()
                }
                57 -> {
                    val bIsReviving = propertyBool()
                    actorBeingRevived[actor.netGUID] = bIsReviving
					println("57: ${actor.netGUID} $bIsReviving")
                }
				58 -> {
					
					 val bIsScopingRemote = propertyBool()
                    actorAims[actor.netGUID] = bIsScopingRemote
					//val bIsReviving = propertyBool()
                    //actorBeingRevived[actor.netGUID] = bIsReviving
					if (actor.netGUID == selfID) {
					//println("AIM: $bIsScopingRemote")
					}
				}
				59 -> {
                    val DamageOrigin = propertyVectorQ()
                }
				60 -> {
                    val RelHitLocation = propertyVectorQ()
                }
                61 -> {
                    val result = propertyName()
                    val b = result
                }
                62 -> {
                    val DamageMaxRadius = propertyFloat()
                }
                63 -> {
                    val BoostGauge = propertyFloat()
                }
                64 -> {
                    val BoostGaugeMax = propertyFloat()
                }
                65 -> {
                    val result = propertyBool()
                }
                66 -> {
                    val result = propertyBool()
                }
                67 -> {
                    val bKilled = propertyBool()
                }
                68 -> {
                    val EnsureReplicationByte = propertyByte()
                }
                69 -> {
                    val AttackerWeaponName = propertyName()
                }
                70 -> {
                    val AttackerLocation = propertyVector()
                }
                71 -> {
                    val TargetingType = readInt(3)
                    val a = TargetingType
                }
                72 -> {
                    val reviveCastingTime = propertyFloat()
                    val a = reviveCastingTime
                }
                73 -> {
                    val result = propertyBool()
                    val b = result
                }
                74 -> {
                    val CharacterState = propertyByte()
                }
                75 -> {
                    val result = propertyBool()
                    val b = result
                }
                76 -> {
                    val GroggyHealth = propertyFloat()
                }
                77 -> {
                    val GroggyHealthMax = propertyFloat()
                }
                78 -> {
                    val result = propertyBool()
                    val b = result
                }
				79 -> {
                    val health = propertyFloat()
                    actorHealth[actor.netGUID] = health
                }
                80 -> {
                    val healthMax = propertyFloat()
                }
				81 -> {
                    val result = propertyBool()
                    val b = result
                }
				85 -> {
                    val result = propertyBool()
                    val b = result
                }
                86 -> {
                    val result = propertyBool()
                    val b = result
                }
                87 -> {
                    val result = propertyBool()
                    val b = result
                }
                88 -> {
                    val result = propertyBool()
                    val b = result
                }
                89 -> {
                    val result = propertyBool()
                    val b = result
                }
                90 -> {
                    val result = readRotationShort()//propertyRotator()
                    val b = result
                }
				92 -> {
                    val damageType = propertyObject()
                }
				93 -> {
                    val result = propertyBool()
                    val b = result
                }
                94 -> {
                    val PlayerInstigator = propertyObject()
                }
            //ATslCharacter
                100 -> {
                    val remote_CastAnim = readInt(8)
                }
                102 -> {
                    val ShoesSoundType = readInt(8)
                    val b = ShoesSoundType
                }
                103 -> {
                    //SpectatedCount
                    val spectated = propertyInt()
					selfSpectatedCount = spectated
					//println("Other: ${actor.netGUID} $spectated")
                    if (actor.netGUID == selfID) {
                        PlayerStateCMD.selfSpectated = spectated
						//println("SPECS: ${PlayerStateCMD.selfSpectated}")
                    }
                }
                104 -> {
                    val result = readInt(4)
                    val b = result
                }
                105 -> {
                    val result = propertyBool()
                    val b = result
                }
                106 -> {
                    val result = propertyBool()
                    val b = result
                }
                107 -> {
                    val result = propertyBool()
                    val b = result
                }
                else -> return false
            }
            return true
        }
    }
}

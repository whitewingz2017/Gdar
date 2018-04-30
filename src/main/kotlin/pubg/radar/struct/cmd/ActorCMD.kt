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
var spectatedCount = 0


object ActorCMD : GameListener {
    init {
        register(this)
    }

    override fun onGameOver() {
        actorWithPlayerState.clear()
        playerStateToActor.clear()
        actorHealth.clear()
		playerSpectatedCounts.clear()
		var spectatedCount = 0
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
	var spectatedCount = 0
    fun process(actor: Actor, bunch: Bunch, repObj: NetGuidCacheObject?, waitingHandle: Int, data: HashMap<String, Any?>): Boolean {
        with(bunch) {
				actorDowned[actor.netGUID] = false
				actorBeingRevived[actor.netGUID] = false
            when (waitingHandle) {
				1 -> {
                    val (attachComponnent, attachName) = bunch.readObject()
                }
				
				2-> {
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
                4 -> {
                    val role = readInt(ROLE_MAX)
                    val b = role
                }
				5 -> propertyVector100() //RelativeScale3D
				6 -> readRotationShort() //RotationOffset (end struct)
                7 -> readBit() //bCanBeDamage
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
				11 -> propertyObject() //Instigator
				12 -> {					// Owner
                    val (netGUID, obj) = readObject()
                    actor.owner = if (netGUID.isValid()) netGUID else null
                    bugln { " owner: [$netGUID] $obj ---------> beOwned:$actor" }
                }
                13 -> {			 		//RemoteRole
                    readInt(ROLE_MAX)
                }
				14 -> {					//struct FRepMovement
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
                15   -> readInt(ROLE_MAX) //Role
				16 -> {//RemoteViewPitch 2
                    val result = readUInt16() * shortRotationScale//pitch
                }
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
				19   -> propertyFloat() //AnimRootMotionTranslationScale
                20   -> propertyBool() //bIsCrouched
				21   -> propertyInt() //JumpMaxCount
				22   -> propertyFloat() //JumpMaxHoldTime
				23   -> propertyName() //struct FBasedMovementInfo ReplicatedBasedMovement | BoneName
				24   -> readRotationShort() //bRelativeRotation
				25   -> propertyBool() //bServerHasBaseComponent
				26   -> propertyBool() //bServerHasVelocity
				27   -> propertyVector100() //Location
				28   -> propertyObject() //MovementBase
				29   -> readRotationShort() //Rotation | end struct
				30   -> propertyByte() //ReplicatedMovementMode
				31 -> {
                    val ReplicatedServerLastTransformUpdateTimeStamp = propertyFloat()
                }
				32   -> propertyVector10() //struct FRepRootMotionMontage RepRootMotion | Acceleration
				33   -> propertyObject() //AnimMontag
				34 -> {//player
                    val bHasAdditiveSources = readBit()
                    val bHasOverrideSources = readBit()
                    val lastPreAdditiveVelocity = propertyVector10()
                    val bIsAdditiveVelocityApplied = readBit()
                    val flags = readUInt8()
                }
				35   -> propertyBool() //bIsActive
				36   -> propertyBool() //bRelativePosition
				37   -> propertyBool() //bRelativeRotation
				38   -> propertyVector10() //LinearVelocity
				
				39   -> propertyVector100() //Location
				40   -> propertyObject() //MovementBase
				41   -> propertyName() //MovementBaseBoneName
				42   -> propertyFloat() //Position
				43   -> readRotationShort() //Rotation
				//AMutableCharacter
                44 -> {	//InstanceDescriptor
                    val arrayNum = readUInt16()
                    var index = readIntPacked()
                    while (index != 0) {
                        val value = readUInt8()
                        index = readIntPacked()
                    }
                }
				//ATslCharacter
				45 -> {	//AimOffsets
                    val AimOffsets = propertyVectorNormal()
                    val b = AimOffsets
                }
				46   -> propertyBool() //bAimStateActive
				47   -> propertyBool() //bIsActiveRagdollActive
				48 -> {
                    val bIsAimingRemote = propertyBool()
                }
				49   -> propertyBool() //bIsCoatEquipped
				50   -> propertyBool() //bIsDemoVaulting_CP
				51   -> propertyBool() //bIsFirstPersonRemote
				52 -> {
                    val bIsDowned=propertyBool()
                    actorDowned[actor.netGUID] = bIsDowned
					//println("83: ${actor.netGUID} $bIsDowned")
                }
				53   -> propertyBool() //bIsHoldingBreath
				54   -> propertyBool() //bIsInVehicleRemote
				55   -> propertyBool() //bIsPeekLeft
				56 -> {
				val (id, team) = propertyObject()
                    //val ActualDamage = propertyFloat()
                }
				57 -> {
                    val bIsReviving = propertyBool()
                    actorBeingRevived[actor.netGUID] = bIsReviving
					//println("84: ${actor.netGUID} $bIsReviving")
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
				59   -> propertyBool() //bIsThirdPerson
				60   -> propertyBool() //bIsThrowHigh
				61   -> propertyBool() //bIsWeaponObstructed
				62   -> propertyBool() //bIsZombie
				63 -> {
                    val BoostGauge = propertyFloat()
                }
				64 -> {
                    val BoostGaugeMax = propertyFloat()
                }
				65   -> propertyBool() //bServerFinishedVault
				66   -> propertyFloat() //BuffFinalSpreadFactor
				67   -> propertyBool() //bUseRightShoulderAiming
				68   -> propertyBool() //bWantsToCancelVault
				69   -> propertyBool() //bWantsToRollingLeft
				70   -> propertyBool() //bWantsToRollingRight
				71   -> propertyBool() //bWantsToRun
				72   -> propertyBool() //bWantsToSprint
				73   -> propertyBool() //bWantsToSprintingAuto
				74 -> {
                    val CharacterState = propertyByte()
                }
				75   -> propertyByte() //CurrentWeaponZoomLevel
				76 -> {
                    val GroggyHealth = propertyFloat()
                }
				77 -> {
                    val GroggyHealthMax = propertyFloat()
                }
				78   -> readRotationShort() //GunDirectionSway
                79 -> {
                    val health = propertyFloat()
                    actorHealth[actor.netGUID] = health
                }
                80 -> {
                    val healthMax = propertyFloat()
                }
				81   -> propertyBool() //IgnoreRotation
				82   -> propertyObject() //InventoryFacade
				83   -> propertyFloat() //struct FTakeHitInfo | ActualDamage
                84 -> {
                    val AttackerLocation = propertyVector()
                }
				85 -> {
                    val AttackerWeaponName = propertyName()
                }
				86 -> {
                    val bKilled = propertyBool()
                }
				87   -> propertyName() //BoneName
				88   -> propertyBool() //bPointDamage
				89   -> propertyBool() //bRadialDamage
				90 -> {
                    val DamageMaxRadius = propertyFloat()
                }
				91 -> {
                    val DamageOrigin = propertyVectorQ()
                }
				92 -> {
                    val damageType = propertyObject()
                }
				93 -> {
                    val EnsureReplicationByte = propertyByte()
                }
                94 -> {
                    val PlayerInstigator = propertyObject()
                }
				95 -> {
                    val RelHitLocation = propertyVectorQ()
                }
				96   -> propertyByte() //ShotDirPitch
				97 -> {
                    val ShotDirYaw = propertyByte()
                }
				98   -> readObject() //NetOwnerController
				99   -> readInt(4) //PreReplicatedStanceMode
				100 -> {
                    val remote_CastAnim = readInt(8)
                }
				101 -> {
                    val reviveCastingTime = propertyFloat()
                    val a = reviveCastingTime
                }
				102 -> {
                    val ShoesSoundType = readInt(8)
                    val b = ShoesSoundType
                }
				103 -> {
                    //SpectatedCount
                    val spectated = propertyInt()
					spectatedCount = spectated
					//println("Other: ${actor.netGUID} $spectated")
                    if (actor.netGUID == selfID) {
                        PlayerStateCMD.selfSpectated = spectated
						//println("SPECS: ${PlayerStateCMD.selfSpectated}")
                    }
                }
				104 -> {
                    val TargetingType = readInt(4)
                    val a = TargetingType
                }
				105 -> {
				val (id, team) = propertyObject()
                   
                }
				106  -> readObject() //VehicleRiderComponent
                107  -> propertyObject() //WeaponProcessor
                else -> return false
            }
            return true
        }
    }
}

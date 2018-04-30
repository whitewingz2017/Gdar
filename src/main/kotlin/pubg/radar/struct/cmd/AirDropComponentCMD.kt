package pubg.radar.struct.cmd

import pubg.radar.deserializer.channel.ActorChannel.Companion.airDropLocation
import pubg.radar.struct.*
import pubg.radar.struct.cmd.*

object AirDropComponentCMD {
    fun process(actor:Actor,bunch:Bunch,repObj:NetGuidCacheObject?,waitingHandle:Int,data:HashMap<String,Any?>):Boolean {
        with(bunch) {
            when (waitingHandle) {
                14 -> {
                    repMovement(actor)
                    airDropLocation[actor.netGUID]=actor.location
                }
                17 -> updateItemBag(actor)
                else -> return ActorCMD.process(actor,bunch,repObj,waitingHandle,data)
            }
            return true
        }
    }
}
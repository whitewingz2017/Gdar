package pubg.radar.struct.cmd

import pubg.radar.deserializer.channel.ActorChannel.Companion.droppedItemCompToItem
import pubg.radar.deserializer.channel.ActorChannel.Companion.droppedItemGroup
import pubg.radar.deserializer.channel.ActorChannel.Companion.droppedItemLocation
import pubg.radar.deserializer.channel.ActorChannel.Companion.droppedItemToItem
import pubg.radar.deserializer.channel.ActorChannel.Companion.itemBag
import pubg.radar.struct.*
import pubg.radar.util.*

object DroppedItemGroupRootComponentCMD {
  fun process(actor: Actor, bunch: Bunch, repObj: NetGuidCacheObject?, waitingHandle: Int, data: HashMap<String, Any?>): Boolean {
    with(bunch) {
      when (waitingHandle) {
        4 -> {
          val arraySize = readUInt16()
          val comps = droppedItemGroup[actor.netGUID] ?: ArrayList(arraySize)
          val new = comps.isEmpty()
          var index = readIntPacked()
          val toRemove = HashSet<NetworkGUID>()
          val toAdd = HashSet<NetworkGUID>()
          while (index != 0) {
            val i = index - 1
            val (netguid, obj) = readObject()
            if (new)
              comps.add(netguid)
            else {
              //remove index
              toRemove.add(comps[i])
              comps[i] = netguid
              toAdd.add(netguid)
            }
//            println("$netguid,$obj")
            index = readIntPacked()
          }
          for (i in comps.lastIndex downTo arraySize)
            toRemove.add(comps.removeAt(i))
          toRemove.removeAll(toAdd)
          droppedItemGroup[actor.netGUID] = comps
          for (removedComp in toRemove)
            droppedItemLocation.remove(droppedItemCompToItem[removedComp] ?: continue)
        }
        else -> return false
      }
    }
    return true
  }
}

fun Bunch.updateItemBag(actor:Actor) {
  val arraySize=readUInt16()
  val oldSize:Int
  val items:DynamicArray<NetworkGUID?>
  val oldItems= itemBag[actor.netGUID]
  if (oldItems == null) {
    oldSize=0
    items=DynamicArray(arraySize)
  } else {
    oldSize=oldItems.size
    items=oldItems.resize(arraySize)
  }
  var index=readIntPacked()
  val toRemove=HashSet<NetworkGUID>()
  val toAdd=HashSet<NetworkGUID>()
  while (index != 0) {
    val i=index-1
    val (netguid,obj)=readObject()
    items[i]?.apply {
      toRemove.add(this)
      toAdd.add(netguid)
    }
    items[i]=netguid
    index=readIntPacked()
  }
  for (i in oldSize-1 downTo arraySize)
    items.rawGet(i)?.apply {toRemove.add(this)}
  toRemove.removeAll(toAdd)
  itemBag[actor.netGUID]=items
  for (removedComp in toRemove)
    droppedItemLocation.remove(droppedItemCompToItem[removedComp] ?: continue)
}
@file:Suppress("NAME_SHADOWING")
package pubg.radar.struct

class Item {
    companion object {
        // Group
        // Inner, Outer ?
        private val category = mapOf(
                "Attach" to mapOf(
                        "Weapon" to mapOf(

                                "Lower" to mapOf(
                                        "AngledForeGrip" to "A.Grip",
                                        "Foregrip" to "V.Grip"
                                ),
                                "Magazine" to mapOf(
                                        "Extended" to mapOf(
                                                "Medium" to "U.Ext",
                                                "Large" to "AR.Ext",
                                                "SniperRifle" to "S.Ext"
                                        ),
                                        "ExtendedQuickDraw" to mapOf(
                                                "Medium" to "U.ExtQ",
                                                "Large" to "AR.ExtQ",
                                                "SniperRifle" to "S.ExtQ"
                                        )
                                ),
                                "Muzzle" to mapOf(
                                        "Choke" to "Choke",
                                        "Compensator" to mapOf(
                                                "Large" to "AR.Comp",
                                                "SniperRifle" to "S.Comp"
                                        ),
                                        "FlashHider" to mapOf(
                                                "Large" to "FH",
                                                "SniperRifle" to "FH"
                                        ),
                                        "Suppressor" to mapOf(
                                                "Medium" to "U.Supp",
                                                "Large" to "AR.Supp",
                                                "SniperRifle" to "S.Supp"
                                        )
                                ),
                                "Stock" to mapOf(
                                        "AR" to "AR.Stock",
                                        "SniperRifle" to mapOf(
                                                "BulletLoops" to "S.Loops",
                                                "CheekPad" to "CheekPad"
                                        )
                                ),
                                "Upper" to mapOf(
                                        "DotSight" to "DotSight",
                                        "Aimpoint" to "Aimpoint",
                                        "Holosight" to "Holosight",
                                        "ACOG" to "ACOG",
                                        "CQBSS" to "CQBSS"
                                )
                        )
                ),

                "Ghillie" to mapOf(
                        "02" to "GhillieBrown",
                        "01" to "GhillieGreen"
                ),

                "Weapon" to mapOf(
                        "Grenade" to "Grenade",
                        "SmokeBomb" to "SmokeBomb",
                        "FlashBang" to "FlashBang",
                        "Molotov" to "Molotov",
                        "M16A4" to "M16A4",
                        "HK416" to "HK416",
                        "Kar98k" to "Kar98k",
                        "SCAR-L" to "SCAR-L",
                        "AK47" to "AK47",
                        "SKS" to "SKS",
                        "Mini14" to "Mini14",
                        "DP28" to "DP28",
                        "UMP" to "UMP",
                        "Vector" to "Vector",
                        "Pan" to "Pan",
                        "SawnOff" to "SawnOff",
                        "UZI" to "UZI",
                        "G1B" to "AUG",
                        "AUG" to "AUG",
                        "Groza" to "Groza",
                        "AWM" to "AWM",
                        "M24" to "M24",
                        "M249" to "M249",
                        "Mk14" to "Mk14"),

                "Boost" to mapOf(
                        "EnergyDrink" to "Drink",
                        "AdrenalineSyringe" to "AdrenalineSyringe",
                        "PainKiller" to "Pain"
                ),
                "Heal" to mapOf(
                        "FirstAid" to "FirstAid",
                        "MedKit" to "MedKit"
                ),
                "Ammo" to mapOf(
                        "9mm" to "9mm",
                        "45mm" to "45mm",
                        "556mm" to "556mm",
                        "762mm" to "762mm",
                        "300mm" to "300mm"
                ),

                "Armor" to mapOf(
                        "C" to "Armor3",
                        "D" to "Armor2",
                        "E" to "Armor1"),
                "Back" to mapOf(
                        "B" to "ParachutePack",
                        "C" to mapOf(
                                "01" to "Bag3",
                                "02" to "Bag3"),
                        "F" to mapOf(
                                "01" to "Bag2",
                                "02" to "Bag2"),
                        "E" to mapOf(
                                "01" to "Bag1",
                                "02" to "Bag1")),
                "Head" to mapOf(
                        "G" to "Helmet3",
                        "F" to mapOf(
                                "01" to "Helmet2",
                                "02" to "Helmet2"),
                        "E" to mapOf(
                                "01" to "Helmet1",
                                "02" to "Helmet1"))
        ) as Map<String, Any>

        /**
         * @return null if not good, or short name for it
         */
        fun isGood(description: String): String? {
            try {
                val start = description.indexOf("Item_")
                if (start == -1) return null//not item
                val words = description.substring(start + 5).split("_")
                var c = category
                for (word in words) {
                    if (word !in c) return null
                    val sub: Any? = c[word]
                    if (sub is String) return sub
                    c = sub as Map<String, Any>
                }
            } catch (e: Exception) {
            }
            return null
        }

        fun simplify(description:String):String {
            try {
                val words = description.split("_")
                var c = category
                for (word in words) {
                    if (word !in c)
                        return description
                    val sub:Any? = c[word]
                    if (sub is String)
                        return sub
                    c = sub as Map<String, Any>
                }
            } catch (e:Exception) {
            }
            return description
        }

    }
}

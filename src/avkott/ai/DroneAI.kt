package avkott.ai

import arc.math.geom.Vec2
import arc.util.Tmp
import mindustry.entities.units.AIController
import mindustry.gen.Call
import mindustry.gen.PosTeam
import mindustry.gen.Unit

open class DroneAI(
    val owner: Unit,
) : AIController() {
    var rallyPos = Vec2()
    val posTeam = PosTeam.create()
    override fun updateUnit() {
        if(!owner.isValid) {
            Call.unitDespawn(unit)
            return
        }
        super.updateUnit()
    }
    override fun updateMovement() {
        rally()
    }
    fun rally(pos: Vec2) {
        rallyPos = pos
    }
    fun rally() {
        Tmp.v2.set(owner.x, owner.y)
        moveTo(Tmp.v1.set(rallyPos).add(Tmp.v2).rotateAround(Tmp.v2, owner.rotation - 90), 2f, 0.6f)
    }
}
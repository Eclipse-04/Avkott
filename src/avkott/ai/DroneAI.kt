package avkott.ai

import arc.math.geom.Vec2
import arc.util.Tmp
import mindustry.entities.units.AIController
import mindustry.gen.Call
import mindustry.gen.PosTeam
import mindustry.gen.Teamc
import mindustry.gen.Unit

class DroneAI(
    val owner: Unit,
    val rallyPos: Vec2
) : AIController() {
    val posTeam = PosTeam.create()

    override fun updateMovement() {
        unloadPayloads()

        val owner = if(owner.isValid) owner else {
            Call.unitDespawn(unit)
            return
        }

        if(owner.isShooting){

            if (unit.hasWeapons()) {
                posTeam.set(owner.aimX, owner.aimY)
                updateWeapons()

                if (unit.type.circleTarget) {
                    circleAttack(120f)
                } else {
                    moveTo(posTeam, unit.type.range * 0.75f)
                    unit.lookAt(posTeam)
                }
            }

        } else {
            //return to owner
            Tmp.v2.set(owner.x, owner.y)
            moveTo(Tmp.v1.set(rallyPos).add(Tmp.v2).rotateAround(Tmp.v2, owner.rotation - 90), 2f, 0.6f)
        }
    }

    override fun target(x: Float, y: Float, range: Float, air: Boolean, ground: Boolean): Teamc? {
        return if(!owner.isValid && !owner.isShooting) null else posTeam
    }
}
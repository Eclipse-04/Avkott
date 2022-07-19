package avkott.ai

import mindustry.gen.Teamc
import mindustry.gen.Unit

class AttackDroneAI(owner: Unit) : DroneAI(owner) {

    override fun updateMovement() {
        super.updateMovement()

        if(owner.isShooting){
            if (unit.hasWeapons()) {
                posTeam.set(owner.aimX, owner.aimY)

                if (unit.type.circleTarget) {
                    circleAttack(120f)
                } else {
                    moveTo(posTeam, unit.type.range * 0.75f)
                    unit.lookAt(posTeam)
                }
            }
        } else rally()
    }

    override fun target(x: Float, y: Float, range: Float, air: Boolean, ground: Boolean): Teamc? {
        return if(!owner.isValid && !owner.isShooting) null else posTeam
    }
}
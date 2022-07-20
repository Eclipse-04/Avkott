package avkott.abilities

import arc.Events
import arc.graphics.g2d.Draw
import arc.math.Angles
import arc.math.geom.Vec2
import arc.util.Time
import avkott.ai.DroneAI
import mindustry.Vars
import mindustry.Vars.state
import mindustry.content.Fx
import mindustry.content.UnitTypes
import mindustry.entities.abilities.Ability
import mindustry.game.EventType.UnitCreateEvent
import mindustry.gen.Unit
import mindustry.graphics.Drawf
import mindustry.graphics.Layer

open class UnitCannonAbility : Ability() {
    var unitSpawn = UnitTypes.avert
    var constructTime = 60f
    var spawnX = 0f
    var spawnY = 0f
    var spawnEffect = Fx.spawn
    var parentizeEffects = false
    var rallyPos = arrayOf(Vec2(5 * 8f, -5 * 8f))
    var layer = Layer.flyingUnitLow - 0.01f
    var rotation = 0f
    var autoRelease = false
    var droneCount = 2
    protected var timer = 0f
    protected var units = ArrayList<Unit>()
    override fun copy(): Ability = super.copy().apply {
        units = ArrayList()
    }
    //todo find a better method?
    var ai = fun(owner: Unit): DroneAI = DroneAI(owner)

    override fun update(unit: Unit) {
        units.retainAll { it.isValid } //filter out dead units
        if (units.size < droneCount) {
            if (timer > constructTime) {
                if(autoRelease || unit.isShooting) {
                    val x = unit.x + Angles.trnsx(unit.rotation, spawnY, spawnX)
                    val y = unit.y + Angles.trnsy(unit.rotation, spawnY, spawnX)

                    spawnEffect.at(x, y, 0f, if (parentizeEffects) unit else null)
                    val unitSpawned = unitSpawn.create(unit.team)
                    unitSpawned.set(x, y)
                    unitSpawned.rotation = unit.rotation + rotation
                    units.add(0, unitSpawned)
                    unitSpawned.controller(ai(unit))
                    updateRally()

                    Events.fire(UnitCreateEvent(unitSpawned, null, unit))
                    if (!Vars.net.client()) unitSpawned.add()

                    timer %= constructTime
                }
            } else timer += Time.delta * state.rules.unitBuildSpeed(unit.team)
        }
    }

    fun updateRally() {
        for (u in units) (u.controller() as DroneAI).rally(rallyPos[units.indexOf(u)])
    }

    override fun draw(unit: Unit) {
        if (units.size < droneCount) Draw.draw(layer) {
            val x = unit.x + Angles.trnsx(unit.rotation, spawnY, spawnX)
            val y = unit.y + Angles.trnsy(unit.rotation, spawnY, spawnX)

            if (timer <= constructTime) Drawf.construct(
                x, y, unitSpawn.fullIcon,
                unit.rotation - 90 + rotation,
                timer / constructTime,
                1f, timer
            )
            else Draw.rect(unitSpawn.fullIcon, x, y, unit.rotation - 90 + rotation)
        }
    }
}
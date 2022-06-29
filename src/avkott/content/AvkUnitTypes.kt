package avkott.content

import arc.func.Func
import arc.func.Prov
import arc.graphics.Color
import mindustry.ai.types.RepairAI
import mindustry.content.Fx
import mindustry.content.UnitTypes
import mindustry.entities.abilities.ArmorPlateAbility
import mindustry.entities.abilities.MoveEffectAbility
import mindustry.entities.bullet.BasicBulletType
import mindustry.entities.effect.MultiEffect
import mindustry.entities.effect.WaveEffect
import mindustry.entities.part.HoverPart
import mindustry.entities.pattern.ShootAlternate
import mindustry.entities.pattern.ShootSpread
import mindustry.entities.units.UnitController
import mindustry.gen.Unit
import mindustry.gen.UnitEntity
import mindustry.graphics.Pal
import mindustry.type.UnitType
import mindustry.type.UnitType.UnitEngine
import mindustry.type.Weapon
import mindustry.type.unit.ErekirUnitType

object AvkUnitTypes {
    lateinit var elud: UnitType
    lateinit var aver: UnitType

    fun load(){
        elud = ErekirUnitType("elud").apply {
            constructor = UnitTypes.elude.constructor

            hovering = true
            shadowElevation = 0.2f
            drag = 0.05f
            speed = 2.8f
            rotateSpeed = 3f
            accel = 0.05f
            health = 2200f
            armor = 9f
            hitSize = 16.5f
            engineOffset = 6.25f
            engineColor = Pal.techBlue
            engineSize = 4f
            itemCapacity = 0
            useEngineElevation = false

            abilities.add(
                MoveEffectAbility(0f, -6.75f, Pal.techBlue, Fx.missileTrail, 3.5f),
                ArmorPlateAbility().apply { healthMultiplier = 0.4f }
            )

            for (f in arrayOf(4.75f, -3.75f)) {
                parts.addAll(
                    HoverPart().apply {
                        x = 6.25f
                        y = f
                        mirror = true
                        radius = 8f
                        phase = 90f
                        stroke = 3f
                        layerOffset = -0.001f
                        color = Pal.techBlue
                    }
                )
            }

            weapons.add(Weapon("elud-weapon").apply {
                x = 3f
                y = -4.25f
                mirror = true
                reload = 40f
                baseRotation = -35f
                shootCone = 360f
                shoot = ShootSpread(3, 9f).apply { shotDelay = 2.5f }

                bullet = object : BasicBulletType(5.4f, 35f) {
                    init {
                        homingPower = 0.19f
                        homingDelay = 4f
                        width = 9f
                        height = 14f
                        lifetime = 40f
                        shootEffect = Fx.sparkShoot
                        smokeEffect = Fx.shootBigSmoke
                        trailColor = Pal.techBlue
                        backColor = trailColor
                        hitColor = backColor
                        frontColor = Color.white
                        trailWidth = 1.7f
                        trailLength = 5
                        splashDamageRadius = 23f
                        splashDamage = 40f

                        despawnEffect = MultiEffect(Fx.hitSquaresColor, WaveEffect().apply {
                            colorFrom = Pal.techBlue.also { colorTo = it }
                            sizeTo = splashDamageRadius + 3f
                            lifetime = 9f
                            strokeFrom = 3f
                        })
                        hitEffect = despawnEffect
                        shootEffect = Fx.shootBigColor
                    }
                }
            })
        }

        aver = ErekirUnitType("aver").apply {
            constructor = Prov { UnitEntity.create() }
            controller = Func<Unit, UnitController> { RepairAI() }
            lowAltitude = false
            flying = true
            drag = 0.06f
            speed = 2.5f
            rotateSpeed = 7f
            accel = 0.07f
            health = 1300f
            armor = 4.5f
            hitSize = 17f
            fogRadius = 35f
            itemCapacity = 20
            engineOffset = 8f
            mineTier = 3
            mineSpeed = 4.5f
            mineWalls = true
            setEnginesMirror(UnitEngine(4.5f, -5f, 4f, 315f))

            weapons.add(Weapon("aver-weapon").apply {
                x = 3.5f
                y = 5f
                mirror = true
                reload = 12f
                shootCone = 30f
                shoot = ShootAlternate(3.5f)

                bullet = BasicBulletType(1f, 20f).apply {
                    keepVelocity = false
                    collidesTeam = true
                    healPercent = 3f
                    homingPower = 0.19f
                    homingDelay = 4f
                    drag = -0.06f
                    width = 9f
                    height = 14f
                    lifetime = 50f
                    shootEffect = Fx.sparkShoot
                    smokeEffect = Fx.shootBigSmoke
                    trailColor = Pal.heal
                    backColor = trailColor
                    hitColor = backColor
                    frontColor = Color.white
                    trailWidth = 1.7f
                    trailLength = 5
                    despawnEffect = Fx.heal
                    hitEffect = despawnEffect
                    shootEffect = Fx.shootBigColor
                }
            })
        }
    }
}
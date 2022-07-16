package avkott.content

import arc.func.Func
import arc.func.Prov
import arc.graphics.Color
import mindustry.Vars.tilePayload
import mindustry.ai.types.BuilderAI
import mindustry.ai.types.RepairAI
import mindustry.content.Fx
import mindustry.content.UnitTypes
import mindustry.entities.abilities.ArmorPlateAbility
import mindustry.entities.abilities.MoveEffectAbility
import mindustry.entities.bullet.BasicBulletType
import mindustry.entities.bullet.ContinuousFlameBulletType
import mindustry.entities.effect.MultiEffect
import mindustry.entities.effect.WaveEffect
import mindustry.entities.part.DrawPart.PartMove
import mindustry.entities.part.DrawPart.PartProgress
import mindustry.entities.part.HoverPart
import mindustry.entities.part.RegionPart
import mindustry.entities.pattern.ShootAlternate
import mindustry.entities.pattern.ShootSpread
import mindustry.entities.units.UnitController
import mindustry.gen.Sounds
import mindustry.gen.Unit
import mindustry.gen.UnitEntity
import mindustry.graphics.Pal
import mindustry.type.UnitType
import mindustry.type.UnitType.UnitEngine
import mindustry.type.Weapon
import mindustry.type.unit.ErekirUnitType
import mindustry.type.weapons.BuildWeapon

object AvkUnitTypes {
    lateinit var elud: UnitType
    lateinit var aver: UnitType
    lateinit var obvi: UnitType
    lateinit var rail: UnitType

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
                        buildingDamageMultiplier = 0.2f
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
                    healAmount = 50f
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
            obvi = ErekirUnitType("obvi").apply {
                constructor = Prov { UnitEntity.create() }
                flying = true
                drag = 0.05f
                speed = 1.95f
                rotateSpeed = 2.1f
                accel = 0.07f
                health = 2500f
                armor = 5f
                hitSize = 27.5f
                engineSize = 0f
                lowAltitude = true

                setEnginesMirror(
                    UnitEngine(21/4f, -50/4f, 4f, 270f),
                    UnitEngine(55/4f, -40/4f, 3.5f, 315f)
                )

                parts.addAll(
                    RegionPart("-blade").apply {
                        moves.add(PartMove(PartProgress.warmup, 1f, -1f, 0f))
                        mirror = true
                        heatColor = Color.valueOf("b332c2")
                    }
                )

                weapons.add(Weapon("hauptwaffe").apply {
                    x = 0f
                    y = -1f
                    shootY = 0f
                    alwaysContinuous = true
                    range = 160f
                    reload = 20f
                    mirror = false
                    shootSound = Sounds.minebeam

                    bullet = ContinuousFlameBulletType().apply {
                        damage = 60f
                        length = 160f
                        knockback = 1.3f
                        pierceCap = 2
                        colors = arrayOf(
                            Color.valueOf("eb7abe").a(0.55f),
                            Color.valueOf("e189f5").a(0.7f),
                            Color.valueOf("907ef7").a(0.8f),
                            Color.valueOf("91a4ff"),
                            Color.white
                        )
                    }
                })
            }
            rail = ErekirUnitType("rail").apply {
                constructor = Prov { UnitEntity.create() }
                controller = Func<Unit, UnitController> { BuilderAI() }
                payloadCapacity = 4 * tilePayload
                flying = true
                drag = 0.05f
                speed = 1.95f
                rotateSpeed = 2.1f
                accel = 0.07f
                health = 2500f
                armor = 5f
                hitSize = 27.5f
                engineSize = 0f
                lowAltitude = true
                drawBuildBeam = false
                rotateToBuilding = false
                buildSpeed = 2.5f
                buildRange = 320f

                setEnginesMirror(
                    UnitEngine(18/4f, -59/4f, 4f, 270f),
                    UnitEngine(56/4f, -36/4f, 3.5f, 315f)
                )

                weapons.addAll(
                    BuildWeapon("avkott-rail-gun").apply { x = 32/4f; y = 15/4f },
                    Weapon("hauptwaffe").apply {
                        x = 0f
                        y = 24/4f
                        reload = 120f
                        range = 70 * 7f

                        bullet = BasicBulletType(8f, 120f).apply {
                            keepVelocity = false
                            recoil = 4f
                            lifetime = 70f
                            width = 10f
                            height = 16f
                            hitSize = 6f
                            pierceCap = 4
                            pierce = true
                            pierceBuilding = true
                            hitColor = Pal.berylShot.also { trailColor = it }.also { backColor = it }
                            frontColor = Color.white
                            trailWidth = 2.8f
                            trailLength = 12
                            hitEffect = Fx.hitBulletColor.also { despawnEffect = it }
                            smokeEffect = Fx.shootSmokeTitan
                            shootEffect = MultiEffect(Fx.shootBigColor, Fx.colorSparkBig)
                            trailEffect = Fx.hitSquaresColor
                            trailInterval = 3f
                            trailRotation = true
                        }
                    }
                )
            }
        }
    }
}
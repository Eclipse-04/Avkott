package avkott.content

import arc.func.Func
import arc.func.Prov
import arc.graphics.Color
import arc.math.Mathf
import arc.math.geom.Vec2
import avkott.abilities.UnitCannonAbility
import mindustry.Vars.tilePayload
import mindustry.ai.types.BuilderAI
import mindustry.ai.types.RepairAI
import mindustry.content.Fx
import mindustry.content.StatusEffects
import mindustry.entities.abilities.ArmorPlateAbility
import mindustry.entities.abilities.MoveEffectAbility
import mindustry.entities.bullet.BasicBulletType
import mindustry.entities.bullet.ContinuousFlameBulletType
import mindustry.entities.bullet.LaserBoltBulletType
import mindustry.entities.bullet.PointLaserBulletType
import mindustry.entities.effect.MultiEffect
import mindustry.entities.effect.WaveEffect
import mindustry.entities.part.DrawPart.PartMove
import mindustry.entities.part.DrawPart.PartProgress
import mindustry.entities.part.HoverPart
import mindustry.entities.part.RegionPart
import mindustry.entities.pattern.ShootAlternate
import mindustry.entities.pattern.ShootSpread
import mindustry.entities.units.UnitController
import mindustry.gen.*
import mindustry.gen.Unit
import mindustry.graphics.Pal
import mindustry.type.UnitType
import mindustry.type.UnitType.UnitEngine
import mindustry.type.Weapon
import mindustry.type.unit.ErekirUnitType
import mindustry.type.weapons.BuildWeapon
import kotlin.math.sqrt

object AvkUnitTypes {
    lateinit var elud: UnitType
    lateinit var aver: UnitType
    lateinit var obvi: UnitType
    lateinit var railDrone: UnitType
    lateinit var rail: UnitType
    lateinit var quelDrone: UnitType
    lateinit var quel: UnitType

    fun load(){
        elud = ErekirUnitType("elud").apply {
            constructor = Prov { ElevationMoveUnit.create() }

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
            constructor = Prov { PayloadUnit.create() }
            controller = Func<Unit, UnitController> { RepairAI() }
            payloadCapacity = sqrt(2f) * tilePayload
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

                parts.add(
                    RegionPart("-blade").apply {
                        moves.add(PartMove(PartProgress.warmup, 1f, -1f, 8f))
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
            railDrone = ErekirUnitType("rail-support").apply {
                constructor = Prov { UnitEntity.create() }
                flying = true
                drag = 0.05f
                speed = 2.85f
                rotateSpeed = 9f
                accel = 0.1f
                health = 600f
                armor = 5f
                hitSize = 28 / 4f
                engineSize = 3f
                engineOffset = 15 / 4f
                trailLength = 9
                playerControllable = false
                logicControllable = false
                allowedInPayloads = false
                useUnitCap = false

                weapons.add(Weapon().apply {
                    x = 0f
                    y = 1.25f
                    reload = 20f
                    mirror = false
                    ejectEffect = Fx.none
                    recoil = 1f
                    shootSound = Sounds.lasershoot

                    bullet = LaserBoltBulletType(5.2f, 30f).apply {
                        lifetime = 30f
                        healAmount = 20f
                        collidesTeam = true
                        backColor = Pal.heal
                        frontColor = Color.white
                        statusDuration = 4f * 60
                        status = StatusEffects.electrified
                    }
                })
            }
            rail = ErekirUnitType("rail").apply {
                constructor = Prov { PayloadUnit.create() }
                controller = Func<Unit, UnitController> { BuilderAI() }
                payloadCapacity = sqrt(3f) * tilePayload
                flying = true
                drag = 0.06f
                speed = 2.1f
                rotateSpeed = 2.1f
                accel = 0.08f
                health = 2650f
                armor = 7f
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

                for(i in Mathf.signs) {
                    abilities.add(
                        UnitCannonAbility().apply {
                            rallyPos = arrayOf(Vec2(20f * i, 15f), Vec2(20f * i, 20f))
                            spawnX = 48 / 4f * i
                            spawnY = 7 / -4f
                            unitSpawn = railDrone
                            constructTime = 60 * 5f
                        }
                    )
                }

                weapons.addAll(
                    Weapon("avkott-rail-barrel").apply {
                        x = 0f
                        shootY = 24/4f
                        reload = 120f
                        range = 70 * 7f
                        recoil = 3.5f

                        mirror = false
                        top = false

                        bullet = BasicBulletType(8f, 120f).apply {
                            keepVelocity = false
                            recoil = 2.6f
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
                            healAmount = 200f
                            collidesTeam = true
                        }
                    },
                    BuildWeapon("avkott-rail-gun").apply { x = 32/4f; y = 15/4f }
                )
            }
            quelDrone = ErekirUnitType("beam-drone").apply {
                constructor = Prov { UnitEntity.create() }
                flying = true
                drag = 0.03f
                speed = 2.5f
                rotateSpeed = 5.5f
                accel = 0.1f
                health = 600f
                armor = 5f
                hitSize = 44 / 4f
                engineSize = 4f
                engineOffset = 18/4f
                trailLength = 9
                playerControllable = false
                logicControllable = false
                allowedInPayloads = false
                useUnitCap = false

                weapons.add(Weapon().apply {
                    x = 0f
                    y = 2.25f
                    mirror = false
                    ejectEffect = Fx.none
                    shootSound = Sounds.lasershoot
                    alwaysContinuous = true

                    bullet = PointLaserBulletType().apply {
                        maxRange = 100f
                        damage = 60f
                        healAmount = 5f
                        collidesTeam = true

                        statusDuration = 4f * 60
                        status = StatusEffects.electrified
                    }
                })
            }

            //todo WIP
            quel = ErekirUnitType("quel").apply {
                constructor = Prov { UnitEntity.create() }
                payloadCapacity = 6 * tilePayload
                flying = true
                drag = 0.06f
                speed = 1.2f
                rotateSpeed = 2.1f
                accel = 0.1f
                health = 9550f
                armor = 13f
                hitSize = 36f
                engineSize = 18 / 4f
                engineOffset = 69 / 4f //nice
                lowAltitude = true
                drawBuildBeam = false
                rotateToBuilding = false
                buildSpeed = 4f
                buildRange = 320f

                setEnginesMirror(
                    UnitEngine(65/4f, -63/4f, 4f, 315f),
                    UnitEngine(27/4f, -80/4f, 4f, 270f)
                )

                for(i in Mathf.signs) {
                    abilities.add(
                        UnitCannonAbility().apply {
                            rotation = -45f * i
                            spawnX = 59 / 4f * i
                            spawnY = 13 / -4f
                            unitSpawn = quelDrone
                            constructTime = 60 * 30f
                            autoRelease = true
                        }
                    )
                }

                weapons.addAll(
                    BuildWeapon("quel-gun1").apply { x = 39/4f; y = 69/4f },
                )
            }
        }
    }
}
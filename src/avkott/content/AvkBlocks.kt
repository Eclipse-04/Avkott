package avkott.content

import arc.graphics.Color
import arc.math.Interp
import mindustry.content.Fx
import mindustry.content.Items
import mindustry.entities.Effect
import mindustry.entities.bullet.ShrapnelBulletType
import mindustry.entities.effect.MultiEffect
import mindustry.entities.part.DrawPart.PartMove
import mindustry.entities.part.DrawPart.PartProgress
import mindustry.entities.part.RegionPart
import mindustry.entities.pattern.ShootPattern
import mindustry.graphics.Pal
import mindustry.type.Category
import mindustry.type.ItemStack
import mindustry.world.Block
import mindustry.world.blocks.defense.turrets.ItemTurret
import mindustry.world.blocks.production.WallCrafter
import mindustry.world.draw.DrawTurret
import mindustry.world.meta.Attribute

object AvkBlocks {
    lateinit var cliffPulverizer: Block
    lateinit var ingen: Block

    fun load(){
        //production
        cliffPulverizer = WallCrafter("cliff-pulverizer").apply{
            requirements(Category.production, ItemStack.with(Items.beryllium, 90, Items.silicon, 50, Items.tungsten, 75))
            consumePower(1.2f)
            drillTime = 69.66f
            size = 3
            attribute = Attribute.sand
            output = Items.sand
            fogRadius = 4
            itemCapacity = 30
        }
        //endregion
        //turret
        ingen = ItemTurret("ingen").apply{
            requirements(Category.turret, ItemStack.with(Items.beryllium, 80, Items.graphite, 45, Items.silicon, 35))

            ammo(Items.graphite, ShrapnelBulletType().apply {
                lifetime = 15f
                length = 105f
                damage = 55f
                shootEffect = Fx.shootBigColor
                smokeEffect = Fx.shootBigSmoke
                ammoMultiplier = 3f
                fromColor = Color.valueOf("feb380")
                hitColor = Color.valueOf("ea8878")
                toColor = hitColor
                hitEffect = Fx.hitBulletColor
                buildingDamageMultiplier = 0.2f
                hitLarge = true
            })
            size = 2
            drawer = DrawTurret("reinforced-").apply {
                parts.addAll(
                    RegionPart("-barrel").apply {
                        progress = PartProgress.reload.curve(Interp.pow2In)
                        moveY = -1f
                        heatColor = Color.valueOf("f03b0e")
                        mirror = false
                        under = true
                    },
                    RegionPart("-sus").apply {
                        heatProgress = PartProgress.smoothReload
                        progress = PartProgress.smoothReload
                        mirror = true
                        moveX = 1.5f
                        moveY = 0.5f
                        heatColor = Color.red.cpy()
                        moves.add(PartMove(PartProgress.smoothReload, 0f, -1.5f, -2f))
                        under = true
                    }
                )
            }
            recoil = 0.5f
            outlineColor = Pal.darkOutline
            shootY = 10f
            range = 110f
            inaccuracy = 5f
            ammoPerShot = 2
            reload = 20f
        }
        //endregion
        //crafter

    }
}
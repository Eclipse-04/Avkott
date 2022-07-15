package avkott.world.block.defend

import arc.graphics.Color
import arc.math.Mathf
import arc.util.Tmp
import mindustry.Vars.*
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.logic.Ranged
import mindustry.world.Block
import mindustry.world.meta.BlockGroup
import mindustry.world.meta.Env

//todo wip

open class BaseProjector(name: String) : Block(name) {
    var range = 100f
    var color = Color.white
    var reload = 180f

    init {
        solid = true
        update = true
        group = BlockGroup.projectors
        hasPower = true
        hasItems = true
        canOverdrive = false
        emitLight = true
        lightRadius = 50f
        envEnabled = envEnabled or Env.space
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, color)
        indexer.eachBlock(
            player.team(), x * tilesize + offset, y * tilesize + offset, range,
            { it.block.canOverdrive }
        ) { Drawf.selected(it, Tmp.c1.set(color).a(Mathf.absin(4f, 1f))) }
    }

    inner class BaseProjectorBuild : Building(), Ranged {
        var progress = 0f

        override fun range() = range
        override fun drawLight() = Drawf.light(x, y, lightRadius * warmup(), color, 0.7f * warmup())

        fun cast() {

        }

        override fun updateTile() {
            super.updateTile()
            if(progress >= 1) cast() else progress += getProgressIncrease(reload)
        }
    }
}
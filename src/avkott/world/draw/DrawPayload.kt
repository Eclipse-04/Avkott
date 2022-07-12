package avkott.world.draw

import arc.graphics.g2d.Draw
import arc.util.Eachable
import mindustry.entities.units.BuildPlan
import mindustry.gen.Building
import mindustry.world.Block
import mindustry.world.blocks.payloads.PayloadBlock
import mindustry.world.blocks.payloads.PayloadBlock.PayloadBlockBuild
import mindustry.world.draw.DrawBlock

class DrawPayload : DrawBlock() {
    var drawOut = true
    var drawIn = true
    var drawPayload = true
    var drawPlan = true
    var defaultIn = true
    var blend = true
    override fun draw(b: Building) {
        if (b !is PayloadBlockBuild<*>) return
        val bu = b.block as PayloadBlock

        if(blend){
            var fallback = true
            if (drawIn) for (i in 0..3) {
                if (b.blends(i) && (i != b.rotation || !drawOut)) {
                    Draw.rect(bu.inRegion, b.x, b.y, i * 90 - 180f)
                    fallback = false
                }
            }
            if (fallback && defaultIn && drawIn) Draw.rect(bu.inRegion, b.x, b.y, b.rotation * 90f)
        } else {
            Draw.rect(bu.inRegion, b.x, b.y, b.rotation * 90 - 180f)
        }
        if(drawOut) Draw.rect(bu.outRegion, b.x, b.y, b.rotdeg())
        if(drawPayload) b.drawPayload()
    }

    override fun drawPlan(block: Block, plan: BuildPlan, list: Eachable<BuildPlan>?) {
        if(!drawPlan) return
        val blockP = block as? PayloadBlock ?: return
        if(drawIn) Draw.rect(blockP.inRegion, plan.drawx(), plan.drawy(), plan.rotation * 90f + 180f)
        if(drawOut) Draw.rect(blockP.outRegion, plan.drawx(), plan.drawy(), plan.rotation * 90f)
    }
}
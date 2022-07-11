package avkott.world.draw

import arc.graphics.g2d.Draw
import mindustry.gen.Building
import mindustry.graphics.Layer
import mindustry.world.blocks.payloads.PayloadBlock
import mindustry.world.blocks.payloads.PayloadBlock.PayloadBlockBuild
import mindustry.world.draw.DrawBlock

class DrawPayload : DrawBlock() {
    override fun draw(b: Building) {
        if (b !is PayloadBlockBuild<*>) return
        val bu = b.block as PayloadBlock

        var fallback = true
        for (i in 0..3) {
            if (b.blends(i) && i != b.rotation) {
                Draw.rect(bu.inRegion, b.x, b.y, i * 90 - 180f)
                fallback = false
            }
        }
        if (fallback) Draw.rect(bu.inRegion, b.x, b.y, (b.rotation * 90).toFloat())
        Draw.z(Layer.blockOver)
        Draw.rect(bu.outRegion, b.x, b.y, b.rotdeg())
        b.drawPayload()
    }
}
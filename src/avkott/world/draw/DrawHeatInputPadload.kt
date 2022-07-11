package avkott.world.draw

import arc.Core
import arc.graphics.Blending
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.TextureRegion
import arc.math.Mathf
import mindustry.gen.Building
import mindustry.world.Block
import mindustry.world.blocks.heat.HeatConsumer
import mindustry.world.blocks.payloads.PayloadBlock
import mindustry.world.draw.DrawBlock

class DrawHeatInputPadload(var suffix: String) : DrawBlock() {
    var color = Color(1f, 0.22f, 0.22f, 0.8f)
    var heatPulse = 0.3f
    var heatPulseScl = 10f

    lateinit var heat: TextureRegion
    lateinit var heatIn: TextureRegion

    @Override
    override fun draw(b: Building) {
        if (b !is PayloadBlock.PayloadBlockBuild<*>) return

        if(b is HeatConsumer){
            val side = b.sideHeat()
            var fallback = true
            for(i in 0..3){
                if(side[i] > 0){
                    Draw.blend(Blending.additive)
                    Draw.color(color, side[i] / b.heatRequirement() * (color.a * (1f - heatPulse + Mathf.absin(heatPulseScl, heatPulse))))
                    if (b.blends(i) || i == b.rotation) {
                        fallback = false
                        Draw.rect(heatIn, b.x, b.y, i * 90f)
                    } else if(!fallback) Draw.rect(heat, b.x, b.y, i * 90f) else Draw.rect(heat, b.x, b.y, i * 90f + 180f)

                    Draw.blend()
                    Draw.color()
                }
            }
        }
    }

    @Override
    override fun load(block: Block){
        heat = Core.atlas.find("${block.name}$suffix")
        heatIn = Core.atlas.find("${block.name}$suffix-1")
    }
}

package avkott.world.draw

import arc.graphics.g2d.Draw
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.world.draw.DrawBlock

class DrawConstructPayload : DrawBlock() {
    var layer = Layer.blockOver

    override fun draw(b: Building) {
        if (b.payload == null) return
        Draw.draw(layer) {
            Drawf.construct(b, b.payload.icon(), 0f, b.progress(), b.warmup(), b.totalProgress())
        }
    }
}
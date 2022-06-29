package avkott.content

import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.math.Angles
import mindustry.content.Liquids
import mindustry.entities.Effect

object AvkFx {
    lateinit var nitrogenSmoke: Effect

    fun load(){
        nitrogenSmoke = Effect(90f) { e ->
            Draw.color(Liquids.nitrogen.color)
            Draw.alpha(e.fout() * 0.6f)

            Angles.randLenVectors(e.id.toLong(), 2, 4f + e.finpow() * e.rotation) { x, y ->
                Fill.circle(e.x + x, e.y + y, e.finpow() * 2.5f)
            }
        }
    }
}
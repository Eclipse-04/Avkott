package avkott.content

import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.math.Angles
import arc.math.Rand
import arc.math.geom.Vec2
import mindustry.content.Liquids
import mindustry.entities.Effect

object AvkFx {
    val rand = Rand()
    val v = Vec2()
    lateinit var nitrogenSmoke: Effect
    lateinit var shootSmokeMissileSmall: Effect

    fun load(){
        nitrogenSmoke = Effect(90f) { e ->
            Draw.color(Liquids.nitrogen.color)
            Draw.alpha(e.fout() * 0.6f)

            Angles.randLenVectors(e.id.toLong(), 2, 4f + e.finpow() * e.rotation) { x, y ->
                Fill.circle(e.x + x, e.y + y, e.finpow() * 2.5f)
            }
        }
        shootSmokeMissileSmall = Effect(70f) { e ->
            Draw.color(e.color)
            Draw.alpha(0.5f)
            Angles.randLenVectors(
                e.id.toLong(), 5, 8f * e.finpow() * 3.3f, e.rotation + 180, 30f
            ) { x, y ->
                Fill.circle(e.x + x, e.y + y, e.fout() * 3f)
            }
        }
    }
}
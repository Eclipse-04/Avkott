package avkott.content

import arc.graphics.g2d.Draw.alpha
import arc.graphics.g2d.Draw.color
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
    lateinit var instColorShoot: Effect

    fun load(){
        nitrogenSmoke = Effect(90f) {
            color(Liquids.nitrogen.color)
            alpha(it.fout() * 0.6f)

            Angles.randLenVectors(it.id.toLong(), 2, 4f + it.finpow() * it.rotation) { x, y ->
                Fill.circle(it.x + x, it.y + y, it.finpow() * 2.5f)
            }
        }
        shootSmokeMissileSmall = Effect(70f) {
            color(it.color)
            alpha(0.5f)
            Angles.randLenVectors(
                it.id.toLong(), 5, 8f * it.finpow() * 3.3f, it.rotation + 180, 30f
            ) { x, y ->
                Fill.circle(it.x + x, it.y + y, it.fout() * 3f)
            }
        }
    }
}
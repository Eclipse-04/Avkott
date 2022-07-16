package avkott.world.block.defend

import arc.Core
import arc.graphics.g2d.Draw
import arc.graphics.g2d.TextureRegion
import arc.util.Time
import mindustry.Vars
import mindustry.content.Fx
import mindustry.entities.Damage
import mindustry.entities.bullet.BulletType
import mindustry.gen.Building
import mindustry.gen.Unit
import mindustry.world.Block
import kotlin.math.sin

open class Bomb(name: String) : Block(name) {
    var bullet: BulletType? = null
    var bullets = 4
    var air = false
    var ground = true
    var radius = 0f
    var damage = 0f

    var topRegion: TextureRegion? = null
    var denotateEffect = Fx.explosion

    init {
        solid = false
        update = true
        targetable = false
    }

    override fun load() {
        super.load()
        topRegion = Core.atlas.find("$name-top")
    }

    inner class BombBuild : Building() {
        override fun drawTeam() {
        }

        override fun drawCracks() {
        }

        override fun draw() {
            super.draw()
            Draw.color(team.color, sin(Time.time * 6))
            Draw.rect(topRegion, x, y)
        }

        override fun unitOn(unit: Unit) {
            if (enabled && unit.team != team) {
                Damage.damage(team, x, y, radius * Vars.tilesize, damage, air, ground)
                denotateEffect.at(this)
                for(i in 0 until bullets) (bullet?.create(this, x, y, 360f))
                kill()
            }
        }

    }
}
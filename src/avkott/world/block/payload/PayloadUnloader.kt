package avkott.world.block.payload

import arc.graphics.g2d.Draw
import arc.graphics.g2d.TextureRegion
import arc.math.Mathf
import arc.math.geom.Geometry
import arc.math.geom.Vec2
import arc.util.Eachable
import avkott.world.block.payload.PayloadSilo.PayloadSiloBuild
import avkott.world.draw.DrawPayload
import mindustry.Vars
import mindustry.Vars.tilesize
import mindustry.content.Fx
import mindustry.entities.units.BuildPlan
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.world.blocks.payloads.Payload
import mindustry.world.blocks.payloads.PayloadBlock
import mindustry.world.blocks.payloads.UnitPayload
import mindustry.world.draw.DrawMulti
import mindustry.world.draw.DrawRegion

class PayloadUnloader(name: String) : PayloadBlock(name) {
    var spawnEffect = Fx.smeltsmoke
    var loadTime = 30f

    var drawer = DrawMulti(
        DrawRegion(""), DrawPayload().apply { blend = false; drawIn = false; drawPayload = false },
        DrawRegion("-top").apply { layer = Layer.blockOver + 0.11f }
    )

    init {
        rotate = true
        rotateDraw = false
        solidifes = true
        commandable = true
    }
    override fun drawOverlay(x: Float, y: Float, rotation: Int) {
        val r = Geometry.d4[rotation]
        val xOff = x - r.x * size * tilesize + tilesize / 2 * r.x
        val yOff = y - r.y * size * tilesize + tilesize / 2 * r.y
        val tile = Vars.world.tile(xOff.toInt() / 8, yOff.toInt() / 8)?.build
        if(tile != null && tile is PayloadSiloBuild) Drawf.select(tile.x, tile.y, tile.block.size.toFloat() * tilesize / 2, Pal.accent)
            else Drawf.select(xOff, yOff, tilesize.toFloat(), Pal.accent)
    }
    override fun icons(): Array<TextureRegion> {
        return arrayOf(region, outRegion, topRegion)
    }

    override fun load() {
        super.load()
        drawer.load(this)
    }

    override fun drawPlanRegion(plan: BuildPlan, list: Eachable<BuildPlan>) {
        drawer.drawPlan(this, plan, list)
    }

    inner class PayloadUnloaderBuild : PayloadBlockBuild<Payload>() {
        var silo: PayloadSiloBuild? = null
        var loadProgress = 0f
        var scl = 0f // visual
        var commandPos: Vec2? = null

        override fun updateTile() {
            super.updateTile()
            val silo = silo
            if(silo != null && payload == null && silo.storedSize > 0) {
                scl = 0f
                unloadPayload(silo)
                spawnEffect.at(this)
                loadProgress = 0f
            } else {
                if(loadProgress <= loadTime) loadProgress += edelta()
                else moveOutPayload()
            }
            scl = Mathf.lerpDelta(scl, 1f, 0.08f)
        }
        fun updateSilo() {
            silo = back() as? PayloadSiloBuild
        }

        fun unloadPayload(silo: PayloadSiloBuild) {
            payload = silo.stored.removeFirst()
            payVector.set(0f, 0f)
            payRotation = rotation.toFloat()

            if(payload is UnitPayload && commandPos != null) (payload as UnitPayload).unit.command().commandPosition(commandPos)
        }
        override fun onProximityUpdate() {
            super.onProximityUpdate()
            updateSilo()
        }

        override fun getCommandPosition(): Vec2? {
            return commandPos
        }

        override fun acceptPayload(source: Building, payload: Payload): Boolean {
            return false
        }

        override fun onCommand(target: Vec2) {
            commandPos = target
        }

        override fun draw() {
            drawer.draw(this)
            Draw.scl(scl)
            drawPayload()
            Draw.reset()
        }
    }
}
package avkott.world.block.payload

import arc.Core.bundle
import arc.graphics.g2d.Draw
import arc.graphics.g2d.TextureRegion
import arc.math.Mathf
import arc.util.Eachable
import avkott.world.draw.DrawPayload
import mindustry.Vars.tilePayload
import mindustry.Vars.tilesize
import mindustry.entities.units.BuildPlan
import mindustry.gen.Building
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.ui.Bar
import mindustry.world.blocks.payloads.Payload
import mindustry.world.blocks.payloads.PayloadBlock
import mindustry.world.draw.DrawMulti
import mindustry.world.draw.DrawRegion

class PayloadSilo(name: String) : PayloadBlock(name) {
    var storage = 8 * tilePayload

    var drawer = DrawMulti(
        DrawRegion(""),
        DrawPayload().apply { drawOut = false; defaultIn = false; drawPlan = false; drawPayload = false },
        DrawRegion("-top").apply { layer = Layer.blockOver + 0.11f }
    )

    override fun icons(): Array<TextureRegion> {
        return arrayOf(region, topRegion)
    }

    override fun load() {
        super.load()
        drawer.load(this)
    }

    override fun drawPlanRegion(plan: BuildPlan, list: Eachable<BuildPlan>) {
        drawer.drawPlan(this, plan, list)
    }

    override fun setBars() {
        super.setBars()

        addBar<PayloadSiloBuild>("Storage") {
            Bar(
                { "${bundle.format("bar.capacity", it.storedSize / tilesize)}\u00B2/${storage / tilesize}\u00B2" },
                { Pal.items },
                { it.storedSize / storage }
            )
        }
    }

    inner class PayloadSiloBuild : PayloadBlockBuild<Payload>() {
        var stored = ArrayList<Payload>()
        var scl = 1f

        val storedSize: Float
            get() {
                var total = 0f
                stored.forEach {
                    total += it.size()
                }
                return total
            }

        override fun updateTile() {
            super.updateTile()
            if(moveInPayload()) {
                stored.add(payload)
                payload = null
                scl = 1f
            } else if(payload != null) scl = Mathf.lerp(scl, 0f, 0.08f)
        }

        override fun acceptPayload(source: Building, payload: Payload): Boolean {
            return super.acceptPayload(source, payload) && storedSize + payload.size() <= storage
        }

        override fun draw() {
            drawer.draw(this)
            Draw.scl(scl)
            drawPayload()
            Draw.reset()
        }
    }
}
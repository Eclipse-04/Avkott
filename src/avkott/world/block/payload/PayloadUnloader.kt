package avkott.world.block.payload

import arc.graphics.g2d.Draw
import arc.graphics.g2d.TextureRegion
import arc.math.Mathf
import arc.math.geom.Geometry
import arc.math.geom.Vec2
import arc.scene.ui.layout.Table
import arc.util.Eachable
import avkott.world.block.payload.PayloadSilo.PayloadSiloBuild
import avkott.world.draw.DrawPayload
import mindustry.Vars
import mindustry.Vars.content
import mindustry.Vars.tilesize
import mindustry.content.Fx
import mindustry.ctype.UnlockableContent
import mindustry.entities.units.BuildPlan
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.type.UnitType
import mindustry.world.Block
import mindustry.world.blocks.ItemSelection
import mindustry.world.blocks.payloads.BuildPayload
import mindustry.world.blocks.payloads.Payload
import mindustry.world.blocks.payloads.PayloadBlock
import mindustry.world.blocks.payloads.UnitPayload
import mindustry.world.blocks.storage.CoreBlock
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
        configurable = true
        saveConfig = true
        outputsPayload = true
        config(Block::class.java) { tile: PayloadUnloaderBuild, block: Block ->
            tile.configBlock = block
            tile.configUnit = null
        }
        config(UnitType::class.java) { tile: PayloadUnloaderBuild, unit: UnitType ->
            tile.configBlock = null
            tile.configUnit = unit
        }
        configClear { tile: PayloadUnloaderBuild ->
            tile.configBlock = null
            tile.configUnit = null
        }
    }
    override fun drawOverlay(x: Float, y: Float, rotation: Int) {
        val r = Geometry.d4[rotation]
        val xOff = x - (size / 2 + 1) * r.x * tilesize
        val yOff = y - (size / 2 + 1) * r.y * tilesize
        val tile = Vars.world.tile(xOff.toInt() / tilesize, yOff.toInt() / tilesize)?.build
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
    fun canProduce(b: Block): Boolean {
        return b.isVisible && b.size < size && b !is CoreBlock && !Vars.state.rules.bannedBlocks.contains(b) && b.environmentBuildable()
    }
    fun canProduce(t: UnitType): Boolean {
        return !t.isHidden && !t.isBanned && t.hitSize < size * tilesize && t.supportsEnv(Vars.state.rules.env)
    }
    override fun drawPlanRegion(plan: BuildPlan, list: Eachable<BuildPlan>) {
        drawer.drawPlan(this, plan, list)
    }

    inner class PayloadUnloaderBuild : PayloadBlockBuild<Payload>() {
        var silo: PayloadSiloBuild? = null
        var loadProgress = 0f
        var scl = 0f // visual
        var commandPos: Vec2? = null
        var configBlock: Block? = null
        var configUnit: UnitType? = null

        override fun updateTile() {
            super.updateTile()
            val silo = silo
            if(silo != null && payload == null && silo.storedSize > 0) {
                if (!unloadPayload(silo)) return
                spawnEffect.at(this)
                scl = 0f
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

        fun unloadPayload(silo: PayloadSiloBuild): Boolean {
            if(configUnit != null) {
                payload = silo.stored.firstOrNull { it is UnitPayload && it.unit.type == configUnit} ?: return false
                silo.stored.remove(payload)
            } else if(configBlock != null) {
                payload = silo.stored.firstOrNull { it is BuildPayload && it.block() == configBlock} ?: return false
                silo.stored.remove(payload)
            } else payload = silo.stored.removeFirst()

            payVector.set(0f, 0f)
            payRotation = rotation.toFloat()
            if(payload is UnitPayload && commandPos != null) (payload as UnitPayload).unit.command().commandPosition(commandPos)
            return true
        }
        override fun onProximityUpdate() {
            super.onProximityUpdate()
            updateSilo()
        }
        override fun buildConfiguration(table: Table?) {
            ItemSelection.buildTable(this@PayloadUnloader, table,
                content.blocks().select { this@PayloadUnloader.canProduce(it)
                }.`as`<UnlockableContent?>()
                .add(content.units().select { this@PayloadUnloader.canProduce(it)
                }.`as`()),
                { config() as UnlockableContent? }
            ) { configure(it) }
        }
        override fun config(): Any? {
            return if (configUnit == null) configBlock else configUnit
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
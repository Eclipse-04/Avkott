package avkott.world.block.payload

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.scene.ui.layout.Table
import arc.struct.Seq
import arc.util.Log
import mindustry.gen.Building
import mindustry.type.Item
import mindustry.type.ItemStack
import mindustry.ui.Styles
import mindustry.world.Block
import mindustry.world.blocks.ItemSelection
import mindustry.world.blocks.payloads.BuildPayload
import mindustry.world.blocks.payloads.Payload
import mindustry.world.blocks.payloads.PayloadBlock

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class PayloadCrafter(name: String) : PayloadBlock(name) {
    /**
     * The recipe must contain a unique [Recipe.payload]
     */
    var recipes = Seq<Recipe>(4)
    class Recipe (
        val payload: Block,
        val time: Float,
        val requirements: Array<ItemStack>,
        val output: Array<ItemStack>
    ){
        val item2Stack = output.associateBy { it.item }
    }

    init {
        hasItems = true
        hasLiquids = true
        hasPower = true
        itemCapacity = 100
        liquidCapacity = 100f
        update = true
        outputsPayload = true
        size = 3
        rotate = true
        solid = true
        configurable = true
        clearOnDoubleTap = true
        saveConfig = true

        config(java.lang.Integer::class.java) { tile: PayloadCrafterBuild, i: Integer ->
            if (!configurable) return@config
            val new = i.toInt()
            if (tile.currentRecipe != new) {
                tile.currentRecipe = if(new < 0) -1 else new.coerceIn(0, recipes.size - 1)
                tile.progress = 0f
            }
        }
    }

    inner class PayloadCrafterBuild : PayloadBlockBuild<BuildPayload>() {
        var currentRecipe = -1
        var progress = 0f
        var exporting = false
        //this thing
        override fun updateTile() {
            super.updateTile()
            Log.info(progress)
            if (currentRecipe != -1 && efficiency > 0.01f) {
                val recipe = recipes.get(currentRecipe)
                if(canExport()) {
                    progress = 0f
                    moveOutPayload()
                } else if (moveInPayload()) {
                    if (progress < 1f) progress += getProgressIncrease(recipe.time) else exporting = true
                }
            }
        }
        override fun handlePayload(source: Building, payload: Payload) {
            super.handlePayload(source, payload)
            exporting = false
        }
        override fun buildConfiguration(table: Table) {
            val payloads: Seq<Block> = recipes.map { it.payload }.filter {
                it.unlockedNow()
            }

            if (payloads.any()) {
                ItemSelection.buildTable(this@PayloadCrafter, table, payloads,
                    { if (currentRecipe < 0) null else recipes[currentRecipe].payload }
                ) {
                    configure(recipes.indexOf { recipe -> recipe.payload == it })
                }
            } else {
                table.table(Styles.black3) { t: Table -> t.add("@none").color(Color.lightGray) }
            }
        }
        fun canExport(): Boolean {
            return payload != null && (exporting
            || payload.block() != recipes[currentRecipe].payload)
        }

        override fun acceptPayload(source: Building, payload: Payload): Boolean {
            return if (currentRecipe >= 0 && payload is BuildPayload) {
                this.payload == null && payload.block() == recipes[currentRecipe].payload
            } else false
        }

        override fun acceptItem(source: Building, item: Item): Boolean {
            return if (currentRecipe in 0 until recipes.size) {
                val recipe = recipes[currentRecipe]
                item in recipe.item2Stack && items[item] < this.getMaximumAccepted(item)
            } else false
        }

        override fun config() = currentRecipe

        override fun draw() {
            Draw.rect(region, x, y)
            //draw input
            var fallback = true
            for (i in 0..3) {
                if (blends(i) && i != rotation) {
                    Draw.rect(inRegion, x, y, (i * 90 - 180).toFloat())
                    fallback = false
                }
            }
            if (fallback) Draw.rect(inRegion, x, y, (rotation * 90).toFloat())
            Draw.rect(outRegion, x, y, rotdeg())

            Draw.rect(topRegion, x, y)
            drawPayload()
        }
    }
}
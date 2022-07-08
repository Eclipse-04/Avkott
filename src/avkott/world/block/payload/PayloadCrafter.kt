package avkott.world.block.payload

import arc.graphics.g2d.Draw
import arc.scene.ui.layout.Table
import arc.struct.Seq
import mindustry.Vars
import mindustry.content.Items
import mindustry.gen.Building
import mindustry.type.Item
import mindustry.world.Block
import mindustry.world.blocks.ItemSelection
import mindustry.world.blocks.payloads.BuildPayload
import mindustry.world.blocks.payloads.PayloadBlock

class PayloadCrafter(name: String) : PayloadBlock(name) {
    var recipes = Seq<Recipe>(4)

    inner class Recipe(val payload: Block, val time: Float, val requirements: Array<Item>)

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

        config(Int::class.java) { tile: PayloadCrafterBuild, i: Int ->
            if (!configurable) return@config
            if (tile.currentRecipe == i) return@config
            tile.currentRecipe = if (i < 0 || i >= recipes.size) -1 else i
            tile.progress = 0f
        }
    }
    inner class PayloadCrafterBuild : PayloadBlockBuild<BuildPayload>() {
        var currentRecipe = -1
        var progress = 0f

        override fun updateTile() {
            super.updateTile()
            if (currentRecipe != -1 && efficiency > 0f) {
                val recipe = recipes.get(currentRecipe)
                progress += getProgressIncrease(recipe.time)
                //actual outputting soon
            }
        }

        override fun buildConfiguration(table: Table) {
            //liplum help aaaaaaaaaaaaaaaaaaaaaAAAAAAAAAA
        }

        //modify soon
        fun canExport(): Boolean {
            return progress >= 1f
        }

        override fun acceptItem(source: Building, item: Item): Boolean {
            if (currentRecipe > 0){
                val recipe = recipes.get(currentRecipe)
                return super.acceptItem(source, item) && recipe.requirements.contains(item)
            } else {
                return false
            }
        }
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
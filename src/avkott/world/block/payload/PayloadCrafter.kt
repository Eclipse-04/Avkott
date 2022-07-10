package avkott.world.block.payload

import arc.Core.bundle
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.TextureRegion
import arc.math.Mathf
import arc.scene.ui.layout.Table
import arc.struct.Seq
import arc.util.Eachable
import arc.util.Strings.autoFixed
import avkott.extension.add
import avkott.ui.addT
import mindustry.content.Fx
import mindustry.entities.units.BuildPlan
import mindustry.gen.Building
import mindustry.gen.Icon
import mindustry.gen.Tex
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.type.Item
import mindustry.type.ItemStack
import mindustry.ui.Bar
import mindustry.ui.ItemDisplay
import mindustry.ui.Styles
import mindustry.world.Block
import mindustry.world.blocks.ItemSelection
import mindustry.world.blocks.heat.HeatConsumer
import mindustry.world.blocks.payloads.BuildPayload
import mindustry.world.blocks.payloads.Payload
import mindustry.world.blocks.payloads.PayloadBlock
import mindustry.world.draw.DrawDefault
import mindustry.world.meta.Stat

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class PayloadCrafter(name: String) : PayloadBlock(name) {
    //The recipe must contain a unique [Recipe.payload]
    var recipes = Seq<Recipe>(4)
    var craftEffect = Fx.smeltsmoke
    //would be use soon
    var mainDrawer = DrawDefault()
    var overheatScale = 1f
    var maxEfficiency = 4f
    var warmupSpeed = 0.019f
    var hasHeat = false

    class Recipe(
        val payload: Block,
        val time: Float,
        val requirements: Array<ItemStack>,
        val output: Array<ItemStack>,
        val power: Float = 0f,
        val heat: Float = 0f,
    ) {
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
            if (tile.currentRecipeIndex != new) {
                tile.currentRecipeIndex = if (new < 0) -1 else new.coerceIn(0, recipes.size - 1)
                tile.progress = 0f
            }
        }
    }

    override fun init() {
        consumePowerDynamic { b: PayloadCrafterBuild ->
            if (b.currentRecipeIndex != -1) recipes[b.currentRecipeIndex].power else 0f
        }
        hasHeat = recipes.any { it.heat > 0f }
        // Initialize others before vanilla one
        super.init()
    }

    override fun icons(): Array<TextureRegion> {
        return arrayOf(region, inRegion, outRegion, topRegion)
    }

    override fun setBars() {
        super.setBars()
        if (hasHeat) {
            addBar<PayloadCrafterBuild>("heat") {
                Bar(
                    { bundle.format("bar.heatpercent", it.heat.toInt(), (it.efficiencyScale() * 100).toInt()) },
                    { Pal.lightOrange },
                    {
                        if (it.enabledRecipe) {
                            it.currentRecipe.run {
                                if (heat > 0f) it.heat / heat
                                else 0f
                            }
                        } else 0f
                    })
            }
        }
    }

    override fun drawPlanRegion(plan: BuildPlan, list: Eachable<BuildPlan>) {
        Draw.rect(region, plan.drawx(), plan.drawy())
        Draw.rect(inRegion, plan.drawx(), plan.drawy(), (plan.rotation * 90).toFloat())
        Draw.rect(outRegion, plan.drawx(), plan.drawy(), (plan.rotation * 90).toFloat())
        Draw.rect(topRegion, plan.drawx(), plan.drawy())
    }

    override fun setStats() {
        super.setStats()

        stats.add(Stat.output) { table ->
            table.row()
            //terrible code
            //edited this line
            for (recipe in recipes) {
                table.addT {
                    background(Tex.whiteui)
                    setColor(Pal.darkestGray)
                    if (!recipe.payload.isPlaceable) {
                        image(Icon.cancel).color(Pal.remove).size(40f)
                        return@addT
                    }
                    if (recipe.payload.unlockedNow()) {
                        image(recipe.payload.uiIcon).size(40f).top().left().padLeft(20f).padTop(20f)
                        addT {
                            addT {
                                add(recipe.payload.localizedName).left()
                                if (recipe.power > 0f) addT {
                                    image(Icon.power).padRight(10f).color(Pal.power)
                                    add("${autoFixed(recipe.power * 60f, 1)} ${bundle["unit.powerunits"]}")
                                }.right().padLeft(30f).color(Pal.power)
                            }.row()

                            image().growX().pad(5f).padLeft(0f).padRight(0f).height(4f).color(Color.darkGray)
                            row()
                            addT {
                                add("${bundle["stat.input"]}:").width(100f)
                                addT {
                                    recipe.requirements.forEach {
                                        add(ItemDisplay(it.item, it.amount, recipe.time, false))
                                    }
                                }.left().growX().row()
                                add("${bundle["stat.output"]}:").width(100f)
                                addT {
                                    recipe.output.forEach {
                                        add(ItemDisplay(it.item, it.amount, recipe.time, false))
                                    }
                                }.left().growX()
                            }.left().row()
                            val timeReq = "${bundle["stat.productiontime"]}: ${
                                autoFixed(recipe.time / 60f, 1)
                            } ${bundle["unit.seconds"]}"
                            add(timeReq).color(Color.lightGray).left()
                        }.pad(20f)
                    }
                }
            }
        }
    }

    inner class PayloadCrafterBuild :
        PayloadBlockBuild<BuildPayload>(), HeatConsumer {
        var currentRecipeIndex = -1
        var progress = 0f
        var exporting = false
        var sideHeat = FloatArray(4)
        var heat = 0f
        var warmup = 0f
        val currentRecipe: Recipe
            get() = recipes[currentRecipeIndex.coerceIn(0, recipes.size - 1)]
        val enabledRecipe: Boolean
            get() = currentRecipeIndex >= 0

        override fun updateTile() {
            super.updateTile()
            if (enabledRecipe) {
                val recipe = currentRecipe
                heat = if (recipe.heat > 0f) calculateHeat(sideHeat)
                else 0f
                if (efficiency > 0.01f) {
                    if (canExport()) {
                        moveOutPayload()
                    } else if (moveInPayload()) {
                        if (canCraft()) {
                            if (progress < 1f) progress += getProgressIncrease(recipe.time) else {
                                progress %= 1f
                                craftEffect.at(x, y)
                                payload.build.items.remove(recipe.requirements)
                                payload.build.items.add(recipe.output) // done
                            }
                        } else exporting = true
                    }
                }
            } else {
                heat = 0f
            }
            warmup = Mathf.approachDelta(warmup, warmupTarget(), warmupSpeed)
        }

        fun canCraft(): Boolean {
            val payBuild = payload?.build
            return if (payBuild != null)
                enabledRecipe && payBuild.items.has(currentRecipe.requirements)
            else false
        }

        override fun shouldConsume(): Boolean {
            return super.shouldConsume() && canCraft()
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
                    { if (currentRecipeIndex < 0) null else currentRecipe.payload }
                ) {
                    configure(recipes.indexOf { recipe -> recipe.payload == it })
                }
            } else {
                table.table(Styles.black3) { t: Table -> t.add("@none").color(Color.lightGray) }
            }
        }

        fun canExport(): Boolean {
            return payload != null && (exporting
                    || payload.block() != currentRecipe.payload)
        }

        override fun acceptPayload(source: Building, payload: Payload): Boolean {
            return if (currentRecipeIndex >= 0 && payload is BuildPayload) {
                this.payload == null && payload.block() == currentRecipe.payload
            } else false
        }
        //????
        override fun acceptItem(source: Building, item: Item): Boolean {
            return if (currentRecipeIndex in 0 until recipes.size) {
                item in currentRecipe.item2Stack && items[item] < this.getMaximumAccepted(item)
            } else false
        }

        override fun config() = currentRecipeIndex
        fun efficiencyScale() =
            if (enabledRecipe) {
                val recipe = currentRecipe
                val req = recipe.heat
                val over = (heat - recipe.heat).coerceAtLeast(0f)
                (heat / req + (over / req) * overheatScale).coerceAtLeast(maxEfficiency)
            } else 1f

        override fun warmup(): Float {
            return warmup
        }

        fun warmupTarget() = if (enabledRecipe) {
            val recipe = currentRecipe
            val req = recipe.heat
            if (req > 0f) (heat / req).coerceIn(0f, 1f)
            else 1f
        } else 0f

        override fun updateEfficiencyMultiplier() {
            val scale = efficiencyScale()
            efficiency *= scale
            potentialEfficiency *= scale
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

            Draw.z(Layer.blockOver)
            Draw.rect(outRegion, x, y, rotdeg())
            drawPayload()
            Draw.rect(topRegion, x, y)
        }

        override fun sideHeat() = sideHeat
        override fun heatRequirement() = currentRecipe.heat
    }
}
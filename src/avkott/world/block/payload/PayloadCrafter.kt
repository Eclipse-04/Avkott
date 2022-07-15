package avkott.world.block.payload

import arc.Core
import arc.Core.bundle
import arc.graphics.Color
import arc.graphics.g2d.TextureRegion
import arc.math.Mathf
import arc.scene.ui.ImageButton
import arc.scene.ui.layout.Table
import arc.struct.Seq
import arc.util.Eachable
import arc.util.Strings.autoFixed
import avkott.extension.add
import avkott.ui.addT
import avkott.ui.collapser
import avkott.world.draw.DrawHeatInputPadload
import avkott.world.draw.DrawPayload
import avkott.world.draw.DrawRecipe
import mindustry.Vars
import mindustry.content.Fx
import mindustry.entities.Effect
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
import mindustry.world.draw.DrawBlock
import mindustry.world.draw.DrawMulti
import mindustry.world.draw.DrawRegion
import mindustry.world.meta.Stat

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class PayloadCrafter(name: String) : PayloadBlock(name) {
    //The recipe must contain a unique [Recipe.payload]
    var recipes = Seq<Recipe>(4)
    var craftEffect: Effect = Fx.smeltsmoke
    var drawer = DrawMulti(
        DrawRegion(""), DrawPayload(),
        DrawRecipe(),
        DrawRegion("-top").apply { layer = Layer.blockOver + 0.2f },
        DrawHeatInputPadload("-heat")
    )
    var overheatScale = 1f
    var maxEfficiency = 4f
    var warmupSpeed = 0.02f
    var hasHeat = false

    class Recipe(
        val payload: Block,
        val time: Float,
        val requirements: Array<ItemStack>,
        val output: Array<ItemStack>,
        val power: Float = 0f,
        val heat: Float = 0f,
        val inputItems: Array<ItemStack> = emptyArray(),
        val outputItems: Array<ItemStack> = emptyArray(),
        val drawer: DrawBlock? = null,
        var consumePayload: Boolean = false,
        val convertInto: Block? = null
    ) {
        val itemOut2Stack = output.associateBy { it.item }
        val itemIn2Stack = inputItems.associateBy { it.item }
        val payloadItem = requirements.isNotEmpty() or output.isNotEmpty()
        val payloadExItem = inputItems.isNotEmpty() or outputItems.isNotEmpty() or (heat > 0)
        init {
            if(convertInto != null) consumePayload = true
        }
    }

    val Recipe.description: String
        get() = bundle["$name.recipe-${recipes.indexOf(this)}.desc", ""]

    init {
        hasItems = true
        hasLiquids = true
        hasPower = true
        itemCapacity = 100
        liquidCapacity = 100f
        update = true
        outputsPayload = true
        rotateDraw = false
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
                if(!Vars.headless){
                    tile.rebuildHoveredInfoIfNeed()
                }
            }
        }
    }

    override fun init() {
        consumePowerDynamic { b: PayloadCrafterBuild ->
            if (b.enabledRecipe) b.currentRecipe.power else 0f
        }
        hasHeat = recipes.any { it.heat > 0f }
        // Initialize others before vanilla one
        super.init()
    }

    override fun icons(): Array<TextureRegion> {
        return arrayOf(region, inRegion, outRegion, topRegion)
    }
    var hoveredInfo: Table? = null
    fun PayloadCrafterBuild.genHeatBar() = Bar(
        {
            bundle.format(
                "bar.heatpercent",
                heat.toInt(),
                (efficiencyScale() * 100).toInt()
            )
        },
        { Pal.lightOrange },
        {
            if (enabledRecipe) {
                currentRecipe.let {
                    if (it.heat > 0f) heat / it.heat else 0f
                }
            } else 0f
        })

    override fun load() {
        super.load()
        drawer.load(this)
    }

    override fun drawPlanRegion(plan: BuildPlan, list: Eachable<BuildPlan>) {
        drawer.drawPlan(this, plan, list)
    }

    inner class PayloadCrafterBuild :
        PayloadBlockBuild<BuildPayload>(), HeatConsumer {
        var currentRecipeIndex = -1
        var progress = 0f
        var totalProgress = 0f
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
                heat = if (currentRecipe.heat > 0f) calculateHeat(sideHeat)
                else 0f
                if (canExport()) {
                    moveOutPayload()
                } else if (moveInPayload()) {
                    if (canCraft()) {
                        if (progress < 1f) {
                            progress += getProgressIncrease(currentRecipe.time)
                            totalProgress += edelta()
                        } else {
                            progress %= 1f
                            craft()
                            // done
                        }
                    } else if (currentRecipe.inputItems.isEmpty()) exporting = true
                }
            } else {
                heat = 0f
            }
            warmup = Mathf.approachDelta(warmup, warmupTarget(), warmupSpeed)
            dumpOutputs()
        }

        fun dumpOutputs() {
            if (currentRecipe.output.isNotEmpty()) {
                for (output in currentRecipe.output) {
                    dump(output.item)
                }
            }
            if (currentRecipe.outputItems.isNotEmpty()) {
                for (output in currentRecipe.outputItems) {
                    dump(output.item)
                }
            }
        }

        override fun progress(): Float = progress
        override fun totalProgress(): Float = totalProgress

        fun craft() {
            if (currentRecipe.requirements.isNotEmpty()) payload.build.items.remove(currentRecipe.requirements)
            if (currentRecipe.output.isNotEmpty()) payload.build.items.add(currentRecipe.output)

            if (currentRecipe.outputItems.isNotEmpty()) {
                for (output in currentRecipe.outputItems) {
                    for (i in 0 until output.amount) {
                        offload(output.item)
                    }
                }
            }

            if (currentRecipe.consumePayload) {
                payload = null
                if (currentRecipe.convertInto != null) payload = BuildPayload(currentRecipe.convertInto, team)
            }

            if (wasVisible) craftEffect.at(x, y)
        }

        val useHeat: Boolean
            get() = enabledRecipe && currentRecipe.heat > 0f

        fun canCraft(): Boolean {
            val payBuild = payload?.build
            return if (payBuild != null)
                enabledRecipe && payload.block() == currentRecipe.payload &&
                (currentRecipe.requirements.isEmpty() || payBuild.items.has(currentRecipe.requirements)) &&
                (currentRecipe.inputItems.isEmpty() || items.has(currentRecipe.inputItems))
            else false
        }

        override fun shouldConsume(): Boolean {
            return super.shouldConsume() && canCraft()
        }

        override fun handlePayload(source: Building, payload: Payload) {
            super.handlePayload(source, payload)
            exporting = false
        }
        override fun display(table: Table) {
            super.display(table)
            hoveredInfo = table
        }
        fun rebuildHoveredInfoIfNeed() {
            try {
                val info = hoveredInfo
                if (info != null) {
                    info.clear()
                    display(info)
                }
            } catch (ignored: Exception) {
                // Maybe null pointer or cast exception
            }
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

        override fun acceptItem(source: Building, item: Item): Boolean {
            return if (currentRecipeIndex in 0 until recipes.size) {
                item in currentRecipe.itemIn2Stack && items[item] < this.getMaximumAccepted(item)
            } else false
        }

        override fun config() = currentRecipeIndex
        fun efficiencyScale(): Float = if (currentRecipe.heat > 0f) {
            val over = (heat - currentRecipe.heat).coerceAtLeast(0f)
            ((heat / currentRecipe.heat).coerceIn(0f, 1f) + over / currentRecipe.heat * overheatScale)
                .coerceAtMost(maxEfficiency)
        } else 1f

        override fun warmup(): Float = warmup

        fun warmupTarget() = if (enabledRecipe && canCraft()) {
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
            drawer.draw(this)
        }

        override fun sideHeat() = sideHeat
        override fun heatRequirement() = currentRecipe.heat
        override fun displayBars(table: Table) {
            for (barProv in listBars()) {
                val bar = barProv.get(this) ?: continue
                table.add(bar).growX()
                table.row()
            }
            if (useHeat) {
                val bar = genHeatBar()
                table.add(bar).growX()
                table.row()
            }
        }
    }

    override fun setStats() {
        super.setStats()

        stats.add(Stat.output) { stat ->
            stat.row()
            //terrible code
            //edited this line
            for (recipe in recipes) {
                stat.addT {
                    background(Tex.whiteui)
                    setColor(Pal.darkestGray)
                    if (!recipe.payload.isPlaceable) {
                        image(Icon.cancel).color(Pal.remove).size(40f)
                        return@addT
                    }
                    if (recipe.payload.unlockedNow()) {
                        addT{
                            image(recipe.payload.uiIcon).size(40f).row()
                            if(recipe.convertInto != null) {
                                image(Icon.down).size(40f).color(Pal.darkishGray).padTop(10f).padBottom(10f).row()
                                image(recipe.convertInto.uiIcon).size(40f)
                            }
                        }.top().left().padLeft(20f).padTop(20f).padBottom(20f)
                        addT {
                            addT {
                                addT {
                                    add(recipe.payload.localizedName).left()
                                    if (recipe.consumePayload) {
                                        row()
                                        add(bundle["stat.consumePayload"]).color(Color.lightGray).left()
                                    }
                                }.left()
                                if (recipe.power > 0f) addT {
                                    image(Icon.power).padRight(5f).color(Pal.power)
                                    add("${autoFixed(recipe.power * 60f, 1)} ${bundle["unit.powerunits"]}")
                                }.right().padLeft(30f).color(Pal.power)
                            }.row()
                            val recipeDesc = recipe.description
                            if (recipeDesc.isNotBlank()) {
                                val info = ImageButton(Icon.downOpen.region, Styles.clearNonei)
                                addT {
                                    add(info)
                                    image().growX().pad(7f).padLeft(5f).padRight(0f).height(4f).color(Color.darkGray)
                                }.growX().row()
                                var collapsed by collapser(false) {
                                    add(recipeDesc).fillX().left().pad(0f, 10f, 4f, 10f).wrap().row()
                                    image().growX().pad(7f).padLeft(0f).padRight(0f).height(4f).color(Color.darkGray)
                                }
                                row()
                                info.update {
                                    val tr = if (collapsed) Icon.downOpen else Icon.upOpen
                                    info.style.imageUp = tr
                                }
                                info.changed {
                                    collapsed = !collapsed
                                }
                            } else {
                                image().growX().pad(7f).padLeft(0f).padRight(0f).height(4f).color(Color.darkGray).row()
                            }
                            addT {
                                if (recipe.requirements.isNotEmpty()) {
                                    add("${bundle["stat.input"]}:").left().padRight(20f)
                                    addT {
                                        recipe.requirements.forEach {
                                            add(ItemDisplay(it.item, it.amount, recipe.time, false).left())
                                        }
                                    }.left().row()
                                }
                                if (recipe.output.isNotEmpty() || recipe.outputItems.isNotEmpty()) {
                                    add("${bundle["stat.output"]}:").left().padRight(20f)
                                    addT {
                                        recipe.output.forEach {
                                            add(ItemDisplay(it.item, it.amount, recipe.time, false).left())
                                        }
                                        recipe.outputItems.forEach {
                                            add(ItemDisplay(it.item, it.amount, recipe.time, false).left())
                                        }
                                    }.left().row()
                                }
                                if (recipe.heat > 0f) {
                                    add("${bundle["bar.heat"]}:").width(70f).left().padRight(20f)
                                    addT {
                                        image(Core.atlas.find("status-burning")).padRight(5f)
                                        add("${autoFixed(recipe.heat, 1)} ${bundle["unit.heatunits"]}")
                                    }.left()
                                }
                            }.left().row()
                            add("${bundle["stat.productiontime"]}: ${autoFixed(recipe.time / 60f, 1)} ${bundle["unit.seconds"]}").color(
                                Color.lightGray
                            ).left().row()
                            if (recipe.heat > 0f) add(
                                "${bundle["stat.maxefficiency"]}: ${
                                    autoFixed(
                                        maxEfficiency * 100,
                                        1
                                    )
                                }%"
                            ).color(Color.lightGray).left()
                        }.pad(20f)
                    }
                }.growX().padBottom(20f).row()
            }
        }
    }

}
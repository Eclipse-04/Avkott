package avkott.world.draw

import avkott.world.block.payload.PayloadCrafter
import mindustry.gen.Building
import mindustry.world.draw.DrawBlock

class DrawRecipe : DrawBlock() {

    override fun draw(build: Building) {
        val crafter = build as? PayloadCrafter.PayloadCrafterBuild ?: return
        if (crafter.enabledRecipe) crafter.currentRecipe.drawer?.draw(crafter)
    }
}
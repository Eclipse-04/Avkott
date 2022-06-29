package avkott.content

import mindustry.content.Liquids
import mindustry.type.Liquid

object AvkLiquids {
    lateinit var liquidNitrogen: Liquid

    fun load(){
        liquidNitrogen = Liquid("liquid-nitrogen").apply {
            color = Liquids.nitrogen.color
            hidden = true
            effect = AvkStatusEffects.nitrogenFrozen
            particleEffect = AvkFx.nitrogenSmoke
            particleSpacing = 10f
            temperature = 0.2f
            coolant = false
        }
    }
}
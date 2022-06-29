package avkott.content

import mindustry.content.Liquids
import mindustry.type.StatusEffect

object AvkStatusEffects {
    lateinit var nitrogenFrozen: StatusEffect

    fun load(){
        nitrogenFrozen = StatusEffect("nitrogen-frozen").apply {
            color = Liquids.nitrogen.color
            speedMultiplier = 0.4f
            healthMultiplier = 0.85f
            reloadMultiplier = 0.7f
            effect = AvkFx.nitrogenSmoke
            effectChance = 0.3f
            damage = 0.3f
        }
    }
}
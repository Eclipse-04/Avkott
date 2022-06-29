package avkott

import avkott.content.*
import mindustry.mod.*

class Avkott : Mod(){

    init{

    }

    override fun loadContent(){
        AvkFx.load()
        AvkStatusEffects.load()
        AvkLiquids.load()
        AvkUnitTypes.load()
        AvkBlocks.load()
    }
}

package avkott.extension

import mindustry.type.ItemStack
import mindustry.world.modules.ItemModule

fun ItemModule.add(stacks:Array<ItemStack>){
    for(stack in stacks){
        add(stack.item, stack.amount)
    }
}
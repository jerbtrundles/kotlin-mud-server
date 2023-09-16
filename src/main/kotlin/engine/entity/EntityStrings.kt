package engine.entity

import engine.entity.behavior.EntityAction
import engine.item.ItemBase

class EntityStrings(
    val entity: EntityBase,
) {
    val standString
        get() = "${entity.randomName} stands up."

//    fun getItemString(item: ItemBase): String {
//
//    }
//    fun get(action: EntityAction) {
//        when (action) {
//            EntityAction.GET_ANY_ITEM -> getitem
//        }
//    }
}
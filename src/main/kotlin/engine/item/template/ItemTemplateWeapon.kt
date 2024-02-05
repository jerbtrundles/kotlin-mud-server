package engine.item.template

import engine.item.ItemWeapon
import engine.item.ItemWeaponType

class ItemTemplateWeapon(
    name: String,
    description: String,
    weight: Double,
    value: Int,
    keywords: List<String>,
    val power: Int,
    val weaponType: ItemWeaponType
): ItemTemplate(name, description, weight, value, keywords) {
    override fun createItem() = ItemWeapon(this)
}
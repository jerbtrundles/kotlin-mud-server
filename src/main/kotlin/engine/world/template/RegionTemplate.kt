package engine.world.template

import com.beust.klaxon.Json
import engine.world.Region

class RegionTemplate(
    @Json(name = "region-id")
    val id: Int,
    @Json(name = "region-name")
    val name: String,
    @Json(name = "region-subregions")
    val subregionTemplates: List<SubregionTemplate>
) {
    fun toRegion(): Region {
        return Region(
            id = id,
            name = name,
            subregions = subregionTemplates.map { subregionTemplate -> subregionTemplate.toSubregion() }
        )
    }
}
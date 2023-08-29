package processor.models

import processor.models.enums.GCType

data class GCBan(val hero: Hero, val season: Int, val week: Int, val gcType: GCType) {
}
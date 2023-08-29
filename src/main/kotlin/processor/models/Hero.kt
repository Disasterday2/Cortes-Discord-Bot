package processor.models

import processor.models.enums.HeroClassType
import processor.models.enums.HeroDamageType

data class Hero(val heroName: String, val damageType: HeroDamageType, val type: HeroClassType)
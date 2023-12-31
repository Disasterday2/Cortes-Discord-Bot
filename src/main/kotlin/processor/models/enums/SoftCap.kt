package processor.models.enums

enum class SoftCap(
    val maxK: Int,
    val x1: Int,
    val a1: Int,
    val b1: Int,
    val x2: Int,
    val a2: Int,
    val b2: Int,
    val minK: Int,
    val x3: Int,
    val a3: Int,
    val b3: Int,
    val x4: Int,
    val a4: Int,
    val b4: Int
) {
    CRIT(
        2000,
        2000,
        1,
        1500,
        1500,
        500,
        750,
        0,
        -500,
        0,
        0,
        0,
        0,
        0
    ),
    ACC(
        2000,
        2000,
        1,
        1500,
        1500,
        500,
        750,
        -920,
        -2,
        3,
        -938,
        1,
        0,
        0
    ),

    CCACC(
        900,
        900,
        1000000,
        1000000,
        450,
        1000,
        0,
        0,
        -500,
        0,
        0,
        0,
        0,
        0
    ),

    DODGE(
        1000,
        1000,
        3,
        0,
        500,
        500,
        250,
        0,
        -500,
        0,
        0,
        0,
        0,
        0
    ),

    CRITRESIST(
        1000,
        1000,
        3,
        0,
        500,
        500,
        250,
        0,
        -500,
        0,
        0,
        0,
        0,
        0
    ),

    CCRESIST(
        1000,
        1000,
        1000000,
        1000000,
        500,
        1000,
        0,
        0,
        -500,
        0,
        0,
        0,
        0,
        0
    ),

    PENETRATION(
        900,
        1000,
        2,
        1000,
        450,
        409,
        266,
        0,
        -500,
        0,
        0,
        0,
        0,
        0
    ),

    ATKSPEED(
        2500,
        2400,
        1,
        -733,
        1600,
        500,
        800,
        250,
        -10000,
        0,
        0,
        500,
        1,
        -1500
    ),

    LIFESTEAL(
        1000,
        1000,
        3,
        0,
        500,
        500,
        250,
        0,
        -500,
        0,
        0,
        0,
        0,
        0
    ),

    BLOCK(
        1000,
        1000,
        3,
        0,
        500,
        500,
        250,
        0,
        -500,
        0,
        0,
        0,
        0,
        0
    ),

    BLOCKDEF(
        450,
        775,
        3,
        1500,
        225,
        204,
        179,
        -920,
        -2,
        3,
        -938,
        -1,
        0,
        0
    ),

    MPATK(
        2300,
        2400,
        1,
        -900,
        1200,
        500,
        600,
        0,
        -500,
        0,
        0,
        0,
        0,
        0
    ),
}
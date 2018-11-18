package co.ndan.kotlingrad.math.types.dependent

@JvmName("g1") operator fun <T> Array<T, `1`>.get(i: `0`): T = contents[0]
@JvmName("g2") operator fun <T> Array<T, `2`>.get(i: `0`): T = contents[0]
@JvmName("g2") operator fun <T> Array<T, `2`>.get(i: `1`): T = contents[1]
@JvmName("g3") operator fun <T> Array<T, `3`>.get(i: `0`): T = contents[0]
@JvmName("g3") operator fun <T> Array<T, `3`>.get(i: `1`): T = contents[1]
@JvmName("g3") operator fun <T> Array<T, `3`>.get(i: `2`): T = contents[2]
@JvmName("g4") operator fun <T> Array<T, `4`>.get(i: `0`): T = contents[0]
@JvmName("g4") operator fun <T> Array<T, `4`>.get(i: `1`): T = contents[1]
@JvmName("g4") operator fun <T> Array<T, `4`>.get(i: `2`): T = contents[2]
@JvmName("g4") operator fun <T> Array<T, `4`>.get(i: `3`): T = contents[3]
@JvmName("g5") operator fun <T> Array<T, `5`>.get(i: `0`): T = contents[0]
@JvmName("g5") operator fun <T> Array<T, `5`>.get(i: `1`): T = contents[1]
@JvmName("g5") operator fun <T> Array<T, `5`>.get(i: `2`): T = contents[2]
@JvmName("g5") operator fun <T> Array<T, `5`>.get(i: `3`): T = contents[3]
@JvmName("g3") operator fun <T> Array<T, `5`>.get(i: `4`): T = contents[4]

@JvmName("0pT") operator fun <T> Array<T, `0`>.plus(t: T): Array<T, `1`> = Array(contents + t)
@JvmName("1pT") operator fun <T> Array<T, `1`>.plus(t: T): Array<T, `2`> = Array(contents + t)
@JvmName("2pT") operator fun <T> Array<T, `2`>.plus(t: T): Array<T, `3`> = Array(contents + t)
@JvmName("3pT") operator fun <T> Array<T, `3`>.plus(t: T): Array<T, `4`> = Array(contents + t)
@JvmName("4pT") operator fun <T> Array<T, `4`>.plus(t: T): Array<T, `5`> = Array(contents + t)
@JvmName("5pT") operator fun <T> Array<T, `5`>.plus(t: T): Array<T, `6`> = Array(contents + t)

@JvmName("0p0") operator fun <T> Array<T, `0`>.plus(l: Array<T, `0`>): Array<T, `0`> = l
@JvmName("0p1") operator fun <T> Array<T, `0`>.plus(l: Array<T, `1`>): Array<T, `1`> = l
@JvmName("0p2") operator fun <T> Array<T, `0`>.plus(l: Array<T, `2`>): Array<T, `2`> = l
@JvmName("0p3") operator fun <T> Array<T, `0`>.plus(l: Array<T, `3`>): Array<T, `3`> = l
@JvmName("1p0") operator fun <T> Array<T, `1`>.plus(l: Array<T, `0`>): Array<T, `1`> = this
@JvmName("2p0") operator fun <T> Array<T, `2`>.plus(l: Array<T, `0`>): Array<T, `2`> = this
@JvmName("3p0") operator fun <T> Array<T, `3`>.plus(l: Array<T, `0`>): Array<T, `3`> = this

@JvmName("1p1") operator fun <T> Array<T, `1`>.plus(l: Array<T, `1`>): Array<T, `2`> = Array(contents + l.contents)
@JvmName("1p2") operator fun <T> Array<T, `1`>.plus(l: Array<T, `2`>): Array<T, `3`> = Array(contents + l.contents)
@JvmName("1p3") operator fun <T> Array<T, `1`>.plus(l: Array<T, `3`>): Array<T, `4`> = Array(contents + l.contents)
@JvmName("2p1") operator fun <T> Array<T, `2`>.plus(l: Array<T, `1`>): Array<T, `3`> = Array(contents + l.contents)
@JvmName("3p1") operator fun <T> Array<T, `3`>.plus(l: Array<T, `1`>): Array<T, `4`> = Array(contents + l.contents)

@JvmName("2p2") operator fun <T> Array<T, `2`>.plus(l: Array<T, `2`>): Array<T, `4`> = Array(contents + l.contents)
@JvmName("2p3") operator fun <T> Array<T, `2`>.plus(l: Array<T, `3`>): Array<T, `5`> = Array(contents + l.contents)
@JvmName("3p2") operator fun <T> Array<T, `3`>.plus(l: Array<T, `2`>): Array<T, `5`> = Array(contents + l.contents)
@JvmName("3p3") operator fun <T> Array<T, `3`>.plus(l: Array<T, `3`>): Array<T, `6`> = Array(contents + l.contents)

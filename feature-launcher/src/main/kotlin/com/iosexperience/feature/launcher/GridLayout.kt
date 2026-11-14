package com.iosexperience.feature.launcher

/** Lógica pura (testável sem Android): pagina a lista de apps conforme linhas × colunas. */
data class GridConfig(val rows: Int = 6, val columns: Int = 4) {
    init { require(rows in 3..8 && columns in 3..6) { "Grade fora do intervalo suportado" } }
    val pageSize get() = rows * columns
}

fun <T> List<T>.paginate(config: GridConfig): List<List<T>> = chunked(config.pageSize)

/** Move item de [from] para [to] (base do arrastar e soltar). */
fun <T> List<T>.moved(from: Int, to: Int): List<T> {
    require(from in indices && to in indices)
    return toMutableList().apply { add(to, removeAt(from)) }
}

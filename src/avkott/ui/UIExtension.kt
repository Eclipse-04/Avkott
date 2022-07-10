package avkott.ui

import arc.scene.ui.layout.Cell
import arc.scene.ui.layout.Table

//thx Liplum
inline fun Table.addT(fill:Table.()->Unit): Cell<Table> =
    this.add(Table().apply(fill))

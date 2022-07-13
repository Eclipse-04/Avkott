package avkott.ui

import arc.scene.ui.layout.Cell
import arc.scene.ui.layout.Collapser
import arc.scene.ui.layout.Table
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

//thx Liplum
inline fun Table.addT(fill: Table.() -> Unit): Cell<Table> =
    this.add(Table().apply(fill))

inline fun Table.collapser(
    animated: Boolean = true,
    crossinline content: Table.() -> Unit,
): ReadWriteProperty<Any?, Boolean> {
    var collapesd = true
    val collapser = Collapser({ it.content() }, false)
    collapser.setCollapsed(animated) { collapesd }
    add(collapser).grow()
    return object : ReadWriteProperty<Any?, Boolean> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean =
            collapesd

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
            collapesd = value
        }
    }
}
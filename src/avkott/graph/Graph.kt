package avkott.graph

import arc.struct.Queue
import arc.struct.Seq
import mindustry.gen.Building

class Graph {
    companion object {
        private val queue = Queue<Building>()
        private val outArray1 = Seq<Building>()
        private val outArray2 = Seq<Building>()
    }
}
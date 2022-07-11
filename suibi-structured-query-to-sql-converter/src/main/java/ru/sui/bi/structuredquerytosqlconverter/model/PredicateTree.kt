package ru.sui.bi.structuredquerytosqlconverter.model

class PredicateTree<T>(
    val not: Boolean? = null,
    val predicate: Predicate,
    val nodes: List<Node<T>>
) {

    enum class Predicate {
        AND, OR
    }

    sealed class Node<T> {
        class SubTree<T>(val tree: PredicateTree<T>) : Node<T>()
        class Value<T>(val value: T) : Node<T>()
    }

    fun values(): Iterator<T> {
        return iterator {
            this@PredicateTree.nodes.forEach {
                when (it) {
                    is Node.SubTree -> yieldAll(it.tree.values())
                    is Node.Value -> yield(it.value)
                }
            }
        }
    }

}



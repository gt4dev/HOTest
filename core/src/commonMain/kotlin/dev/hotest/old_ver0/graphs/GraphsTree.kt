package dev.hotest.old_ver0.graphs

internal class GraphTreeNode<T>(
    val value: T,
    val parent: GraphTreeNode<T>? = null
) {
    val children = mutableListOf<GraphTreeNode<T>>()

    /**
     * Adds one child node under this node and returns it.
     *
     * Example:
     * `root.addChild("A")` creates tree `root -> A`.
     */
    fun addChild(value: T): GraphTreeNode<T> {
        val child = GraphTreeNode(
            value = value,
            parent = this
        )
        children.add(child)
        return child
    }

    fun childAt(index: Int): GraphTreeNode<T>? = children.getOrNull(index)

    fun clearChildren() {
        children.clear()
    }
}
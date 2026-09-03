package dev.hotest.old_ver0.variants

import dev.hotest.old_ver0.graphs.GraphTreeNode

internal class VariantsTree {

    private val rootNode = GraphTreeNode<VariantsTreeNodeData>(VariantsTreeNodeData.Root)

    internal fun clear() {
        rootNode.clearChildren()
    }

    internal fun createNavigator(): VariantsTreeNavigator = VariantsTreeNavigator(this)

    internal fun hasPendingLeaves(): Boolean =
        rootNode.children.any { groupHasPendingLeaves(VariantsGroupNode(it)) }

    internal fun findOrCreateVariant(
        group: VariantsGroupNode,
        index: Int,
        comment: String
    ): VariantOptionNode {
        val optionNode = group.node.childAt(index)
            ?: group.node.addChild(
                VariantsTreeNodeData.VariantOption(
                    index = index,
                    comment = comment
                )
            )
        return VariantOptionNode(optionNode)
    }

    internal fun finishGroup(group: VariantsGroupNode, variantCount: Int) {
        group.node.groupData().variantCount = variantCount
    }

    internal fun markAsBranch(option: VariantOptionNode) {
        option.node.optionData().isLeaf = false
    }

    internal fun markLeafExecuted(option: VariantOptionNode) {
        val data = option.node.optionData()
        data.isLeaf = true
        data.leafExecuted = true
    }

    internal fun optionHasPendingLeaves(option: VariantOptionNode): Boolean {
        val data = option.node.optionData()
        return when (data.isLeaf) {
            true -> !data.leafExecuted
            false -> option.node.children.any { groupHasPendingLeaves(VariantsGroupNode(it)) }
            null -> true
        }
    }

    private fun groupHasPendingLeaves(group: VariantsGroupNode): Boolean {
        val data = group.node.groupData()
        if (data.variantCount == 0) {
            return false
        }

        return group.node.children.any { optionHasPendingLeaves(VariantOptionNode(it)) }
    }

    private fun findOrCreateGroup(
        container: GraphTreeNode<VariantsTreeNodeData>,
        groupIndex: Int,
        comment: String
    ): VariantsGroupNode {
        val groupNode = container.childAt(groupIndex)
            ?: container.addChild(
                VariantsTreeNodeData.VariantsGroup(
                    comment = comment
                )
            )
        return VariantsGroupNode(groupNode)
    }

    internal inner class VariantsTreeNavigator internal constructor(
        private val tree: VariantsTree
    ) {
        private val containerStack = mutableListOf(ContainerCursor(tree.rootNode))

        internal fun startRun() {
            containerStack.clear()
            containerStack.add(ContainerCursor(tree.rootNode))
        }

        internal fun enterGroup(comment: String): VariantsGroupNode {
            val parentCursor = containerStack.last()
            val groupIndex = parentCursor.nextChildGroupIndex
            parentCursor.nextChildGroupIndex += 1
            return tree.findOrCreateGroup(
                container = parentCursor.container,
                groupIndex = groupIndex,
                comment = comment
            )
        }

        internal fun enterVariant(option: VariantOptionNode) {
            containerStack.add(ContainerCursor(option.node))
        }

        internal fun exitVariant() {
            containerStack.removeAt(containerStack.lastIndex)
        }
    }
}

internal class VariantsGroupNode internal constructor(
    internal val node: GraphTreeNode<VariantsTreeNodeData>
)

internal class VariantOptionNode internal constructor(
    internal val node: GraphTreeNode<VariantsTreeNodeData>
)

internal sealed interface VariantsTreeNodeData {
    data object Root : VariantsTreeNodeData

    data class VariantsGroup(
        val comment: String,
        var variantCount: Int = -1
    ) : VariantsTreeNodeData

    data class VariantOption(
        val index: Int,
        val comment: String,
        var isLeaf: Boolean? = null,
        var leafExecuted: Boolean = false
    ) : VariantsTreeNodeData
}

private class ContainerCursor(
    val container: GraphTreeNode<VariantsTreeNodeData>,
    var nextChildGroupIndex: Int = 0
)

private fun GraphTreeNode<VariantsTreeNodeData>.groupData(): VariantsTreeNodeData.VariantsGroup =
    value as? VariantsTreeNodeData.VariantsGroup
        ?: error("Expected variants group node")

private fun GraphTreeNode<VariantsTreeNodeData>.optionData(): VariantsTreeNodeData.VariantOption =
    value as? VariantsTreeNodeData.VariantOption
        ?: error("Expected variant option node")

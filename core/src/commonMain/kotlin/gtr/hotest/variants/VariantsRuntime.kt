package gtr.hotest.variants

internal class VariantsRuntime {

    private val tree = VariantsTree()
    private val navigator = tree.createNavigator()
    private val groupStack = mutableListOf<GroupContext>()
    private val variantStack = mutableListOf<RunningVariantContext>()
    private var pendingRuns = true
    private var leafExecutedInCurrentRun = false

    fun reset() {
        tree.clear()
        pendingRuns = true
        leafExecutedInCurrentRun = false
        groupStack.clear()
        variantStack.clear()
    }

    internal fun hasPendingRuns(): Boolean = pendingRuns

    fun startRun() {
        leafExecutedInCurrentRun = false
        groupStack.clear()
        variantStack.clear()
        navigator.startRun()
    }

    fun finishRun() {
        pendingRuns = tree.hasPendingLeaves()
    }

    fun enterGroup(comment: String): GroupContext {
        variantStack.lastOrNull()?.encounteredChildGroup = true
        val group = navigator.enterGroup(comment)
        val ctx = GroupContext(group = group)
        groupStack.add(ctx)
        return ctx
    }

    internal fun currentGroup(): GroupContext? = groupStack.lastOrNull()

    fun exitGroup(): GroupContext = groupStack.removeAt(groupStack.lastIndex)

    fun registerVariant(comment: String): VariantContext? {
        val ctx = currentGroup() ?: return null
        val index = ctx.variantCount
        ctx.variantCount += 1

        val option = tree.findOrCreateVariant(
            group = ctx.group,
            index = index,
            comment = comment
        )

        val shouldExecute =
            !leafExecutedInCurrentRun &&
                !ctx.variantSelected &&
                tree.optionHasPendingLeaves(option)

        if (shouldExecute) {
            ctx.variantSelected = true
        }

        return VariantContext(
            option = option,
            shouldExecute = shouldExecute
        )
    }

    fun enterVariant(option: VariantOptionNode) {
        variantStack.add(RunningVariantContext(option))
        navigator.enterVariant(option)
    }

    fun exitVariant() {
        val ctx = variantStack.removeAt(variantStack.lastIndex)
        navigator.exitVariant()

        if (ctx.encounteredChildGroup) {
            tree.markAsBranch(ctx.option)
            return
        }

        tree.markLeafExecuted(ctx.option)
        leafExecutedInCurrentRun = true
    }

    fun finishGroup(ctx: GroupContext) {
        tree.finishGroup(ctx.group, ctx.variantCount)
    }
}

internal class GroupContext(
    val group: VariantsGroupNode,
    var variantCount: Int = 0,
    var variantSelected: Boolean = false
)

internal class VariantContext(
    val option: VariantOptionNode,
    val shouldExecute: Boolean
)

internal class RunningVariantContext(
    val option: VariantOptionNode,
    var encounteredChildGroup: Boolean = false
)

package burlton

class Graph
{
    private val hmNodeToChildren = mutableMapOf<String, MutableList<String>>()

    fun addNode(node: String)
    {
        hmNodeToChildren[node] = mutableListOf()
    }

    fun addLink(parent: String, child: String): Boolean
    {
        //Check we're not creating a cycle
        if (relationshipExists(child, parent))
        {
            return false
        }

        if (!hmNodeToChildren.containsKey(parent))
        {
            addNode(parent)
        }

        hmNodeToChildren[parent]!!.add(child)

        if (!hmNodeToChildren.containsKey(child))
        {
            addNode(child)
        }

        return true
    }

    fun getAllChildren(parent: String): MutableSet<String>
    {
        val children = getChildren(parent) ?: return mutableSetOf()

        val allChildren = mutableSetOf<String>()
        children.forEach{
            allChildren.add(it)
            allChildren.addAll(getAllChildren(it))
        }

        return allChildren
    }


    fun relationshipExists(parent: String, child: String): Boolean
    {
        val list = hmNodeToChildren[parent] ?: return false

        return list.contains(child)
    }

    fun getChildren(node: String, depth: Int = 1): MutableList<String>?
    {
        val children = hmNodeToChildren[node] ?: return null
        if (depth == 0)
        {
            return mutableListOf(node)
        }

        val childrenAtRightDepth = mutableListOf<String>()
        children.forEach{
            val subChildren = getChildren(it, depth-1)!!
            childrenAtRightDepth.addAll(subChildren)
        }

        return childrenAtRightDepth
    }

    fun contains(node: String): Boolean
    {
        return hmNodeToChildren.containsKey(node)
    }

    fun getDepth(node: String): Int
    {
        if (!hmNodeToChildren.contains(node))
        {
            return -1
        }

        return 1
    }

    fun size(): Int = hmNodeToChildren.size
}
package burlton

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GraphTest
{
    @Test
    fun testEmptyGraph()
    {
        val graph = Graph()

        assertThat(graph.size(), equalTo(0))
    }

    @Test
    fun testAddNode()
    {
        val graph = Graph()

        graph.addNode("Test")

        assertTrue(graph.contains("Test"))
        assertFalse(graph.contains("Test2"))
    }

    @Test
    fun testUnlinkedNode()
    {
        val graph = Graph()

        graph.addNode("Test")

        assertNotNull(graph.getChildren("Test"))
        assertTrue(graph.getChildren("Test")!!.isEmpty())
    }

    @Test
    fun testLinkingNode()
    {
        val graph = Graph()

        graph.addNode("A")
        graph.addLink("A", "B")

        assertTrue(graph.getChildren("A")!!.contains("B"))
    }

    @Test
    fun testChildrenSizeConsistency()
    {
        val graph = Graph()
        graph.addNode("Parent")

        graph.addLink("Parent", "Child")

        assertThat(graph.size(), equalTo(2))
    }

    @Test
    fun testAddingNewParent()
    {
        val graph = Graph()

        graph.addLink("Parent", "Child")

        assertTrue(graph.contains("Parent"))
        assertTrue(graph.contains("Child"))
    }

    @Test
    fun testNoCycles()
    {
        val graph = Graph()
        graph.addNode("Parent")

        assertTrue(graph.addLink("Parent", "Child"))
        assertFalse(graph.addLink("Child", "Parent"))

        assertFalse(graph.relationshipExists("Child", "Parent"))
    }

    @Test
    fun testNestedChildren()
    {
        val graph = constructGraphWithThreeLinkedNodes()

        assertTrue(graph.relationshipExists("Grandparent", "Parent"))
        assertTrue(graph.relationshipExists("Parent", "Child"))
        assertThat(graph.size(), equalTo(3))
    }

    @Test
    fun testGettingAllChildren()
    {
        val graph = constructGraphWithThreeLinkedNodes()

        val allChildren = graph.getAllChildren("Grandparent")

        assertThat(allChildren.size, equalTo(2))
        assertTrue(allChildren.contains("Parent"))
        assertTrue(allChildren.contains("Child"))
    }

    @Test
    fun testGettingAllChildrenHasNoDuplicates()
    {
        val graph = constructGraphWithThreeLinkedNodes()
        graph.addLink("Grandparent", "Mother")
        graph.addLink("Mother", "Child")

        val allChildren = graph.getAllChildren("Grandparent")

        assertThat(allChildren.size, equalTo(3))
        assertTrue(allChildren.contains("Mother"))
        assertTrue(allChildren.contains("Parent"))
        assertTrue(allChildren.contains("Child"))
    }

    private fun constructGraphWithThreeLinkedNodes(): Graph
    {
        val graph = Graph()
        graph.addNode("Grandparent")

        graph.addLink("Grandparent", "Parent")
        graph.addLink("Parent", "Child")

        return graph
    }

    @Test
    fun testGettingChildrenAtSpecificDepth()
    {
        val graph = constructGraphWithThreeLinkedNodes()

        val topLevelChildern = graph.getChildren("Grandparent", 1)!!
        val secondLevelChildren = graph.getChildren("Grandparent", 2)!!
        val thirdLevelChildren = graph.getChildren("Grandparent", 3)!!

        assertThat(topLevelChildern.size, equalTo(1))
        assertTrue(topLevelChildern.contains("Parent"))
        assertThat(secondLevelChildren.size, equalTo(1))
        assertTrue(secondLevelChildren.contains("Child"))
        assertTrue(thirdLevelChildren.isEmpty())
    }

    @Test
    fun testNoDuplicateChildren()
    {
        val graph = constructGraphWithThreeLinkedNodes()
        val result = graph.addLink("Parent", "Child")
        val children = graph.getChildren("Parent")!!

        assertFalse(result)
        assertThat(children.size, equalTo(1))
    }

    @Test
    fun testNoSingleCycles()
    {
        val graph = Graph()
        graph.addNode("A")
        val result = graph.addLink("A", "A")
        val children = graph.getChildren("A")!!

        assertFalse(result)
        assertTrue(children.isEmpty())
    }

    @Test
    fun testNoLongCycles()
    {
        val graph = Graph()

        graph.addLink("A", "B")
        graph.addLink("B", "C")
        val result = graph.addLink("C", "A")


        assertFalse(result)
    }

    @Test
    fun testFindingNodeDepth()
    {
        val graph = constructGraphWithThreeLinkedNodes()

        val grandParentDepth = graph.getDepth("Grandparent")
        val parentDepth = graph.getDepth("Parent")
        val childDepth = graph.getDepth("Child")

        val miscDepth = graph.getDepth("Rubbish")

        assertThat(grandParentDepth, equalTo(2))
        assertThat(parentDepth, equalTo(1))
        assertThat(childDepth, equalTo(0))
        assertThat(miscDepth, equalTo(-1))
    }

    @Test
    fun testMultiThreadedCycle()
    {
        val graph = Graph()

        for (i in 1..100)
        {
            val r1 = Runnable{graph.addLink("A$i", "B$i")}
            val r2 = Runnable{graph.addLink("B$i", "A$i")}

            val t1 = Thread(r1)
            val t2 = Thread(r2)

            t1.start()
            t2.start()

            t1.join()
            t2.join()

            assertFalse(graph.relationshipExists("A$i", "B$i") && graph.relationshipExists("B$i", "A$i"))
        }
    }
}
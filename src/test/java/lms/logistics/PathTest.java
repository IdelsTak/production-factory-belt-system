package lms.logistics;

import lms.logistics.belts.Belt;
import lms.logistics.container.Producer;
import lms.logistics.container.Receiver;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Before;
import org.junit.Test;

public class PathTest {

    private Path path;
    private Belt node;
    private Path previous;
    private Path next;

    @Before
    public void setup() {
        Item key = new Item("something");
        previous = new Path(new Producer(1, key));
        next = new Path(new Receiver(2, key));
        node = new Belt(3);
        path = new Path(node, previous, next);
    }

    @Test
    public void testHead() {
        assertEquals(previous, path.head());
    }

    @Test
    public void testGetNode() {
        assertEquals(node, path.getNode());
    }

    @Test
    public void testTail() {
        assertEquals(next, path.tail());
    }

    @Test
    public void testGetPrevious() {
        assertEquals(previous, path.getPrevious());
    }

    @Test
    public void testSetPrevious() {
        path.setPrevious(next);
        assertNotEquals(previous, path.getPrevious());
    }

    @Test
    public void testGetNext() {
        assertEquals(next, path.getNext());
    }

    @Test
    public void testSetNext() {
        path.setNext(previous);
        assertNotEquals(next, path.getNext());
    }

    @Test
    public void testEquals() {
        Item key = new Item("something");
        previous = new Path(new Producer(1, key));
        next = new Path(new Receiver(2, key));
        node = new Belt(3);
        Path simplePath = new Path(node, previous, next);
        assertEquals(path, simplePath);
    }

    @Test
    public void testToString() {
        assertEquals("START -> <Producer-1> -> END", path.toString());
    }

}

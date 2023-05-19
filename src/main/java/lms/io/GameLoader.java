package lms.io;

import java.io.*;
import java.util.*;
import lms.exceptions.FileFormatException;
import lms.grid.*;
import lms.logistics.Item;
import lms.logistics.Transport;
import lms.logistics.belts.Belt;
import lms.logistics.container.Producer;
import lms.logistics.container.Receiver;

/**
 * This class is responsible for loading (reading and parsing) a text file
 * containing details required for the creation of a simulated factory
 * represented in the form of a graphical hexagonal grid. The factory consists
 * of hexagonal nodes (as seen in a beehive) which are linked together to form a
 * complete and symmetrical grid. Each node within this grid provides a
 * depiction of one or more simulated production line(s) nodes. A production
 * line consists of one Producer, one or more Receiver(s) and numerous connected
 * nodes, called Belts.
 * <p>
 * The Producer nodes produce Items while the Receiver Nodes consume them. In
 * between each pair (or more) of a Producer and Receiver, are conveyor belt
 * nodes. Each belt node transports the Items produced by the Producer towards
 * the direction of the connected Receiver(s). Each production line, can have
 * one or more Producer, and one or more Receiver.
 * <p>
 * If multiple Receivers are on a production line, then one or more splitter
 * nodes would be required to distribute the direction of the connected Belts
 * from the Producer(s) to the Receiver(s).
 * <p>
 * For example (Where p is a Producer, c is a Receiver, --- are belts and *
 * represents a splitter):
 * <p>
 * <p>
 * p----*-----c \____c
 * <p>
 * <p>
 * <p>
 * In the text file, each hexagonal grid node is encoded to represent a specific
 * type of node.
 * <p>
 * The available hexagonal grid types are: Encoding Example Encoding Function
 * "o" Empty white hexagon, without any connections "w" A black unusable hexagon
 * (a wall) "r" A Receiver node where belts lead towards it from a Producer "p"
 * A Producer node, which produce Items that lead down the belts to the
 * Receivers "b" A Belt node, which connects Producers to Receivers and moves
 * items "s" (For CSSE7023 only) A Splitter node, which acts as a belt, except
 * it has one input and 2 outputs
 * <p>
 * The text file starts by providing an integer value which will be stored as
 * the range. The range value is responsible for deciding the size of the
 * hexagonal grid. The size of a grid will be equal to range * 2 + 1.
 * <p>
 * There is then a line of 5 underscores to indicate the end of a section. This
 * line may also serve as a commenting line: Any text occuring after the
 * underscores but on the same line should be ignored while reading the file.
 * <p>
 * The second section of data provides two integer values on two rows, depicting
 * the number of producers, followed by the number of Receivers, respectively.
 * <p>
 * It is followed by a line of underscores to indicate the end of a section, and
 * optionally, a comment on the same line, as described above.
 * <p>
 * The third and fourth sections provides Item keys for the producers, and Item
 * keys for Receivers, respectively. All paths must have at least one producer
 * and at least one Receiver.
 * <p>
 * There is a line of underscores after each of the third and fourth sections,
 * and optionally, a comment on the same line, as described above.
 * <p>
 * The fifth section provides the distributed encoding data for each node in the
 * grid, as seen in each row in turn of the hexagonal grid.
 * <p>
 * Example encoded grid:
 * <p>
 * <p>
 * w w w w
 * w b b b b
 * b b p w w b
 * b w b w b b o
 * b p b w r w
 * w o b b r
 * w w w w
 * <p>
 * <p>
 * Note that leading spaces are irrelevant, and are included in our examples
 * because they increase readability of the hexagon.
 * <p>
 * When you parse a grid, Producers, Receivers and Belts given numbers
 * representing their positional location. For example, in the grid shown above:
 * <p>
 * Example output: Annotated screenshot
 * <p>
 * For example, in a smaller grid:
 * <p>
 * <p>
 * w o
 * p b r
 * o w
 * <p>
 * <p>
 * the following numbers would be assigned:
 * <p>
 * <p>
 * x x
 * 1 2 3
 * x x
 * <p>
 * <p>
 * The hexagon grid is followed by a line of underscores to indicate the end of
 * a section, and optionally, a comment, as described above.
 * <p>
 * The final section provides specific linking data for each hexagonal node's
 * neighbouring connections, which aids in creating the belt pathways. Each line
 * of the file contains the current, previous and next node links data, with the
 * format depending on the type of node:
 * <p>
 * For a belt: The first value contains the node's positional location in the
 * grid as an integer, followed by its previously connected (if it is connected)
 * node depicted by a minus sign. If the current node has a link forward, it
 * will follow after a comma. For example, 2-1,3 would indicate that Belt-2
 * accepts input from node 1 (whatever it is) and outputs to node 3 (whatever it
 * is).
 * <p>
 * For a producer: The first value contains the node's positional location in
 * the grid as an integer, followed by its next connected node depicted by a
 * minus sign. Producers cannot have a previous link. For example, 1-2 would
 * indicate that Producer-1 outputs to node 2 (whatever it is)
 * <p>
 * For a Receiver: The first value contains the node's positional location in
 * the grid as an integer, followed by its previously connected node depicted by
 * a minus sign. Receivers cannot have a link forward. For example, 1-2 would
 * indicate that Receiver-1 accepts input from node 2 (whatever it is).
 * <p>
 * If a connection is already defined or known, then the text file may omit the
 * previously connected node in the list to indicate this. For example, if we
 * have a grid with Producer-1, Belt-2 and Receiver-3, it could be described in
 * several ways:
 * <p>
 * Option 1: Show Belt-2's prior connection to Producer-1 and forward connection
 * to Receiver-3. This means Producer-1 and Receiver-3 don't need unique lines,
 * as their connections have already been established:
 * <p>
 * <p>
 * 2-1,3
 * <p>
 * <p>
 * Option 2: Show Producer-1's connection to Belt-2, and Belt-2's connection to
 * Receiver-3. Note that in defining Belt-2's connections, we indicate that
 * Belt-2's prior connection has already been defined by following the minus
 * sign with a comma immediately:
 * <p>
 * <p>
 * 1-2 2-,3
 * <p>
 * <p>
 * Option 3: Show Receiver-3's prior connection to Belt-2, and Belt-2's prior
 * connection to Producer-1. Note the trailing comma in the line where Belt-2's
 * connections are defined, as this indicates that the forward connection has
 * already been defined:
 * <p>
 * <p>
 * 3-2 2-1,
 * <p>
 * <p>
 * Option 4: Show Producer-1's forward connection to Belt-2, and Receiver-3's
 * prior connection to Belt-2. Note that this sufficiently defines Belt-2's
 * connections, and therefore Belt-2 doesn't need a line:
 * <p>
 * <p>
 * 1-2 3-2
 * <p>
 * <p>
 * This class provides methods to parse the text file and extract the necessary
 * data into the relevant class objects, to create a hexagonal grid with
 * interconnected nodes; depicting one or more simulated production lines in a
 * factory.
 * <p>
 * Within your program, the values of connections are maintained in a Container
 * class, similar to a doubly linked list.
 * <p>
 * The connections between nodes can be used to create belt pathways that
 * connect factories or processing nodes in a hexagonal grid. By following the
 * connections from each node, the pathway through the grid can be determined,
 * enabling the efficient transport of goods or materials.
 * <p>
 * Receiver/s on any given path should have the same item key as the producer on
 * that path.
 */
public class GameLoader {

    /**
     * Constructor for the class.
     */
    public GameLoader() {
    }

    /**
     * The load method provides an access point to load and parse the grid map
     * text file.
     * <p>
     * When reading the input grid:
     * <p>
     * p = insert a Producer at this position of the grid b = insert a Belt node
     * at this position of the grid r = insert a Receiver at this position of
     * the grid s = insert a Splitter at this position of the grid * (CSSE7023
     * students only) o or w = insert a lambda into the grid that returns the
     * appropriate character code
     *
     * @param reader the reader to read from
     * @return the game grid loaded from the reader files
     * @throws IOException if there is an error reading from the reader
     * @throws FileFormatException if the file is not in the correct format, for
     * example:
     * <p>
     * If there is extra or missing information in a section (for example, if
     * the final section refers to nodes that don't exist). If related sections
     * have information that doesn't match up (for example, producer and
     * receiver on the same path having non-matching item keys; or the stated
     * number of producers or receivers not matching the number in the hex grid;
     * or having a different number of item keys than stated
     * producers/receivers). If there are unexpected characters (for example,
     * the hex grid contain characters other than o,w,r,p,b or s; or there are
     * more characters in the hex grid than expected for the current row). If
     * sections are not separated by underscores. If the input is not well
     * formatted (for example, if there are duplicate or missing - or , in a
     * connection line; or an impossible connection is specified, such as an
     * input to a producer). If there is an insufficient number of underscores
     * on a dividing line.
     * @throws NullPointerException if reader is null
     *
     */
    public static GameGrid load(Reader reader) throws IOException,
            FileFormatException {
        if (reader == null) {
            throw new NullPointerException();
        }

        try (BufferedReader in = new BufferedReader(reader)) {
            int range = -1;
            String line;
            int lineCount = 0;
            int sectionCount = 0;
            int numOfProducers = -1;
            int createdProducersCount = 0;
            int numOfReceivers = -1;
            int createdReceiversCount = 0;
            LinkedList<Item> producerItems = new LinkedList<>();
            LinkedList<Item> receiverItems = new LinkedList<>();
            LinkedList<Transport> transports = new LinkedList<>();
            char[][] grid = null;
            int gridLineCount = 0;
            GameGrid gameGrid = null;

            while ((line = in.readLine()) != null) {
                lineCount++;

                if (lineCount == 1) {
                    range = Integer.parseInt(line);
                    continue;
                }

                if (line.contains("_") && countOccurrences(line, '_') != 5) {
                    throw new FileFormatException("unexpected number of underscore lines");
                }

                if (line.contains("_____")) {
                    sectionCount++;
                    continue;
                }

                switch (sectionCount) {
                    case 1 -> {
                        if (numOfProducers == -1 && numOfReceivers == -1) {
                            numOfProducers = Integer.parseInt(line);
                        } else if (numOfProducers != -1 && numOfReceivers == -1) {
                            numOfReceivers = Integer.parseInt(line);
                        }
                    }
                    case 2 -> {
                        if (createdProducersCount < numOfProducers) {
                            createdProducersCount++;
                            producerItems.add(new Item(line));
                        }
                    }
                    case 3 -> {
                        if (createdReceiversCount < numOfReceivers) {
                            createdReceiversCount++;
                            receiverItems.add(new Item(line));
                        }
                    }
                    case 4 -> {
                        if (grid == null) {
                            grid = new char[range * 2 + 1][];
                        }

                        String gridLine = line.trim().replaceAll("\\s", "");
                        grid[gridLineCount] = gridLine.toCharArray();
                        gridLineCount++;
                    }
                    case 5 -> {
                        if (gameGrid == null) {
                            gameGrid = new GameGrid(range);
                            createTransports(grid, transports, producerItems, receiverItems);
                        }

                        parsePathsData(line, transports);
                    }
                }

            }

            if (grid != null && gameGrid != null) {
                setCoordinates(gameGrid, grid, transports);
            }

            return gameGrid;
        } catch (Exception ex) {
            throw new FileFormatException(ex);
        }
    }

    /**
     * Creates transports (belts, producers, and receivers) based on the
     * provided grid data.
     *
     * @param grid the grid data representing the layout of components
     * @param transports the list of transports to populate with created
     * transports
     * @param producerItems the list of producer items to assign to producer
     * transports
     * @param receiverItems the list of receiver items to assign to receiver
     * transports
     */
    private static void createTransports(char[][] grid,
            LinkedList<Transport> transports, LinkedList<Item> producerItems,
            LinkedList<Item> receiverItems) {
        int componentsCount = 1;

        for (char[] row : grid) {
            for (int column = 0; column < row.length; column++) {
                char element = row[column];
                switch (element) {
                    case 'b' -> {
                        Belt belt = new Belt(componentsCount);
                        transports.add(belt);
                        componentsCount++;
                    }
                    case 'p' -> {
                        Item item = producerItems.pop();
                        Producer producer = new Producer(componentsCount, item);
                        transports.add(producer);
                        componentsCount++;
                    }
                    case 'r' -> {
                        Item item = receiverItems.pop();
                        Receiver receiver = new Receiver(componentsCount, item);
                        transports.add(receiver);
                        componentsCount++;
                    }
                    default -> {
                    }
                }
            }
        }
    }

    /**
     * Sets the coordinates for transports and other components in the game grid
     * based on the provided grid data.
     *
     * @param gameGrid the game grid object to set the coordinates on
     * @param grid the grid data representing the layout of components
     * @param transports the list of transports to assign coordinates to
     * @throws FileFormatException if there is an error in the file format or if
     * there are invalid node types in the grid
     */
    private static void setCoordinates(GameGrid gameGrid, char[][] grid,
            LinkedList<Transport> transports) throws FileFormatException {
        Comparator<Coordinate> comparator = Comparator
                .comparingInt(Coordinate::getCordR)
                .thenComparingInt(Coordinate::getCordQ);
        List<Coordinate> sortedCoordinates = gameGrid
                .getGrid()
                .keySet()
                .stream()
                .filter(c -> !c.equals(new Coordinate()))
                .sorted(comparator)
                .toList();
        LinkedList<Coordinate> keys = new LinkedList<>(sortedCoordinates);
        LinkedList<Character> flattenedGrid = new LinkedList<>();

        for (char[] row : grid) {
            for (char element : row) {
                flattenedGrid.add(element);
            }
        }

        if (flattenedGrid.size() != (keys.size() + 1)) {
            throw new FileFormatException("no-hex grid");
        }

        int middleIndex = flattenedGrid.size() / 2;

        for (int i = 0; i < flattenedGrid.size(); i++) {
            Character element = flattenedGrid.get(i);
            Coordinate coordinate = null;

            if (i == middleIndex) {
                coordinate = new Coordinate();
            }

            if (!keys.isEmpty() && coordinate == null) {
                coordinate = keys.pop();
            }

            GridComponent component;

            switch (element) {
                case 'p', 'r', 'b' ->
                    component = transports.pop();
                case 's', 'o', 'w' ->
                    component = () -> String.valueOf(element);
                default ->
                    throw new FileFormatException("Invalid node type: " + element);
            }

            gameGrid.setCoordinate(coordinate, component);
        }

        if (!transports.isEmpty()) {
            String message = "invalid number of characters in the hex grid";
            throw new FileFormatException(message);
        }
    }

    /**
     * Parses a line of paths' data and establishes connections between
     * transports.
     *
     * @param line the line of paths' data to parse
     * @param transports the list of transports to establish connections on
     * @throws FileFormatException if there is an error in the file format or if
     * a transport with the specified ID is not found
     */
    private static void parsePathsData(String line,
            List<Transport> transports) throws FileFormatException {

        try {
            Transport transport;
            String[] parts = line.split("-");
            int id = Integer.parseInt(parts[0]);

            if (line.contains(",")) {
                // Belt
                String[] connections = parts[1].split(",");
                int previousId = connections[0].isEmpty() ? -1
                        : Integer.parseInt(connections[0]);
                int nextId = (connections.length < 2 || connections[1].isEmpty())
                        ? -1
                        : Integer.parseInt(connections[1]);

                transport = findTransport(transports, id);
                if (previousId != -1) {
                    Transport prevTransport = findTransport(transports, previousId);
                    if (!(transport instanceof Producer)) {
                        transport.setInput(prevTransport.getPath());
                    }
                    if (!(prevTransport instanceof Receiver)) {
                        prevTransport.setOutput(transport.getPath());
                    }
                }
                if (nextId != -1) {
                    Transport nextTransport = findTransport(transports, nextId);
                    if (!(transport instanceof Receiver)) {
                        transport.setOutput(nextTransport.getPath());
                    }
                    if (!(nextTransport instanceof Producer)) {
                        nextTransport.setInput(transport.getPath());
                    }
                }
            } else {
                // Producer or Receiver
                int connectedId = -1;
                connectedId = Integer.parseInt(parts[1]);

                if (connectedId != -1) {
                    transport = findTransport(transports, id);
                    Transport connectedTransport = findTransport(transports, connectedId);

                    if (transport instanceof Producer producer) {
                        producer.setOutput(connectedTransport.getPath());
                        if (!(connectedTransport instanceof Producer)) {
                            connectedTransport.setInput(producer.getPath());
                        }
                    } else {
                        transport.setInput(connectedTransport.getPath());
                        if (!(connectedTransport instanceof Receiver)) {
                            connectedTransport.setOutput(transport.getPath());
                        }
                    }
                }
            }
        } catch (NumberFormatException | FileFormatException ex) {
            throw new FileFormatException(ex);
        }
    }

    /**
     * Finds a Transport object with the specified ID from a list of Transports.
     *
     * @param transports the list of Transports to search from
     * @param id the ID of the Transport to find
     * @return the Transport object with the specified ID
     * @throws FileFormatException if the Transport with the specified ID is not
     * found in the list
     */
    private static Transport findTransport(List<Transport> transports, int id) throws
            FileFormatException {
        return transports
                .stream()
                .filter(transport -> transport.getId() == id)
                .findFirst()
                .orElseThrow(FileFormatException::new);
    }

    /**
     * Counts the number of occurrences of a specified character in a given
     * string.
     *
     * @param text The input string in which to count the occurrences.
     * @param character The character to count the occurrences of.
     * @return The number of occurrences of the specified character in the
     * string.
     */
    private static int countOccurrences(String text, char character) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == character) {
                count++;
            }
        }
        return count;
    }

}

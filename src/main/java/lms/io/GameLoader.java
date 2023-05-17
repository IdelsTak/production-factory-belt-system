package lms.io;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
                System.out.printf("line: %s; #: %d%n", line, lineCount);

                if (lineCount == 1) {
                    range = Integer.parseInt(line);
                    System.out.printf("range: %d%n", range);
                    continue;
                }

                if (line.contains("_____")) {
                    sectionCount++;
                    System.out.printf("end of section: %d%n", sectionCount);
                    continue;
                }

                // End of section 1 - now reading section 2
                if (sectionCount == 1) {
                    if (numOfProducers == -1 && numOfReceivers == -1) {
                        numOfProducers = Integer.parseInt(line);
                    } else if (numOfProducers != -1 && numOfReceivers == -1) {
                        numOfReceivers = Integer.parseInt(line);
                    }
                }

                if (sectionCount == 2) {
                    if (createdProducersCount < numOfProducers) {
                        createdProducersCount++;
                        producerItems.add(new Item(line));
                    }
                }

                if (sectionCount == 3) {
                    if (createdReceiversCount < numOfReceivers) {
                        createdReceiversCount++;
                        receiverItems.add(new Item(line));
                    }
                }

                if (sectionCount == 4) {
                    if (grid == null) {
                        grid = new char[range * 2 + 1][];
                    }

                    String gridLine = line.trim().replaceAll("\\s", "");
                    grid[gridLineCount] = gridLine.toCharArray();
                    gridLineCount++;
                }

                if (sectionCount == 5) {
                    if (gameGrid == null) {
                        gameGrid = new GameGrid(range);
                        int componentsCount = 1;

                        for (int i = 0; i < grid.length; i++) {
                            for (int j = 0; j < grid[i].length; j++) {
                                char element = grid[i][j];
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

                    List<Integer> linkingData = new ArrayList<>();
                    String pattern = "\\d+";
                    Pattern regex = Pattern.compile(pattern);
                    Matcher matcher = regex.matcher(line);

                    while (matcher.find()) {
                        String numStr = matcher.group();
                        int num = Integer.parseInt(numStr);
                        linkingData.add(num);
                    }

                    List<Transport> connectedTransports = transports
                            .stream()
                            .filter(t -> linkingData.contains(t.getId()))
                            .toList();

                    Transport firstTransport = connectedTransports
                            .stream()
                            .filter(t -> t.getId() == linkingData.get(0))
                            .findFirst()
                            .orElseThrow();
                    Transport secondTransport = connectedTransports
                            .stream()
                            .filter(t -> t.getId() == linkingData.get(1))
                            .findFirst()
                            .orElseThrow();

                    System.out.println("linking data: " + linkingData);

                    if (connectedTransports.size() == 2) {
                        if (firstTransport instanceof Producer producer) {
                            producer.setOutput(secondTransport.getPath());
                            secondTransport.setInput(producer.getPath());
                        } else if (firstTransport instanceof Receiver receiver) {
                            receiver.setInput(secondTransport.getPath());
                            secondTransport.setOutput(receiver.getPath());
                        } else {
                            if (secondTransport instanceof Producer producer) {
                                firstTransport.setInput(producer.getPath());
                                producer.setOutput(firstTransport.getPath());
                            } else {
                                firstTransport.setOutput(secondTransport.getPath());
                                secondTransport.setInput(firstTransport.getPath());
                            }
                        }
                    } else if (connectedTransports.size() == 3) {
                        Transport thirdTransport = connectedTransports
                                .stream()
                                .filter(t -> t.getId() == linkingData.get(2))
                                .findFirst()
                                .orElseThrow();

                        if (firstTransport instanceof Belt belt) {
                            belt.setInput(secondTransport.getPath());
                            secondTransport.setOutput(belt.getPath());
                            belt.setOutput(thirdTransport.getPath());
                        }

                        if (thirdTransport instanceof Receiver receiver) {
                            receiver.setInput(firstTransport.getPath());
                        }
                    }

                }

            }

            System.out.println("Finished parsing sections");

            if (grid != null && gameGrid != null) {
                for (int row = 0; row < grid.length; row++) {
                    for (int column = 0; column < grid[row].length; column++) {
                        char nodeType = grid[row][column];
                        Coordinate coordinate = getCoordinate(row, column, grid);

                        switch (nodeType) {
                            case 'p', 'r', 'b' ->
                                gameGrid.setCoordinate(coordinate, transports.pop());
                            case 's', 'o', 'w' ->
                                gameGrid.setCoordinate(coordinate, () -> String.valueOf(nodeType));
                            default ->
                                throw new FileFormatException("Invalid node type: " + nodeType);
                        }
                    }
                }
            }
            return gameGrid;
        }
    }

    /**
     * Retrieves the coordinate based on the given row, column, and grid.
     *
     * @param row The row index in the grid.
     * @param column The column index in the grid.
     * @param grid The 2D grid containing the nodes.
     * @return The coordinate corresponding to the given row, column, and grid.
     */
    private static Coordinate getCoordinate(int row, int column, char[][] grid) {
        Coordinate origin = new Coordinate();
        Coordinate coordinate = null;
        Orientation orientation = getOrientation(row, column, grid);

        if (orientation != null) {
            switch (orientation) {
                case TOP_LEFT -> coordinate = origin.getTopLeft();
                case TOP_RIGHT -> coordinate = origin.getTopRight();
                case LEFT -> coordinate = origin.getLeft();
                case RIGHT -> coordinate = origin.getRight();
                case BOTTOM_LEFT -> coordinate = origin.getBottomLeft();
                case BOTTOM_RIGHT -> coordinate = origin.getBottomRight();
            }
        } else {
            coordinate = origin;
        }

        return coordinate;
    }

    /**
     * Retrieves the orientation based on the given row index, column index, and
     * hexagon grid.
     *
     * @param row The row index in the hexagon grid.
     * @param column The column index in the hexagon grid.
     * @param grid The 2D array representing the hexagon grid.
     * @return The orientation of the element at the specified row and column
     * indexes.
     */
    private static Orientation getOrientation(int row, int column, char[][] grid) {
        int middleRow = grid.length / 2;
        int middleColumn = grid[middleRow].length / 2;

        int rowDiff = row - middleRow;
        int columnDiff = column - middleColumn;

        if (rowDiff == 0 && columnDiff == 0) {
            // Element is at the center, no direction
            return null;
        } else if (rowDiff < 0 && columnDiff >= 0) {
            return Orientation.TOP_RIGHT;
        } else if (rowDiff < 0 && columnDiff < 0) {
            return Orientation.TOP_LEFT;
        } else if (rowDiff == 0 && columnDiff < 0) {
            return Orientation.LEFT;
        } else if (rowDiff == 0 && columnDiff > 0) {
            return Orientation.RIGHT;
        } else if (rowDiff > 0 && columnDiff >= 0) {
            return Orientation.BOTTOM_RIGHT;
        } else if (rowDiff > 0 && columnDiff < 0) {
            return Orientation.BOTTOM_LEFT;
        } else {
            return null;
        }
    }

}

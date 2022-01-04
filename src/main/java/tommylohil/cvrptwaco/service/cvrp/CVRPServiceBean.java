package tommylohil.cvrptwaco.service.cvrp;

import org.springframework.stereotype.Service;
import tommylohil.cvrptwaco.dto.response.CVRPUsingACOResponse;
import tommylohil.cvrptwaco.model.Ant;
import tommylohil.cvrptwaco.model.CVRPUsingACOResult;
import tommylohil.cvrptwaco.model.Node;
import tommylohil.cvrptwaco.model.TimeWindow;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CVRPServiceBean implements CVRPService {

    @Override
    public CVRPUsingACOResult calculateCVRPUsingACO(List<Node> nodeList, List<TimeWindow> timeWindowList, Double vehicleCapacity, Long maxNumberOfVehicle) throws Exception {

        // Define parameter for ACO
        Double alpha = 2.0d;
        Double beta = 1.0d;
        Double rho = .05d;

        Double totalDistanceOfAnIteration;
        Integer iterationCounter = 0;
        Integer maxIterationCounter = 100;
        Double optimalMinimumDistance = Double.MAX_VALUE;
        Double[][] euclideanDistanceMatrix = getEuclideanDistanceMatrix(nodeList);
        Double[][] tauMatrix = getTauMatrix(nodeList.size());
        Double[][] etaMatrix = getEtaMatrix(nodeList.size(), euclideanDistanceMatrix);
        LocalTime earliestStartTime = getEarliestStartTime(nodeList, timeWindowList);
        List<Node> route;
        List<Ant> antList;
        CVRPUsingACOResult result = null;

        while (iterationCounter <= maxIterationCounter) {

            System.out.println("----------------------New Iteration-------------------");
            totalDistanceOfAnIteration = 0d;
            resetAllNodeVisited(nodeList);
            antList = new ArrayList<>();

            // Looping to create new ant colony
            while (!isAllNodeVisited(nodeList) && (antList.size() <= maxNumberOfVehicle)) {
                // Assign an artificial ant from depot (0, 0) with maximum capacity
                LinkedList<Node> nodeLinkedList = new LinkedList<>(nodeList);
                nodeLinkedList.remove(0);
                route = new ArrayList<>();
                route.add(nodeList.get(0));
                nodeLinkedList = new LinkedList<>(nodeLinkedList.stream().filter(node -> !node.getVisited()).collect(Collectors.toList()));
                Ant ant = Ant.builder().availableNodeList(nodeLinkedList).route(route).arrivalTimeList(new ArrayList<>()).capacity(vehicleCapacity).currentTime(earliestStartTime).build();
                System.out.println("----------------------New Ant-----------------------");

                while (ant.getCapacity() > 0) {
                    System.out.println("\n\ncapacity = " + ant.getCapacity());
                    List<Node> currentAvailableNodeList = getCurrentAvailableNodeList(ant, timeWindowList, euclideanDistanceMatrix);
                    if (currentAvailableNodeList.size() > 0) {
                        System.out.println(">>> current available node list");
                        System.out.println(currentAvailableNodeList);

                        // Choose node using random proportional rule
                        chooseAndVisitNode(ant, currentAvailableNodeList, timeWindowList, alpha, beta, rho, tauMatrix, etaMatrix, euclideanDistanceMatrix);
                    } else {
                        // Get next node to be visited
                        List<Node> nextNodeToBeVisitedList = getNextAvailableNodeList(ant, timeWindowList, euclideanDistanceMatrix);
                        if (nextNodeToBeVisitedList.size() > 0) {
                            System.out.println(">>> next node to be visited list");
                            System.out.println(nextNodeToBeVisitedList);

                            // Choose node using random proportional rule
                            chooseAndVisitNode(ant, nextNodeToBeVisitedList, timeWindowList, alpha, beta, rho, tauMatrix, etaMatrix, euclideanDistanceMatrix);
                        } else {
                            System.out.println(">>> next node to be visited list not found");

                            Node depotNode = Node.builder().id(0).x(0.0).y(0.0).demand(0.0).timeWindowIndex(-1).visited(true).build();
                            List<Node> routeWithLastDepotNode = ant.getRoute();
                            routeWithLastDepotNode.add(depotNode);
                            ant.setRoute(routeWithLastDepotNode);

                            // Next node not found
                            break;
                        }
                    }
                }

                // Calculate total distance of an ant
                Double totalDistanceOfAnAnt = ant.calculateTotalDistance(euclideanDistanceMatrix);
                totalDistanceOfAnIteration += totalDistanceOfAnAnt;
                antList.add(ant);
                System.out.println("Total distance: " + totalDistanceOfAnAnt);
            }

            if (antList.size() > maxNumberOfVehicle) {
                continue;
            }

            // Get optimal minimum distance
            if (optimalMinimumDistance > totalDistanceOfAnIteration) {
                iterationCounter = 0;
                optimalMinimumDistance = totalDistanceOfAnIteration;

                // TODO: Record all information needed to be output
                result = CVRPUsingACOResult.builder()
                        .antList(antList)
                        .totalDistance(optimalMinimumDistance)
                        .build();
            } else {
                iterationCounter++;
            }

            if (iterationCounter <= maxIterationCounter) {
                // Update pheromone

            }
        }

        // TODO: Output

        return result;
    }

    private Double[][] getEuclideanDistanceMatrix(List<Node> nodeList) {
        int numberOfNodes = nodeList.size();
        Double[][] matrix = new Double[numberOfNodes][numberOfNodes];

        for (int i = 0; i < numberOfNodes; i++) {
            for (int j = 0; j < i; j++) {
                matrix[i][j] = getEuclideanDistance(nodeList.get(i), nodeList.get(j));
                matrix[j][i] = getEuclideanDistance(nodeList.get(i), nodeList.get(j));
            }
        }

        return matrix;
    }

    private Double[][] getTauMatrix(Integer matrixSize) {
        Double[][] matrix = new Double[matrixSize][matrixSize];

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < i; j++) {
                matrix[i][j] = 1.0;
                matrix[j][i] = 1.0;
            }
        }

        return matrix;
    }

    private Double[][] getEtaMatrix(Integer matrixSize, Double[][] euclideanDistanceMatrix) {
        Double[][] matrix = new Double[matrixSize][matrixSize];

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < i; j++) {
                matrix[i][j] = 1 / euclideanDistanceMatrix[i][j];
                matrix[j][i] = matrix[i][j];
            }
        }

        return matrix;
    }

    private Double getEuclideanDistance(Node sourceNode, Node targetNode) {
        return Math.sqrt(Math.pow(sourceNode.getX() - targetNode.getX(), 2) + Math.pow(sourceNode.getY() - targetNode.getY(), 2));
    }

    // Take the earliest start time of all nodes
    private LocalTime getEarliestStartTime(List<Node> nodeList, List<TimeWindow> timeWindowList) {
        return nodeList.stream()
                .skip(1)
                .map(node -> timeWindowList.get(node.getTimeWindowIndex()).getStartTime())
                .min(LocalTime::compareTo).get();
    }

    private List<Node> getCurrentAvailableNodeList(Ant ant, List<TimeWindow> timeWindowList, Double[][] euclideanDistanceMatrix) {
        // Get current time window index
        Integer currentTimeWindowIndex = timeWindowList.stream()
                .filter(timeWindow -> (ant.getCurrentTime().compareTo(timeWindow.getStartTime()) >= 0) && (ant.getCurrentTime().compareTo(timeWindow.getEndTime()) <= 0))
                .findFirst()
                .map(timeWindow -> timeWindow.getId())
                .orElse(-1);

        if (currentTimeWindowIndex == -1) {
            return Collections.emptyList();
        }

        Node currentNode = ant.getRoute().get(ant.getRoute().size() - 1);

        return ant.getAvailableNodeList().stream()
                .filter(node -> node.getTimeWindowIndex() == currentTimeWindowIndex)
                .filter(node -> ant.getCurrentTime().plusMinutes(kmToMinutes(euclideanDistanceMatrix[currentNode.getId()][node.getId()])).compareTo(timeWindowList.get(currentTimeWindowIndex).getEndTime()) <= 0)
                .filter(node -> node.getDemand() <= ant.getCapacity())
                .collect(Collectors.toList());
    }

    private List<Node> getNextAvailableNodeList(Ant ant, List<TimeWindow> timeWindowList, Double[][] euclideanDistanceMatrix) {
        // Get next time window index list
        List<Integer> nextTimeWindowIndexList = timeWindowList.stream()
                .filter(timeWindow -> timeWindow.getStartTime().isAfter(ant.getCurrentTime()))
                .map(timeWindow -> timeWindow.getId())
                .collect(Collectors.toList());

        Node currentNode = ant.getRoute().get(ant.getRoute().size() - 1);

        return ant.getAvailableNodeList().stream()
                .filter(node -> nextTimeWindowIndexList.contains(node.getTimeWindowIndex()))
                .filter(node -> ant.getCurrentTime().plusMinutes(kmToMinutes(euclideanDistanceMatrix[currentNode.getId()][node.getId()])).compareTo(timeWindowList.get(node.getTimeWindowIndex()).getEndTime()) <= 0)
                .filter(node -> node.getDemand() <= ant.getCapacity())
                .collect(Collectors.toList());
    }

    private void chooseAndVisitNode(Ant ant, List<Node> currentAvailableNodeList, List<TimeWindow> timeWindowList, Double alpha, Double beta, Double rho, Double[][] tauMatrix, Double[][] etaMatrix, Double[][] euclideanDistanceMatrix) {
        // Choose node using random proportional rule
        Node chosenNode = new Node();
        LocalTime arrivalTime = null;
        {
            Node currentNode = ant.getRoute().get(ant.getRoute().size() - 1);
            List<Double> tauTimesEtaList = new ArrayList<>();
            Double totalTauTimesEta = 0.0;
            Double tauTimesEta = 0.0;

            for (Node currentAvailableNode : currentAvailableNodeList) {
                // tauTimesEta = τ(i,j)^α * η(i,j)^β
                tauTimesEta = Math.pow(tauMatrix[currentNode.getId()][currentAvailableNode.getId()], alpha) *
                        Math.pow(etaMatrix[currentNode.getId()][currentAvailableNode.getId()], beta);
                tauTimesEtaList.add(tauTimesEta);
                totalTauTimesEta += tauTimesEta;
            }

            // Build probability of each current available node
            List<Double> probability = new ArrayList<>();
            for (int index = 0; index < currentAvailableNodeList.size(); index++) {
                // probability of path x = tauTimesEta of path x / totalTauTimesEta
                probability.add(tauTimesEtaList.get(index) / totalTauTimesEta);
            }

            System.out.println("choose >> currentAvailableNodeList = " + currentAvailableNodeList);

            // Select a node by implementing Roulette Wheel
            Double sumOfProbability = 0.0;
            Double randomNumber = Math.random();
            for (int index = 0; index < currentAvailableNodeList.size(); index++) {
                System.out.println("random number = " + randomNumber);
                sumOfProbability += probability.get(index);
                System.out.println("sumOfProbability = " + sumOfProbability);
                if (randomNumber <= sumOfProbability) {
                    chosenNode = currentAvailableNodeList.get(index);
                    arrivalTime = ant.getCurrentTime().plusMinutes(kmToMinutes(euclideanDistanceMatrix[currentNode.getId()][chosenNode.getId()]));

                    // Set ant current time
                    LocalTime nodeTimeWindowStartTime = timeWindowList.get(currentAvailableNodeList.get(index).getTimeWindowIndex()).getStartTime();
                    if (arrivalTime.isBefore(nodeTimeWindowStartTime)) {
                        System.out.println("start time = " + nodeTimeWindowStartTime);
                        ant.setCurrentTime(nodeTimeWindowStartTime);
                    }

                    break;
                }
            }
        }

        // Visit the chosen node
        System.out.println("chosen node: " + chosenNode);
        ant.visit(chosenNode, arrivalTime);

    }

    private Long kmToMinutes(Double km) {
        return (long) Math.ceil(km * 2);
    }

    private Boolean isAllNodeVisited(List<Node> nodeList) {
        nodeList.stream().forEach(node -> {
            System.out.println("\n----------------------> node " + node.getId() + " visited is " + node.getVisited() + "\n");
        });
        return nodeList.stream().allMatch(node -> node.getVisited() == true);
    }

    private void resetAllNodeVisited(List<Node> nodeList) {
        nodeList.stream().forEach(node -> node.setVisited(false));
        nodeList.get(0).setVisited(true);
    }

}

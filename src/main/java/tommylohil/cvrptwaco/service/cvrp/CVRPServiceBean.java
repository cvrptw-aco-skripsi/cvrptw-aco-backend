package tommylohil.cvrptwaco.service.cvrp;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tommylohil.cvrptwaco.constant.BusinessError;
import tommylohil.cvrptwaco.exception.BaseBusinessException;
import tommylohil.cvrptwaco.model.Ant;
import tommylohil.cvrptwaco.model.CVRPUsingACOResult;
import tommylohil.cvrptwaco.model.Node;
import tommylohil.cvrptwaco.model.TimeWindow;

import java.time.LocalTime;
import java.util.*;
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
        Integer maxIterationCounter = 150;
        Double optimalMinimumDistance = Double.MAX_VALUE;
        Double[][] euclideanDistanceMatrix = getEuclideanDistanceMatrix(nodeList);
        Double[][] tauMatrix = getTauMatrix(nodeList.size());
        Double[][] etaMatrix = getEtaMatrix(nodeList.size(), euclideanDistanceMatrix);
        LocalTime earliestStartTime = getEarliestStartTime(nodeList, timeWindowList);
        List<Node> route;
        List<Ant> antList;
        CVRPUsingACOResult result = null;
        while (iterationCounter <= maxIterationCounter) {
            totalDistanceOfAnIteration = 0d;
            resetAllNodeVisited(nodeList);
            antList = new ArrayList<>();
            while (!isAllNodeVisited(nodeList) && (antList.size() < maxNumberOfVehicle)) {
                LinkedList<Node> nodeLinkedList = new LinkedList<>(nodeList);
                nodeLinkedList.remove(0);
                route = new ArrayList<>();
                route.add(nodeList.get(0));
                nodeLinkedList = new LinkedList<>(nodeLinkedList.stream().filter(node -> !node.getVisited()).collect(Collectors.toList()));
                Ant ant = Ant.builder().availableNodeList(nodeLinkedList).route(route).arrivalTimeList(new ArrayList<>()).capacity(vehicleCapacity).currentTime(earliestStartTime).build();
                while (ant.getCapacity() > 0) {
                    List<Node> currentAvailableNodeList = getCurrentAvailableNodeList(ant, timeWindowList, euclideanDistanceMatrix);
                    if (currentAvailableNodeList.size() > 0) {

                        chooseAndVisitNode(ant, currentAvailableNodeList, timeWindowList, alpha, beta, rho, tauMatrix, etaMatrix, euclideanDistanceMatrix);
                    } else {
                        List<Node> nextNodeToBeVisitedList = getNextAvailableNodeList(ant, timeWindowList, euclideanDistanceMatrix);
                        if (nextNodeToBeVisitedList.size() > 0) {

                            chooseAndVisitNode(ant, nextNodeToBeVisitedList, timeWindowList, alpha, beta, rho, tauMatrix, etaMatrix, euclideanDistanceMatrix);
                        } else {
                            ant.setRoute(routeWithLastDepotNode(ant));
                            break;
                        }
                    }
                }
                Node lastNode = ant.getRoute().get(ant.getRoute().size() - 1);
                if ((lastNode.getX() != 0) || (lastNode.getY() != 0)) {
                    ant.setRoute(routeWithLastDepotNode(ant));
                }
                Double totalDistanceOfAnAnt = ant.calculateTotalDistance(euclideanDistanceMatrix);
                totalDistanceOfAnIteration += totalDistanceOfAnAnt;
                antList.add(ant);
            }
            if (optimalMinimumDistance > totalDistanceOfAnIteration) {
                iterationCounter = 0;
                optimalMinimumDistance = totalDistanceOfAnIteration;
                result = CVRPUsingACOResult.builder()
                        .antList(antList)
                        .totalDistance(optimalMinimumDistance)
                        .build();
            } else {
                iterationCounter++;
            }
            if (iterationCounter <= maxIterationCounter) {
                tauMatrix = updatePheromone(tauMatrix, etaMatrix, euclideanDistanceMatrix, nodeList.size(), rho, antList);
            }
        }
        if (!isAllNodeVisited(nodeList)) {
            throw new BaseBusinessException(HttpStatus.BAD_REQUEST, "solution", BusinessError.NOT_FOUND);
        }
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

            // Select a node by implementing Roulette Wheel
            Double sumOfProbability = 0.0;
            Double randomNumber = Math.random();
            for (int index = 0; index < currentAvailableNodeList.size(); index++) {
                sumOfProbability += probability.get(index);
                if (randomNumber <= sumOfProbability) {
                    chosenNode = currentAvailableNodeList.get(index);
                    arrivalTime = ant.getCurrentTime().plusMinutes(kmToMinutes(euclideanDistanceMatrix[currentNode.getId()][chosenNode.getId()]));

                    // Set ant current time
                    LocalTime nodeTimeWindowStartTime = timeWindowList.get(currentAvailableNodeList.get(index).getTimeWindowIndex()).getStartTime();
                    if (arrivalTime.isBefore(nodeTimeWindowStartTime)) {
                        ant.setCurrentTime(nodeTimeWindowStartTime);
                    }

                    break;
                }
            }
        }

        // Visit the chosen node
        ant.visit(chosenNode, arrivalTime);
    }

    private Long kmToMinutes(Double km) {
        return (long) Math.ceil(km * 2);
    }

    private Boolean isAllNodeVisited(List<Node> nodeList) {
        return nodeList.stream().allMatch(node -> node.getVisited() == true);
    }

    private void resetAllNodeVisited(List<Node> nodeList) {
        nodeList.stream().forEach(node -> node.setVisited(false));
        nodeList.get(0).setVisited(true);
    }

    private Double[][] updatePheromone(Double[][] tauMatrix, Double[][] etaMatrix, Double[][] euclideanDistanceMatrix, Integer matrixSize, Double rho, List<Ant> antList) {
        // Get Δτ(i,j) of all ant
        Double[][] deltaTau = new Double[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < i; j++) {
                deltaTau[i][j] = 0d;
                deltaTau[j][i] = 0d;
            }
        }
        for (Ant ant: antList) {
            Double totalDistance = ant.calculateTotalDistance(euclideanDistanceMatrix);
            for (int i=1; i<ant.getRoute().size(); i++) {
                Integer sourceId = ant.getRoute().get(i-1).getId();
                Integer targetId = ant.getRoute().get(i).getId();

                deltaTau[sourceId][targetId] += 1 / totalDistance;
                deltaTau[targetId][sourceId] += 1 / totalDistance;
            }
        }

        // Update pheromone matrix
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < i; j++) {
                // τ(i,j) = (1-ρ) * τ(i,j) + (iteration: k=1 to m)Σ (Δτ(i,j) of k)
                tauMatrix[i][j] *= (1 - rho);
                tauMatrix[j][i] *= (1 - rho);

                tauMatrix[i][j] += deltaTau[i][j];
                tauMatrix[j][i] += deltaTau[j][i];
            }
        }

        return tauMatrix;
    }

    // return new routes
    private List<Node> routeWithLastDepotNode(Ant ant) {
        Node depotNode = Node.builder().id(0).x(0.0).y(0.0).demand(0.0).timeWindowIndex(-1).visited(true).build();
        List<Node> route = ant.getRoute();
        route.add(depotNode);
        return route;
    }
}

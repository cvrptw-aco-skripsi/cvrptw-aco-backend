package tommylohil.cvrptwaco.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
public class Ant {

    private LinkedList<Node> availableNodeList;
    private List<Node> route;
    private List<LocalTime> arrivalTimeList;
    private Double capacity;
    private LocalTime currentTime;

    public Ant(LinkedList<Node> availableNodeList, List<Node> route, List<LocalTime> arrivalTimeList, Double capacity, LocalTime currentTime) {
        this.availableNodeList = availableNodeList;
        this.route = route;
        this.arrivalTimeList = arrivalTimeList;
        this.capacity = capacity;
        this.currentTime = currentTime;
    }

    public void visit(Node node, LocalTime arrivalTime) {
        this.availableNodeList.remove(node);
        this.route.add(node);
        this.arrivalTimeList.add(arrivalTime);
        this.capacity -= node.getDemand();
        this.currentTime = this.currentTime.plusMinutes(10);
        node.setVisited(true);
    }

    //    public Double calculateTotalDistance(Double[][] euclideanDistanceMatrix) {
    public Double calculateTotalDistance(Double[][] euclideanDistanceMatrix) {
        Double totalDistance = 0d;
        for (int i=1; i<this.route.size(); i++) {
            Integer sourceId = this.route.get(i-1).getId();
            Integer targetId = this.route.get(i).getId();
            if (!Objects.equals(sourceId, targetId)) {
                totalDistance += euclideanDistanceMatrix[this.route.get(i).getId()][this.route.get(i-1).getId()];
            }
        }

        return totalDistance;
    }
}

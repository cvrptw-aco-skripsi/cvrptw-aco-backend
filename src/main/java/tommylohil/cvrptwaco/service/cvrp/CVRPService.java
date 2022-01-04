package tommylohil.cvrptwaco.service.cvrp;

import tommylohil.cvrptwaco.model.CVRPUsingACOResult;
import tommylohil.cvrptwaco.model.Node;
import tommylohil.cvrptwaco.model.TimeWindow;

import java.util.List;

public interface CVRPService {

    CVRPUsingACOResult calculateCVRPUsingACO(List<Node> nodeList, List<TimeWindow> timeWindowList, Double vehicleCapacity, Long maxNumberOfVehicle) throws Exception;

}

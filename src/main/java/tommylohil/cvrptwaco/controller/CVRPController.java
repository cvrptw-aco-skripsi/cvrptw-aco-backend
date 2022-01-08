package tommylohil.cvrptwaco.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tommylohil.cvrptwaco.base.BaseMethod;
import tommylohil.cvrptwaco.base.BaseResponse;
import tommylohil.cvrptwaco.controller.path.CVRPControllerPath;
import tommylohil.cvrptwaco.dto.request.CVRPUsingACORequest;
import tommylohil.cvrptwaco.dto.response.CVRPUsingACOResponse;
import tommylohil.cvrptwaco.exception.BaseBusinessException;
import tommylohil.cvrptwaco.model.CVRPUsingACOResult;
import tommylohil.cvrptwaco.model.Node;
import tommylohil.cvrptwaco.model.TimeWindow;
import tommylohil.cvrptwaco.service.cvrp.CVRPService;

import javax.validation.Valid;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(CVRPControllerPath.BASE_PATH)
public class CVRPController {

    final CVRPService cvrpService;

    public CVRPController(CVRPService cvrpService) {
        this.cvrpService = cvrpService;
    }

    @PostMapping(CVRPControllerPath.CALCULATE_CVRP_USING_ACO)
    public ResponseEntity<?> calculateCVRPUsingACO(@Valid @RequestBody CVRPUsingACORequest request) throws JSONException {
        try {

            List<Node> nodeList = new ArrayList<>();
            List<TimeWindow> timeWindowList = new ArrayList<>();

            // Build nodeList
            Node newNode = Node.builder().id(0).x(0.0).y(0.0).demand(0.0).timeWindowIndex(-1).visited(true).build();
            nodeList.add(newNode);
            Integer nodeIndex = 1;
            for (CVRPUsingACORequest.NodeRequest nodeRequest : request.getNodes()) {
                newNode = Node.builder().id(nodeIndex).visited(false).build();
                BeanUtils.copyProperties(nodeRequest, newNode);
                nodeIndex++;
                nodeList.add(newNode);
            }

            // Build timeWindowList
            Integer timeWindowIndex = 0;
            for (CVRPUsingACORequest.TimeWindowRequest timeWindowRequest : request.getTimeWindows()) {
                TimeWindow newTimeWindow = TimeWindow.builder().id(timeWindowIndex).build();
                BeanUtils.copyProperties(timeWindowRequest, newTimeWindow);
                timeWindowIndex++;
                timeWindowList.add(newTimeWindow);
            }

            // Run ACO Algorithm
            CVRPUsingACOResult result = cvrpService.calculateCVRPUsingACO(nodeList, timeWindowList, request.getVehicleCapacity(), request.getMaxNumberOfVehicle());


            return new ResponseEntity<>(
                    new BaseResponse<>(
                            HttpStatus.OK.value(),
                            HttpStatus.OK.getReasonPhrase(),
                            toCVRPUsingACOResponse(result),
                            null
                    ),
                    HttpStatus.OK
            );
        } catch (BaseBusinessException exception) {
            return BaseMethod.handleBusinessException(exception);
        } catch (Exception exception) {
            return BaseMethod.handleException(exception);
        }
    }

    private CVRPUsingACOResponse toCVRPUsingACOResponse(CVRPUsingACOResult cvrpUsingACOResult) {
        List<CVRPUsingACOResponse.Vehicle> vehicleList = cvrpUsingACOResult.getAntList().stream()
                .map(ant -> CVRPUsingACOResponse.Vehicle.builder()
                        .nodes(ant.getRoute())
                        .arrivalTimes(ant.getArrivalTimeList())
                        .build()
                )
                .collect(Collectors.toList());

        return CVRPUsingACOResponse.builder()
                .numberOfUsedVehicle(cvrpUsingACOResult.getAntList().size())
                .totalDistance(cvrpUsingACOResult.getTotalDistance())
                .vehicles(vehicleList)
                .build();
    }

    public static class Vehicle {

        private List<Node> nodes;
        private List<LocalTime> arrivalTimes;
    }
}

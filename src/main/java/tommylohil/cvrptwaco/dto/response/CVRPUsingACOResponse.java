package tommylohil.cvrptwaco.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tommylohil.cvrptwaco.model.Node;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVRPUsingACOResponse {

    private Integer numberOfUsedVehicle;
    private Double totalDistance;
    private List<Vehicle> vehicles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Vehicle {

        private List<Node> nodes;
        private List<LocalTime> arrivalTimes;
    }
}

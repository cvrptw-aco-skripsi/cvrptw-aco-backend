package tommylohil.cvrptwaco.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tommylohil.cvrptwaco.constant.ValidationError;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVRPUsingACORequest {

    @NotNull(message = ValidationError.REQUIRED)
    @Valid
    private List<TimeWindowRequest> timeWindows;

    @NotNull(message = ValidationError.REQUIRED)
    @Valid
    private List<NodeRequest> nodes;

    @NotNull(message = ValidationError.REQUIRED)
    private Double vehicleCapacity;

    @NotNull(message = ValidationError.REQUIRED)
    private Long maxNumberOfVehicle;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeWindowRequest {

        @NotNull(message = ValidationError.REQUIRED)
        private LocalTime startTime;

        @NotNull(message = ValidationError.REQUIRED)
        private LocalTime endTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeRequest {

        @NotNull(message = ValidationError.REQUIRED)
        private double x;

        @NotNull(message = ValidationError.REQUIRED)
        private double y;

        @NotNull(message = ValidationError.REQUIRED)
        private Integer timeWindowIndex;

        @NotNull(message = ValidationError.REQUIRED)
        private Double demand;
    }
}

package tommylohil.cvrptwaco.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVRPUsingACOResult {

    private List<Ant> antList;
    private Double totalDistance;
}

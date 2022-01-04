package tommylohil.cvrptwaco.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Node {

    private Integer id;
    private Double x;
    private Double y;
    private Integer timeWindowIndex;
    private Double demand;
    private Boolean visited;
}

package tommylohil.cvrptwaco.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tommylohil.cvrptwaco.constant.ValidationError;

import javax.validation.constraints.NotNull;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeWindow {

    private Integer id;
    private LocalTime startTime;
    private LocalTime endTime;
}

package tommylohil.cvrptwaco.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseBusinessException extends Exception {

    private HttpStatus httpStatus;
    private String errorField;
    private String errorMessage;
}

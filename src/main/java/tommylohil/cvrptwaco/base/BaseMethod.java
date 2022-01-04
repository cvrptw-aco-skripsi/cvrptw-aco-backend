package tommylohil.cvrptwaco.base;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tommylohil.cvrptwaco.exception.BaseBusinessException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BaseMethod {

    public static ResponseEntity<?> handleBusinessException(BaseBusinessException exception) {

        HashMap<String, List<String>> errorMap = new HashMap<>();
        errorMap.put(exception.getErrorField(), new ArrayList<>());
        return new ResponseEntity<>(
                new BaseResponse<>(
                        exception.getHttpStatus().value(),
                        exception.getHttpStatus().getReasonPhrase(),
                        null,
                        errorMap
                ),
                exception.getHttpStatus()
        );
    }

    public static ResponseEntity<?> handleException(Exception exception) {

        HashMap<String, List<String>> errorMap = new HashMap<>();
        String errorMessage = ExceptionUtils.getStackTrace(exception);
        errorMap.put("error", List.of(errorMessage));
        return new ResponseEntity<>(
                new BaseResponse<>(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        null,
                        errorMap
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}

package tommylohil.cvrptwaco.exception;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import tommylohil.cvrptwaco.base.BaseResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ControllerAdvice
public class ErrorHandlingControllerAdvice {

    /** Handle Request Body Validation */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public final ResponseEntity<Object> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) throws JSONException {

        HashMap<String, List<String>> errorMap = new HashMap<>();
        String fieldErrorKey, fieldErrorMessage;

        // Fill hashmap with key and array error message
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrorKey = fieldError.getField();
            fieldErrorMessage = fieldError.getDefaultMessage();

            if (!errorMap.containsKey(fieldErrorKey)) {
                errorMap.put(fieldErrorKey, new ArrayList<String>());
            }
            errorMap.get(fieldErrorKey).add(fieldErrorMessage);
        }

        return new ResponseEntity<>(
                new BaseResponse<>(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        null,
                        errorMap
                ),
                HttpStatus.BAD_REQUEST
        );
    }
}

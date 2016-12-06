package carloancalculator;

import com.google.gson.Gson;

/**
 *
 * @author Ben
 */
public class ErrorResponse extends Response {
    public Boolean error = true;
    public String code = "0";
    public String message = "failed";
    
    public ErrorResponse(String code, String message){
        super(200);
        
        this.code = code;
        this.message = message;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carloancalculator;

import com.google.gson.Gson;

/**
 *
 * @author Ben
 */
public class ErrorResponse extends Response {
    public Details error = new Details();
    
    public class Details {
        public String code = "Error";
        public String message = "Message to send to client describing error";        
    }

    
    public ErrorResponse(String code, String message){
        super(500);
        
        error.code = code;
        error.message = message;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carloancalculator;

/**
 *
 * @author Ben
 */
public class ResultResponse extends Response {
    public double result = 0;
    
    public ResultResponse(double result){
        super(200);
        
        this.result = result;
        
    }
   
}

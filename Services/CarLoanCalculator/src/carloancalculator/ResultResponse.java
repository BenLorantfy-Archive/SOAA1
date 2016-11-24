package carloancalculator;

/**
 *
 * @author Ben
 */
public class ResultResponse extends Response {
    public double MonthlyPaymentFor36MonthLoan = 0;
    public double MonthlyPaymentFor48MonthLoan = 0;
    public double MonthlyPaymentFor60MonthLoan = 0;
    
    public ResultResponse(double payment36, double payment48, double payment60){
        super(200);
        
        MonthlyPaymentFor36MonthLoan = payment36;
        MonthlyPaymentFor48MonthLoan = payment48;
        MonthlyPaymentFor60MonthLoan = payment60;
        
    }
   
}

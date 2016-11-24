package carloancalculator;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.google.gson.Gson;

/**
 *
 * @author Ben
 */
public class CarLoanCalculator {
    private static HttpServer server = null;
    private static int port = 1337;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        
        try{
            System.out.println("Creating Http Server...");
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new Handler());
            server.setExecutor(null); // creates a default executor
            server.start();
            System.out.println("Created Http Server on port " + port + "...");
        }catch(Exception e){
            System.out.println("Failed to create HTTP Server: " + e.getMessage());
        }
        
        // Attach event on application quit
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                System.out.println("Stopping http server...");
                if(server != null){
                     server.stop(0);
                }
            }

        });
    }
    
    public static boolean match(String path, HttpExchange t){
        String[] parts = path.split(" ");
        String wantedVerb = parts[0];
        String wantedPath = parts[1];
        
        if(!wantedVerb.equals(t.getRequestMethod())){
            return false;
        }
        
        return wantedPath.trim().equalsIgnoreCase(t.getRequestURI().getPath());
    }
    
    static class Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) {
            Response response = null;
            String body = "";
            
            // [ Try to Read the HTTP request body ]
            try{
                // http://stackoverflow.com/a/10910032/3006989
                InputStreamReader isr =  new InputStreamReader(t.getRequestBody(),"utf-8");
                BufferedReader br = new BufferedReader(isr);

                int b;
                StringBuilder buf = new StringBuilder(512);
                while ((b = br.read()) != -1) {
                    buf.append((char) b);
                }

                br.close();
                isr.close();   
                
                body = buf.toString();
            }catch(Exception e){
                // do nothing and leave body blank
            }
   
    
            try{
                // System.out.println(t.getRequestMethod() + " " + t.getRequestURI().getPath());
            
                if(match("POST /calculations",t)){  
                    // [ Try to parse json ]
                    Request request = null;
                    try{
                        request = new Gson().fromJson(body, Request.class);          
                    }catch(Exception e){
                        new ErrorResponse("Malformed JSON",e.getMessage()).send(t);
                        return;
                    }       
                    
                    if(request == null){
                        new ErrorResponse("Malformed JSON","Malformed JSON").send(t);
                        return;                     
                    }
                    
                    // [ Do calculation ]
                    double r = request.InterestRate / 1200;
                    double P = request.PrincipalAmount;
                    double payment36 = ( r + ( r / ( Math.pow(1 + r,36) - 1 ) )) * P;
                    double payment48 = ( r + ( r / ( Math.pow(1 + r,48) - 1 ) )) * P;
                    double payment60 = ( r + ( r / ( Math.pow(1 + r,60) - 1 ) )) * P;
                    response = new ResultResponse(payment36,payment48,payment60);
                }else{
                    response = new ErrorResponse("404","Page Not Found");                  
                }               
            }catch(Exception e){
                response = new ErrorResponse("Exception",e.getMessage());           
            }

            response.send(t);

        }
        
    }
    
}

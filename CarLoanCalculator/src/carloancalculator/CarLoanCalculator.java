
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carloancalculator;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.google.gson.Gson;

/**
 *
 * @author Ben
 */
public class CarLoanCalculator {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        HttpServer server = null;
        try{
            System.out.println("Creating Http Server...");
            server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/", new Handler());
            server.setExecutor(null); // creates a default executor
            server.start();
            System.out.println("Created Http Server on port 8000...");
        }catch(Exception e){
            System.out.println("Failed to create HTTP Server: " + e.getMessage());
        }
        

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
            
            try{
                // System.out.println(t.getRequestMethod() + " " + t.getRequestURI().getPath());
            
                if(match("GET /calculation",t)){   
                    response = new ResultResponse(100 * Math.random());
                }else{
                    response = new ErrorResponse("404","404");                  
                }               
            }catch(Exception e){
                response = new ErrorResponse("Exception",e.getMessage());           
            }

            response.send(t);

        }
        
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carloancalculator;

import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;

import com.google.gson.Gson;

/**
 *
 * @author Ben
 */
abstract public class Response {
    // http://stackoverflow.com/a/5889590/3006989
    private transient int code = 200;
    
    public Response(int code){
        this.code = code;
    }
    
    public String toJson(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
    
    public void send(HttpExchange t){
         try{
            String json = this.toJson();
            t.sendResponseHeaders(code, json.length());
            OutputStream os = t.getResponseBody();
            os.write(json.getBytes());
            os.close();                  
        }catch(Exception e){
            System.out.println("IO Failed: " + e.getMessage());
        }       
    }
}

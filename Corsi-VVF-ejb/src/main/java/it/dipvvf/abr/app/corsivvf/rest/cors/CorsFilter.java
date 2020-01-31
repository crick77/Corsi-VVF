/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.rest.cors;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * Aggiunge gli header per evitare errori CORS
 * 
 * @author riccardo.iovenitti
 */
@Provider
public class CorsFilter implements ContainerResponseFilter { 
    public CorsFilter() {
        super();
        System.out.println("CORS Filter started.");
    }
    
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
          responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
          responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
          responseContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, device-id");
          responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }    
}

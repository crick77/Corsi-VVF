/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.rest.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import it.dipvvf.abr.app.corsivvf.ejb.MiscServices;
import java.io.IOException;
import javax.annotation.Priority;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.Priorities;
import javax.ws.rs.core.Response;

/**
 *
 * @author riccardo.iovenitti
 */
@Provider
@JWTSecurityCheck
@Priority(Priorities.AUTHENTICATION)
@Stateless
public class JWtSecurityFilter implements ContainerRequestFilter {
    private final static int TOKEN_START = "Bearer".length();
    @Inject
    MiscServices msb;    
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String token = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        System.out.printf("Controllo token [%s] per servizio [%s]", token, requestContext.getUriInfo().getPath());
        if(token!=null) {
            token = token.substring(TOKEN_START).trim();
            
            DecodedJWT jwt = msb.decodeToken(token);            
            if(jwt!=null) {
                System.out.printf("Token decodificato: %s", jwt.getIssuer());
                return;
            }
            else {
                System.out.printf("Impossibile decodificare il token [%s]", token);
            }
        }
        
        System.out.println("Autorizzazione non presente. Restituzione 401.");
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }
}

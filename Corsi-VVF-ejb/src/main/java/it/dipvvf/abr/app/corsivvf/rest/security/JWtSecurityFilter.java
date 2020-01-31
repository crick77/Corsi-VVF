/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.rest.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import it.dipvvf.abr.app.corsivvf.ejb.MiscServices;
import it.dipvvf.abr.app.corsivvf.ejb.SessionStorage;
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
    @Inject
    MiscServices msb;    
    @Inject
    SessionStorage ss;
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String token = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        System.out.printf("Controllo token [%s] per servizio [%s]", token, requestContext.getUriInfo().getPath());
        if(token!=null) {
            token = token.replace(MiscServices.BEARER, "").trim();
            
            DecodedJWT jwt = msb.decodeToken(token);            
            if(jwt!=null) {
                System.out.printf("Token decodificato: %s", jwt.getIssuer());
                if(ss.isValid(token)) {
                    System.out.println("Il token risulta valido e in sessione.");
                    return;
                }
                else {
                    System.out.println("ATTENZIONE: Il token non è più in sessione.");
                }
            }
            else {
                System.out.printf("Impossibile decodificare il token [%s]", token);
            }
        }
        
        System.out.println("Autorizzazione non presente/valida. Restituzione 401.");
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }
}

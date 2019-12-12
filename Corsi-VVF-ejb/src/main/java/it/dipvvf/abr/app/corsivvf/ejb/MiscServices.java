/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.ejb;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import it.dipvvf.abr.app.corsivvf.model.Documento;
import java.util.Date;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@LocalBean
public class MiscServices {
    @PersistenceContext
    EntityManager em;
    private static final String SECRET = "trytodecryptthisbastard!!";
    public static final long NO_EXPIRE = -1;

    public String createToken(String id, long expiration) {
        JWTCreator.Builder token = JWT.create().withSubject(id);
        if(expiration<=NO_EXPIRE) {
            token = token.withExpiresAt(new Date(System.currentTimeMillis() + expiration));
        }
        return token.sign(Algorithm.HMAC512(SECRET.getBytes()));
    }

    public long toMillis(int days, int hours, int minutes) {
        return (days*3600)+(hours*60)+minutes;
    }
   
    /** 
     * to be removed - servlet upload
     * 
     * @param doc 
     */
    public void saveDocument(Documento doc) {        
        em.persist(doc);
        em.flush();
    }
}

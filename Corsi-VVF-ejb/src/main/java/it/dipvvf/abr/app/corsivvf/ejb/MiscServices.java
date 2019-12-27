/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.ejb;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import it.dipvvf.abr.app.corsivvf.model.Documento;
import java.rmi.server.UID;
import java.util.Date;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
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
    private static final String ISSUER = "CorsiVVF.dipvvf.it";
    public static final long NO_EXPIRE = -1;

    /**
     * 
     * @param id
     * @param expiration
     * @return 
     */
    public String createToken(String id, long expiration) {
        JWTCreator.Builder token = JWT.create().withSubject(id).withIssuer(ISSUER);
        if (expiration <= NO_EXPIRE) {
            token = token.withExpiresAt(new Date(System.currentTimeMillis() + expiration));
        }
        return token.sign(Algorithm.HMAC512(SECRET.getBytes()));
    }

    /**
     * 
     * @param token
     * @return 
     */
    public DecodedJWT decodeToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC512(SECRET.getBytes()))
                    .withIssuer(ISSUER)
                    .build(); //Reusable verifier instance
            return verifier.verify(token);
        } catch (JWTVerificationException jwte) {
            return null;
        }
    }

    /**
     * 
     * @param days
     * @param hours
     * @param minutes
     * @return 
     */
    public long toMillis(int days, int hours, int minutes) {
        return (days * 3600) + (hours * 60) + minutes;
    }

    /**
     * 
     * @return 
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String generateUID() {
        return UUID.randomUUID().toString().toUpperCase();
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

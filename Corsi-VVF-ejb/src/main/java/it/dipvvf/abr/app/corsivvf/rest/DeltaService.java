/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.rest;

import it.dipvvf.abr.app.corsivvf.ejb.BaseService;
import it.dipvvf.abr.app.corsivvf.ejb.MiscServices;
import it.dipvvf.abr.app.corsivvf.model.Categoria;
import it.dipvvf.abr.app.corsivvf.model.Delta;
import it.dipvvf.abr.app.corsivvf.model.DeltaConst;
import it.dipvvf.abr.app.corsivvf.model.Documento;
import it.dipvvf.abr.app.corsivvf.model.Installazione;
import it.dipvvf.abr.app.corsivvf.model.Sincronizzazione;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Servizi dedicati al dispositivo
 *
 * @author ospite
 */
@Stateless
@Path("deltas")
@Produces(MediaType.APPLICATION_JSON)
public class DeltaService extends BaseService {

    @PersistenceContext
    EntityManager em;
    @Inject
    MiscServices ms;

    /**
     *
     * @param devId
     * @param devToken
     * @param uriInfo
     * @return
     */
    @GET
    public Response getPendingStates(@HeaderParam("Device-Id") String devId, @HeaderParam("Device-Token") String devToken, @Context UriInfo uriInfo) {
        if (devId == null || devToken == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        //DecodedJWT token = ms.decodeToken(devToken);
        //if (token == null) 
        //    return Response.status(Response.Status.UNAUTHORIZED).build();
        List<Integer> lPending = em.createQuery("SELECT d.id FROM Delta d INNER JOIN d.idSincronizzazione s INNER JOIN s.idInstallazione i INNER JOIN i.idDispositivo dev WHERE s.stato = :status AND dev.token = :token AND dev.deviceid = :devid ORDER BY s.dataora ASC", Integer.class)
                .setParameter("status", DeltaConst.Status.PENDING.toString())
                .setParameter("token", devToken)
                .setParameter("devid", devId)
                .setMaxResults(1)
                .getResultList();

        return Response.ok(resourcesToURI(uriInfo, lPending)).build();
    }

    /**
     *
     * @param devId
     * @param devToken
     * @param uriInfo
     * @return
     */
    @GET
    @Path("count")
    public Response getPendingStateCount(@HeaderParam("Device-Id") String devId, @HeaderParam("Device-Token") String devToken, @Context UriInfo uriInfo) {
        if (devId == null || devToken == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        //DecodedJWT token = ms.decodeToken(devToken);
        //if (token == null) 
        //    return Response.status(Response.Status.UNAUTHORIZED).build();
        
        long pendingCount = em.createQuery("SELECT COUNT(d.id) FROM Delta d INNER JOIN d.idSincronizzazione s INNER JOIN s.idInstallazione i INNER JOIN i.idDispositivo dev WHERE s.stato = :status AND dev.token = :token AND dev.deviceid = :devid", Long.class)
                .setParameter("status", DeltaConst.Status.PENDING.toString())
                .setParameter("token", devToken)
                .setParameter("devid", devId)
                .getSingleResult();

        JsonObject countObj = Json.createObjectBuilder()
                .add("count", pendingCount)
                .build();

        return Response.ok(countObj).build();
    }

    /**
     *
     * @param idDelta
     * @param devId
     * @param devToken
     * @return
     */
    @GET
    @Path("{iddelta: \\d+}")
    public Response getPendingStateDetail(@PathParam("iddelta") int idDelta, @HeaderParam("Device-Id") String devId, @HeaderParam("Device-Token") String devToken) {
        try {
            if (devId == null || devToken == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            //DecodedJWT token = ms.decodeToken(devToken);
            //if (token == null) 
            //    return Response.status(Response.Status.UNAUTHORIZED).build();
            
            Delta delta = em.createQuery("SELECT d FROM Delta d JOIN d.idSincronizzazione s JOIN s.idInstallazione i JOIN i.idDispositivo dev WHERE s.stato = :status AND dev.deviceid = :devId AND dev.token = :token AND d.id = :iddelta", Delta.class)
                    .setParameter("status", DeltaConst.Status.PENDING.toString())
                    .setParameter("devId", devId)
                    .setParameter("token", devToken)
                    .setParameter("iddelta", idDelta)
                    .getSingleResult();

            return Response.ok(delta).build();
        } catch (NoResultException nre) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * *******************************************************
     *
     * SERVIZI DELTA
     *
     *********************************************************
     */
    
    /**
     *
     * @param cat
     * @param operation
     */
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void enqueueCategoryDelta(Categoria cat, DeltaConst.Operation operation) {
        // add new deltas to installable devices
        List<Installazione> lInst = em.createQuery("SELECT i FROM Installazione i WHERE i.idCorso = :corso", Installazione.class)
                .setParameter("corso", cat.getIdCorso())
                .getResultList();

        int counter = 0;
        Date syncDate = new Date();
        for (Installazione inst : lInst) {
            // inserisce una nuova sincronizzazione
            Sincronizzazione sinc = new Sincronizzazione();
            sinc.setDataora(syncDate);
            sinc.setIdInstallazione(inst);
            sinc.setStato(DeltaConst.Status.PENDING.toString());
            em.persist(sinc);

            Delta d = new Delta();
            d.setRisorsa(cat.getNome());
            d.setIdSincronizzazione(sinc);
            d.setOperazione(operation.toString());
            d.setTipologia(DeltaConst.ResourceType.CATEGORY.toString());
            d.setOrdine(counter++);
            d.setStato(DeltaConst.Status.PENDING.toString());
            d.setDimensione(-1);
            d.setTipoRisorsaPadre(DeltaConst.ResourceType.COURSE.toString());
            d.setUidRisorsaPadre(cat.getIdCorso().getUidRisorsa());
            d.setUidRisorsa(cat.getUidRisorsa());

            em.persist(d);
        }
    }

    /**
     *
     * @param doc
     * @param operation
     */
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void enqueueDocumentDelta(Documento doc, DeltaConst.Operation operation) {
        // add new deltas to installable devices        
        String sql = "SELECT i FROM Installazione i WHERE i.";
        Object id;
        if (doc.getIdCorso() != null) {
            sql += "idCorso = :id";
            id = doc.getIdCorso();
        } else {
            sql += "idCategoria = :id";
            id = doc.getIdCategoria();
        }
        List<Installazione> lInst = em.createQuery(sql, Installazione.class)
                .setParameter("id", id)
                .getResultList();

        int counter = 0;
        Date dataSync = new Date();
        for (Installazione inst : lInst) {
            Sincronizzazione sinc = new Sincronizzazione();
            sinc.setDataora(dataSync);
            sinc.setIdInstallazione(inst);
            sinc.setStato(DeltaConst.Status.PENDING.toString());
            em.persist(sinc);

            Delta d = new Delta();
            d.setRisorsa(doc.getNomefile());
            d.setIdSincronizzazione(sinc);
            d.setOperazione(operation.toString());
            d.setTipologia(DeltaConst.ResourceType.DOCUMENT.toString());
            d.setOrdine(counter++);
            d.setStato(DeltaConst.Status.PENDING.toString());
            d.setDimensione(doc.getDimensione());
            d.setMd5(doc.getChecksum());
            if (doc.getIdCorso() != null) {
                d.setUidRisorsaPadre(doc.getIdCorso().getUidRisorsa());
                d.setTipoRisorsaPadre(DeltaConst.ResourceType.COURSE.toString());
            } else {
                d.setUidRisorsaPadre(doc.getIdCategoria().getUidRisorsa());
                d.setTipoRisorsaPadre(DeltaConst.ResourceType.CATEGORY.toString());
            }
            d.setUidRisorsa(doc.getUidRisorsa());

            em.persist(d);
        }
    }
}

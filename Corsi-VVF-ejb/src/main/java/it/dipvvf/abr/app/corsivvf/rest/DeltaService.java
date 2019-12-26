/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.rest;

import it.dipvvf.abr.app.corsivvf.ejb.BaseService;
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
@Path("pending")
@Produces(MediaType.APPLICATION_JSON)
public class DeltaService extends BaseService {
    @PersistenceContext
    EntityManager em;
    
    /**
     *
     * @param devUid
     * @param uriInfo
     * @return
     */
    @GET
    public Response getPendingStates(@HeaderParam("Device-uid") int devUid, @Context UriInfo uriInfo) {
        List<Integer> lPending = em.createQuery("SELECT s.id FROM Sincronizzazione s JOIN Dispositivo d WHERE s.stato = :status AND d.deviceid = :devUid ORDER BY s.dataora ASC", Integer.class)
                .setParameter("status", DeltaConst.Status.PENDING.toString())
                .setParameter("devUid", devUid)
                .getResultList();

        return (lPending.isEmpty()) ? Response.noContent().build() : Response.ok(resourcesToURI(uriInfo, lPending)).build();
    }

    /**
     *
     * @param idInst
     * @param devUid
     * @return
     */
    @GET
    @Path("{idinst: \\d+}")
    public Response getPendingStateDetail(@PathParam("idinst") int idInst, @HeaderParam("Device-uid") int devUid) {
        try {
            Sincronizzazione sinc = em.createQuery("SELECT s FROM Sincronizzazione s JOIN Dispositivo d WHERE s.stato = :status AND d.deviceid = :devUid and s.id = :idinst", Sincronizzazione.class)
                    .setParameter("status", DeltaConst.Status.PENDING.toString())
                    .setParameter("devUid", devUid)
                    .setParameter("idinsta", idInst)
                    .getSingleResult();
            
            return Response.ok(sinc).build();
        }
        catch(NoResultException nre) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     *
     * @param idInst
     * @param devUid
     * @param uriInfo
     * @return
     */
    @GET
    @Path("{idinst: \\d+}/deltas")
    public Response getPendingStateDeltas(@PathParam("idinst") int idInst, @HeaderParam("Device-uid") int devUid, @Context UriInfo uriInfo) {
        try {
            Sincronizzazione sinc = em.createQuery("SELECT s FROM Sincronizzazione s JOIN Dispositivo d WHERE s.stato = :status AND d.deviceid = :devUid and s.id = :idinst", Sincronizzazione.class)
                    .setParameter("status", DeltaConst.Status.PENDING.toString())
                    .setParameter("devUid", devUid)
                    .setParameter("idinsta", idInst)
                    .getSingleResult();
            
            List<Integer> lDelta = em.createQuery("SELECT d.id FROM Delta d WHERE d.idSincronizzazione = :idsinc ORDER BY d.ordine", Integer.class)
                                .setParameter("idsinc", sinc)
                                .getResultList();
            
            return Response.ok(resourcesToURI(uriInfo, lDelta)).build();
        }
        catch(NoResultException nre) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    
    
    /**
     * 
     * @param idInst
     * @param idDelta
     * @param devUid
     * @return 
     */
    @GET
    @Path("{idinst: \\d+}/deltas/{iddelta: \\d+}")
    public Response getPendingStateDeltaDetail(@PathParam("idinst") int idInst, @PathParam("iddelta") int idDelta, @HeaderParam("Device-uid") int devUid) {
        try {
            String sql = "SELECT d FROM Delta d JOIN Sincronizzazione s JOIN Dispositivo d WHERE s.stato = :status AND d.deviceid = :devUid AND s.id = :idinst AND d.id = iddelta";
            
            Delta delta = em.createQuery(sql, Delta.class)
                    .setParameter("status", DeltaConst.Status.PENDING.toString())
                    .setParameter("devUid", devUid)
                    .setParameter("idinsta", idInst)
                    .setParameter("iddelta", idDelta)
                    .getSingleResult();
            
            return Response.ok(delta).build();
        }
        catch(NoResultException nre) {
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
        if (removePending(cat.getUidRisorsa())) {
            return;
        }

        // add new deltas to installable devices
        List<Installazione> lInst = em.createQuery("SELECT i FROM Installazione i WHERE i.idCorso = :corso", Installazione.class)
                .setParameter("corso", cat.getIdCorso())
                .getResultList();

        Date syncDate = new Date();
        for (Installazione inst : lInst) {
            // add progressively new delta(s)
            int lastOrder = em.createQuery("SELECT MAX(d.ordine) FROM Delta d WHERE d.idDispositivo = :device", Integer.class)
                    .setParameter("device", inst.getIdDispositivo())
                    .getSingleResult();

            // cerca una sincronizzazione per questo dispositivo in stato "pending"
            List<Sincronizzazione> lSinc = em.createQuery("SELECT s FROM Sincronizzazione s JOIN Dispositivo d WHERE d.idDispositivo = :device AND s.stato = :status ORDER BY s.dataora DESC", Sincronizzazione.class)
                                    .setParameter("device", inst.getIdDispositivo())
                                    .setParameter("status", DeltaConst.Status.PENDING.toString())
                                    .getResultList();
            
            Sincronizzazione sinc;
            if(lSinc.isEmpty()) {
                // non trovata, inserisce una nuova sincronizzazione
                sinc = new Sincronizzazione();
                sinc.setDataora(syncDate);
                sinc.setIdDispositivo(inst.getIdDispositivo());
                sinc.setStato(DeltaConst.Status.PENDING.toString());
                em.persist(sinc);
            }
            else {
                // trovata! usa la più recente
                sinc = lSinc.get(0);
            }
            
            Delta d = new Delta();

            d.setRisorsa(cat.getNome());
            d.setDataSincronizzazione(syncDate);
            d.setIdSincronizzazione(sinc);
            d.setOperazione(operation.toString());
            d.setTipologia(DeltaConst.ResourceType.CATEGORY.toString());
            d.setOrdine(lastOrder + 1);
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
        if (removePending(doc.getUidRisorsa())) {
            return;
        }

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

        Date syncDate = new Date();
        for (Installazione inst : lInst) {
            // add progressively new delta(s)
            int lastOrder = em.createQuery("SELECT MAX(d.ordine) FROM Delta d WHERE d.idDispositivo = :device", Integer.class)
                    .setParameter("device", inst.getIdDispositivo())
                    .getSingleResult();

            Delta d = new Delta();

            d.setRisorsa(doc.getNomefile());
            d.setDataSincronizzazione(syncDate);
            d.setIdDispositivo(inst.getIdDispositivo());
            d.setOperazione(operation.toString());
            d.setTipologia(DeltaConst.ResourceType.DOCUMENT.toString());
            d.setOrdine(lastOrder + 1);
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

    /**
     *
     * @param resourceUid
     * @return
     */
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    private boolean removePending(String resourceUid) {
        // check if there is a pending operation before issuing a new op
        List<Delta> lD = em.createQuery("SELECT d FROM Delta d WHERE d.uidRisorsa = :uid AND d.stato = :status", Delta.class)
                .setParameter("uid", resourceUid)
                .setParameter("status", DeltaConst.Status.PENDING)
                .getResultList();
        if (!lD.isEmpty()) {
            lD.forEach((d) -> {
                em.remove(d);
            });

            return true;
        }

        return false;
    }
}

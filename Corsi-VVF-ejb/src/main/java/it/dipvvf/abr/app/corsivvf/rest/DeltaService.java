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
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@LocalBean
@Path("delta")
@Produces(MediaType.APPLICATION_JSON)
public class DeltaService extends BaseService {
    @PersistenceContext
    EntityManager em;

    /**
     *
     * @param idDev
     * @return
     */
    @GET
    @Path("{iddev: \\d+}")
    public Response getDeviceDeltas(@PathParam("iddev") int idDev) {
        int deltaCount = em.createQuery("SELECT COUNT(d.id) FROM Delta d JOIN Dispositivo dp WHERE dp.id = :idDisp AND d.stato <> 'DONE'", Integer.class)
                .setParameter("idDisp", idDev)
                .getSingleResult();

        return Response.ok(String.valueOf(deltaCount)).build();
    }

    /**
     *
     * @param idDispositivo
     * @return
     */
    @GET
    @Path("{iddev: \\d+}/items")
    public Response getDeviceDeltaItems(@PathParam("iddev") int idDispositivo) {
        List<Integer> lDelta = em.createQuery("SELECT d.id FROM Delta d JOIN Dispositivo dp WHERE dp.id = :idDisp AND d.stato <> 'DONE'")
                .setParameter("idDisp", idDispositivo)
                .getResultList();

        return (lDelta.isEmpty()) ? Response.noContent().build() : Response.ok(lDelta).build();
    }

    /**
     *
     * @param idDispositivo
     * @param idItem
     * @return
     */
    @GET
    @Path("{iddev: \\d+}/items/{iditem: \\d+}")
    public Response getDeviceDeltaItemDetail(@PathParam("iddev") int idDispositivo, @PathParam("iditem") int idItem) {
        List<Delta> lDelta = em.createQuery("SELECT d FROM Delta d JOIN Dispositivo dp WHERE dp.id = :idDisp AND d.id = :idItem")
                .setParameter("idDisp", idDispositivo)
                .setParameter("idItem", idItem)
                .getResultList();

        return (lDelta.isEmpty()) ? Response.noContent().build() : Response.ok(lDelta).build();
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
        if(removePending(cat.getUidRisorsa())) return;
        
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

            Delta d = new Delta();

            d.setRisorsa(cat.getNome());
            d.setDataSincronizzazione(syncDate);
            d.setIdDispositivo(inst.getIdDispositivo());
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
        if(removePending(doc.getUidRisorsa())) return;
        
        // add new deltas to installable devices        
        String sql = "SELECT i FROM Installazione i WHERE i.";
        Object id;
        if(doc.getIdCorso()!=null) {
            sql+="idCorso = :id";
            id = doc.getIdCorso();
        }
        else {
            sql+="idCategoria = :id";
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
            if(doc.getIdCorso()!=null) {
                d.setUidRisorsaPadre(doc.getIdCorso().getUidRisorsa());
                d.setTipoRisorsaPadre(DeltaConst.ResourceType.COURSE.toString());
            }
            else {
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
        if(!lD.isEmpty()) {
            for(Delta d : lD) {
                em.remove(d);
            }
            
            return true;
        }
        
        return false;
    }
}

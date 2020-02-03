/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.rest;

import it.dipvvf.abr.app.corsivvf.ejb.BaseService;
import it.dipvvf.abr.app.corsivvf.ejb.MiscServices;
import it.dipvvf.abr.app.corsivvf.model.Categoria;
import it.dipvvf.abr.app.corsivvf.model.Corso;
import it.dipvvf.abr.app.corsivvf.model.Delta;
import it.dipvvf.abr.app.corsivvf.model.DeltaConst;
import it.dipvvf.abr.app.corsivvf.model.Dispositivo;
import it.dipvvf.abr.app.corsivvf.model.Documento;
import it.dipvvf.abr.app.corsivvf.model.Installazione;
import it.dipvvf.abr.app.corsivvf.model.Sincronizzazione;
import it.dipvvf.abr.app.corsivvf.rest.security.DeviceSecurityCheck;
import it.dipvvf.abr.app.corsivvf.rest.security.JWTSecurityCheck;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@LocalBean
@Path("devices")
@Produces(MediaType.APPLICATION_JSON)
public class DispositiviService extends BaseService {
    @Inject
    MiscServices ms;
    @PersistenceContext
    EntityManager em;
    @Resource
    EJBContext ctx;

    /**
     * 
     * @return 
     */
    @GET
    @JWTSecurityCheck
    public Response getDevices() {
        return ok(em.createQuery("SELECT d.id FROM Dispositivo d").getResultList());
    }

     /**
     * 
     * @param deviceId
     * @return 
     */
    @POST
    @DeviceSecurityCheck
    public Response registerDevice(@HeaderParam("Device-Id") String deviceId) {
        if (deviceId == null) {
            return badRequest();
        }

        // Recupera il device
        
        Dispositivo d;
        try {
            d = em.createQuery("SELECT d FROM Dispositivo d WHERE d.deviceid = :devid", Dispositivo.class)
                .setParameter("devid", deviceId)
                .getSingleResult();
        }
        catch(NoResultException nre) {
            // Non esiste? Restituisce lo stato al client
            return notFound();
        }

        // Token già generato, restituisce lo stato
        if (d.getToken() != null) {
            return noContent();
        }

        // Non abilitato? Segnala al client la mancanza di autorizzazione
        if (!d.getAbilitato()) {
            return unauthorized();
        }

        // Genera un token univoco abbinato al deviceid senza scadenza
        // e aggiorna archivio
        String devToken = ms.createToken(deviceId, MiscServices.NO_EXPIRE);
        d.setToken(devToken);

        return ok(devToken);
    }
    
    /**
     * 
     * @param id
     * @return 
     */
    @GET
    @Path("{id: \\d+}")
    @JWTSecurityCheck
    public Response getDeviceDetail(@PathParam("id") int id) {
        Dispositivo disp = em.find(Dispositivo.class, id);
        return (disp == null) ? notFound() : ok(disp);
    }

    /**
     * 
     * @param id
     * @param d
     * @return 
     */
    @PUT
    @Path("{id: \\d+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @JWTSecurityCheck
    public Response updateDevice(@PathParam("id") int id, Dispositivo d) {
        try {
            em.persist(d);
            em.flush();
            
            return noContent();
        } 
        catch(PersistenceException pe) {
            ctx.setRollbackOnly();
            return conflict(pe.toString());
        }
        catch (Exception e) {
            ctx.setRollbackOnly();
            return error(e.toString());
        }
    }

    /**
     * 
     * @param id
     * @return 
     */
    @GET
    @Path("{id: \\d+}/courses")
    @JWTSecurityCheck
    public Response getDeviceCourses(@PathParam("id") int id) {
        return ok(em.createQuery("SELECT i.id FROM Installazione i JOIN i.idDispositivo d WHERE d.id = :iddisp")
                .setParameter("iddisp", id)
                .getResultList());
    }

    /**
     * 
     * @param id
     * @param idcorso
     * @return 
     */
    @GET
    @Path("{id: \\d+}/courses/{idcourse: \\d+}")
    @JWTSecurityCheck
    public Response getDettaglioDeviceCorsi(@PathParam("id") int id, @PathParam("idcourse") int idcorso) {
        try {
            return ok(em.createQuery("SELECT i FROM Installazione i JOIN i.idDispositivo d JOIN i.idCorso c WHERE d.id = :id AND c.id = :idcorso", Installazione.class)
                    .setParameter("id", id)
                    .setParameter("idcorso", idcorso)
                    .getSingleResult());
        }
        catch(NoResultException nre) {
            return notFound();
        }
    }

    /**
     * 
     * @param id
     * @param idCorso
     * @param info
     * @return 
     */
    @POST
    @Path("{id: \\d+}/courses/{idcourse: \\d+}")
    @JWTSecurityCheck
    public Response installDeviceCourse(@PathParam("id") int id, @PathParam("idcourse") int idCorso, @Context UriInfo info) {
        Dispositivo disp = em.find(Dispositivo.class, id);
        if (disp == null) {
            return notFound();
        }

        Corso corso = em.find(Corso.class, idCorso);
        if (corso == null) {
            return notFound();
        }

        try {
            Date dataSinc = new Date();
            int order = 0;
            
            Installazione inst = new Installazione();
            inst.setIdCorso(corso);
            inst.setIdDispositivo(disp);
            inst.setDataInstallazione(dataSinc);
            em.persist(inst);

            Sincronizzazione sinc = new Sincronizzazione();
            sinc.setIdInstallazione(inst);
            sinc.setDataora(dataSinc);
            sinc.setStato(DeltaConst.Status.PENDING.toString());
            em.persist(sinc);
            
            Delta d = new Delta();
            d.setIdSincronizzazione(sinc);
            d.setRisorsa(corso.getTitolo());
            d.setOperazione(DeltaConst.Operation.ADD.toString());
            d.setTipologia(DeltaConst.ResourceType.COURSE.toString());
            d.setOrdine(order++);
            d.setStato(DeltaConst.Status.PENDING.toString());
            d.setDimensione(-1);
            d.setUidRisorsa(corso.getUidRisorsa());
            em.persist(d);

            List<Categoria> categorie = em.createQuery("SELECT c FROM Categoria c WHERE c.idCorso = :corso", Categoria.class)
                    .setParameter("corso", corso)
                    .getResultList();

            for (Categoria categoria : categorie) {
                d = new Delta();
                
                d.setIdSincronizzazione(sinc);
                d.setRisorsa(categoria.getNome());
                d.setOperazione(DeltaConst.Operation.ADD.toString());
                d.setTipologia(DeltaConst.ResourceType.CATEGORY.toString());
                d.setOrdine(order++);
                d.setStato(DeltaConst.Status.PENDING.toString());
                d.setDimensione(-1);
                d.setTipoRisorsaPadre(DeltaConst.ResourceType.COURSE.toString());
                d.setUidRisorsaPadre(categoria.getIdCorso().getUidRisorsa());
                d.setUidRisorsa(categoria.getUidRisorsa());
                em.persist(d);
            }

            List<Documento> documenti = em.createQuery("SELECT d FROM Documento d WHERE d.idCorso = :corso", Documento.class)
                                        .setParameter("corso", corso)
                                        .getResultList();
            for(Documento documento : documenti) {
                d = new Delta();
                
                d.setIdSincronizzazione(sinc);
                d.setRisorsa(documento.getNomefile());
                d.setOperazione(DeltaConst.Operation.ADD.toString());
                d.setTipologia(DeltaConst.ResourceType.DOCUMENT.toString());
                d.setOrdine(order++);
                d.setStato(DeltaConst.Status.PENDING.toString());
                d.setDimensione(documento.getDimensione());
                d.setMd5(documento.getChecksum());
                d.setUidRisorsaPadre(documento.getIdCorso().getUidRisorsa());
                d.setTipoRisorsaPadre(DeltaConst.ResourceType.COURSE.toString());
                d.setUidRisorsa(documento.getUidRisorsa());
                em.persist(d);
            }
            
            documenti = em.createQuery("SELECT d FROM Documento d JOIN d.idCategoria c WHERE c.idCorso = :corso", Documento.class)
                                        .setParameter("corso", corso)
                                        .getResultList();
            for(Documento documento : documenti) {
                d = new Delta();
                
                d.setIdSincronizzazione(sinc);
                d.setRisorsa(documento.getNomefile());
                d.setOperazione(DeltaConst.Operation.ADD.toString());
                d.setTipologia(DeltaConst.ResourceType.DOCUMENT.toString());
                d.setOrdine(order++);
                d.setStato(DeltaConst.Status.PENDING.toString());
                d.setDimensione(documento.getDimensione());
                d.setMd5(documento.getChecksum());
                d.setUidRisorsaPadre(documento.getIdCategoria().getUidRisorsa());
                d.setTipoRisorsaPadre(DeltaConst.ResourceType.CATEGORY.toString());
                d.setUidRisorsa(documento.getUidRisorsa());
                em.persist(d);
            }
            
            em.flush();

            return created(resourceToURI(info, inst.getId()));
        } 
        catch(PersistenceException pe) {
            ctx.setRollbackOnly();
            return conflict(pe.toString());
        }
        catch(Exception e) {
            ctx.setRollbackOnly();
            return error(e.toString());
        }
    }
}

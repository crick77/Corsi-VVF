/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.ejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author riccardo.iovenitti
 */
public abstract class BaseService {
    /**
     * 
     * @param uriInfo
     * @param resources
     * @return 
     */
    public List<String> resourcesToURI(UriInfo uriInfo, List<? extends Object> resources) {
        List<String> uris = new ArrayList<>(resources!=null ? resources.size() : 1);
        
        if(resources!=null) {                
            resources.stream().map((res) -> {
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                builder.path(String.valueOf(res));
                return builder;
            }).forEachOrdered((builder) -> {
                uris.add(builder.build().toASCIIString());
            });
        }
        
        return uris;
    }
    
    /**
     * 
     * @param uriInfo
     * @param resources
     * @return 
     */
    public List<String> resourcesToURI(UriInfo uriInfo, Collection<? extends Object> resources) {       
        List<? extends Object> l;   
        if(resources!=null) {
            l = new ArrayList<>(resources);       
        }
        else {
            l = new ArrayList<>();
        }
        
        return resourcesToURI(uriInfo, l);
    }
    
    /**
     * 
     * @param uriInfo
     * @param resource
     * @return 
     */
    public String resourceToURI(UriInfo uriInfo, Object resource) {                       
        ArrayList<Object> resources = new ArrayList<>();
        resources.add(resource);
        return resourcesToURI(uriInfo, resources).get(0);
    }
    
    /**
     * 
     * @return 
     */
    public Response ok() {
        return Response.ok().build();
    }
    
    /**
     * 
     * @param entity
     * @return 
     */
    public Response ok(Object entity) {
        return Response.ok(entity).build();
    }
    
    /**
     * 
     * @return 
     */
    public Response badRequest() {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }
    
    /**
     * 
     * @param e
     * @return 
     */
    public Response badRequest(Object e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(e).build();
    }
    
    /**
     * 
     * @return 
     */
    public Response notFound() {
        return Response.status(Response.Status.NOT_FOUND).build();
    }
    
    /**
     * 
     * @return 
     */
    public Response unauthorized() {
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    
    /**
     * 
     * @return 
     */
    public Response noContent() {
        return Response.noContent().build();
    }
    
    /**
     * 
     * @return 
     */
    public Response notModified() {
        return Response.notModified().build();
    }
   
    /**
     * 
     * @return 
     */
    public Response conflict() {
        return Response.status(Response.Status.CONFLICT).build();
    }
    
    /**
     * 
     * @param entity
     * @return 
     */
    public Response conflict(Object entity) {
        return Response.status(Response.Status.CONFLICT).entity(entity).build();
    }
    
    /**
     * 
     * @param location
     * @return 
     */
    public Response created(Object location) {
        return Response.status(Response.Status.CREATED).entity(location).build();
    }
    
    /**
     * 
     * @param entity
     * @return 
     */
    public Response unprocessableEntity(Object entity) {
        return Response.status(422).entity(entity).build();
    }
    
    /**
     * 
     * @return 
     */
    public Response error() {
        return Response.serverError().build();
    }
    
    /**
     * 
     * @param entity
     * @return 
     */
    public Response error(Object entity) {
        return Response.serverError().entity(entity).build();
    }
    
    /**
     * 
     * @param fileName
     * @param data
     * @return 
     */
    public Response downloadFile(String fileName, byte[] data) {
        return Response.ok(data).header("Content-Disposition", "attachment; filename=\""+fileName+"\"").build();
    }
}

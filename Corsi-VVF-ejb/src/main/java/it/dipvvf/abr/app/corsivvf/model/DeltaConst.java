/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.model;

/**
 *
 * @author riccardo.iovenitti
 */
public final class DeltaConst {
    public final class Operation {
        public final static String ADD = "ADD";
        public final static String REMOVE = "REMOVE";
        
        private Operation() {}
    }

    public final class ResourceType {
        public final static String COURSE = "COURSE";
        public final static String CATEGORY = "CATEGORY";
        public final static String DOCUMENT = "DOCUMENT";
        
        private ResourceType() {}
    }
        
    public final class Status {
        public final static String PENDING = "PENDING";
        public final static String ERROR = "ERROR";
        public final static String DONE = "DONE";
        
        private Status() {}
    }
    
    private DeltaConst() {}
}

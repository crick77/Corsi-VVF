/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.bean;

/**
 *
 * @author riccardo.iovenitti
 */
public final class Counter {
    private int count;
    
    public Counter() {        
        count = 0;
    }
    
    public synchronized int next() {        
        return count++;
    }
    
    public synchronized int current() {
        return count;
    }
}

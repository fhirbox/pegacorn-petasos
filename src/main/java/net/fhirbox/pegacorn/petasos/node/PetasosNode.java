/*
 * Based on the code by Mark A. Hunter at
 * https://github.com/fhirbox/pegacorn-communicate-iris/tree/master/src/main/java/net/fhirbox/pegacorn/communicate/iris/utilities
 * and updated for Infinispan 10.x
 * 
 * This class creates the clustered cache manager and configures the shared cache.
 * 
 */
package net.fhirbox.pegacorn.petasos.node;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.enterprise.context.ApplicationScoped;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.eviction.EvictionType;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;

@ApplicationScoped
public class PetasosNode {
 
    private PetasosNode node;

    @Inject
    DefaultCacheManager petasosCacheManager;   

    // The clustered cache
    private Cache<String, String> petasosParcelCache;
    private Cache<String, String> petasosWatchdogCache;

    @PostConstruct
    public void start() {
        // get or create the clustered cache which will hold the transactions (aka Units of Work)
        petasosParcelCache = petasosCacheManager.getCache("petasos-parcel-cache", true);
        petasosParcelCache = petasosCacheManager.getCache("petasos-watchdog-cache", true);
    }
    
    private PetasosNode() {
    }
    
    @Produces
    public PetasosNode getNode() {
        if (node == null) {
            node = new PetasosNode();
        }
        return node;
    }
}
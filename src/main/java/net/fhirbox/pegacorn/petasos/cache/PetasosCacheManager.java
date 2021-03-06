/*
 * Based on the code by Mark A. Hunter at
 * https://github.com/fhirbox/pegacorn-communicate-iris/tree/master/src/main/java/net/fhirbox/pegacorn/communicate/iris/utilities
 * and updated for Infinispan 10.x
 * 
 * This class creates the clustered cache manager and configures the shared cache.
 * 
 */
package net.fhirbox.pegacorn.petasos.cache;

import javax.annotation.PreDestroy;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.enterprise.context.ApplicationScoped;
import org.infinispan.manager.DefaultCacheManager;

import net.fhirbox.pegacorn.deploymentproperties.PetasosProperties;

import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.eviction.EvictionType;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.CacheMode;

@ApplicationScoped
public class PetasosCacheManager {
 
    @Inject
    PetasosProperties petasosProperties;
    
    private DefaultCacheManager petasosCacheManager;

    @Produces
    public DefaultCacheManager getCacheManager() {
        if (petasosCacheManager == null) {
            // configure a named clustered cache configuration using Infinispan defined defaults
            GlobalConfigurationBuilder builder = new GlobalConfigurationBuilder().clusteredDefault();
            
            // complete the config with a cluster name, jgroups config, and enable JMX statistics
            GlobalConfiguration global = builder.transport().clusterName("petasos-cluster").addProperty("configurationFile", "jgroups-petasos.xml").jmx().enable().build();
            
            // define a local configuration for setting finer level properties including
            // individual cache statistics and methods required for configuring the cache
            // as clustered
//            Configuration local = new ConfigurationBuilder().statistics().enable().clustering()
//                    .cacheMode(CacheMode.DIST_SYNC).build();
           // note the doco for each of the persistence methods is poor so I have just copied the
           // persistence config from
           // https://infinispan.org/docs/stable/titles/configuring/configuring.html#configuring_cache_stores-persistence
         // not sure about preload effect when starting a new pod - could out of date info clobber newer info, see
          // https://docs.jboss.org/infinispan/10.1/apidocs/org/infinispan/configuration/cache/AbstractStoreConfigurationBuilder.html#preload(boolean)
           Configuration local = new ConfigurationBuilder().statistics().enable().clustering().cacheMode(CacheMode.DIST_SYNC)
            .persistence()
            .passivation(true) // only write the cache overflow to disk
            .addSingleFileStore() // the disk cache
               .preload(true) 
               .shared(false)
               .fetchPersistentState(true)
               .ignoreModifications(false)
               .purgeOnStartup(false)
               .location(petasosProperties.getCacheOverflowDirectory())
               .async()
                  .enabled(true)
                  .threadPoolSize(5)
            .memory()
//               .storageType(StorageType.BINARY) // let Infinispan decide?
               .evictionType(EvictionType.MEMORY)
               .size(petasosProperties.getCacheSizeInBytes()) // cache size in bytes, need to make configurable
            .build();
            
            // create a cache manager based on the gloabl configuration
            petasosCacheManager = new DefaultCacheManager(global);
            // define a set of caches based on the local configuration
            petasosCacheManager.defineConfiguration("petasos-parcel-cache", local);
            petasosCacheManager.defineConfiguration("petasos-watchdog-cache", "petasos-parcel-cache", local);
            petasosCacheManager.defineConfiguration("petasos-uow-to-wup-map", "petasos-parcel-cache", local);
            petasosCacheManager.defineConfiguration("capability-map", "petasos-parcel-cache", local);
        }
        return petasosCacheManager;
    }
 
    @PreDestroy
    public void cleanUp() {
        petasosCacheManager.stop();
        petasosCacheManager = null;
    }
}
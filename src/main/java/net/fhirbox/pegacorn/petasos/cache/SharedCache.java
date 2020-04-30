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
import javax.enterprise.context.ApplicationScoped;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.CacheMode;

@ApplicationScoped
public class SharedCache {
 
    private DefaultCacheManager shareCacheManager;

    public DefaultCacheManager getCacheManager() {
        if (shareCacheManager == null) {
            // configure a named clustered cache configuration using Infinispan defined defaults
            GlobalConfigurationBuilder builder = new GlobalConfigurationBuilder().clusteredDefault().defaultCacheName("mitaf-clustered-cache");
            
            // complete the config with a cluster name, jgroups config, and enable JMX statistics
            GlobalConfiguration global = builder.transport().clusterName("mitaf-cluster").addProperty("configurationFile", "jgroups-k8s.xml").jmx().enable().build();
            
            // define a local configuration for setting finer level properties including
            // individual cache statistics and methods required for configuring the cache
            // as clustered
            Configuration local = new ConfigurationBuilder().statistics().enable().clustering()
                    .cacheMode(CacheMode.DIST_SYNC).build();
            
            // create a cache manager based on the configurations
            shareCacheManager = new DefaultCacheManager(global, local, true);
        }
        return shareCacheManager;
    }
 
    @PreDestroy
    public void cleanUp() {
        shareCacheManager.stop();
        shareCacheManager = null;
    }
}
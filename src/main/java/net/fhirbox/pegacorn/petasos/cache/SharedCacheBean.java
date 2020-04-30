package net.fhirbox.pegacorn.petasos.cache;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import org.infinispan.manager.DefaultCacheManager;

/**
 *
 * @author Mark A. Hunter (ACT Health)
 */
@ApplicationScoped
public class SharedCacheBean {
  
    @Inject
    SharedCache cacheManagerProvider;
 
    @Produces
    DefaultCacheManager getDefaultCacheManager() {
        return cacheManagerProvider.getCacheManager();
    }
}
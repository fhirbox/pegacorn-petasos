package net.fhirbox.pegacorn.petasos.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirbox.pegacorn.petasos.common.PetasosParcelJSON;
import net.fhirbox.pegacorn.petasos.model.UoW;

import java.util.concurrent.CompletionStage;

import java.util.concurrent.CompletableFuture;

import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;

// clustered to ensure all nodes get the notification, async (sync=false) to ensure listener
// is notified in a separate thread so our listener processing is non-blocking.
// see https://docs.jboss.org/infinispan/10.1/apidocs/org/infinispan/notifications/Listener.html
@Listener(clustered = true, sync=false)
public class ParcelMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(ParcelMonitor.class);

    PetasosNode node;
    
    // when a parcel is added, check if it is a multicast and add its FDN to a list which
    // will hold all parcel FDNs for that UoW FDN.
    @CacheEntryCreated
    public CompletionStage<Void> monitorParcel(CacheEntryCreatedEvent<String, String> event) {
        PetasosParcelJSON parcelJSON = new PetasosParcelJSON(event.getValue());
        String uowQualifiedFDN = parcelJSON.getUoWFDN();
        // if multicast, store it with the Node
        if (uowQualifiedFDN.contains(UoW.HASH_ATTRIBUTE)) {
            node.registerMulticastParcel(uowQualifiedFDN, parcelJSON);
        }

        // Not clear if Infinispan manages non-blocking listener tasks
        // https://infinispan.org/docs/stable/titles/developing/developing.html#synchronicity_of_events
        // https://stackoverflow.com/questions/46641700/what-is-the-correct-way-to-create-an-already-completed-completablefuturevoid/46642974#46642974
        CompletableFuture<Void> cf = CompletableFuture.allOf();
        return cf;
    }
    
    // Listeners are not managed by the container, so not using injection
    public void setNodeReference(PetasosNode node) {
        this.node = node;
    } 
}
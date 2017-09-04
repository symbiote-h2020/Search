package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.core.internal.CoreResourceRegisteredOrModifiedEventPayload;

/**
 * Interface containing all relevant interfaces for Resource related events.
 *
 * Created by Mael on 16/01/2017.
 */
public interface IResourceEvents {

    /**
     * Registers resource representation in the Apache Jena repository.
     *
     * @param resources Resource to be saved
     * @return <code>true</code> if registration was successful.
     */
    public boolean registerResource( CoreResourceRegisteredOrModifiedEventPayload resources );

    /**
     * Updates specified resource representation in the Apache Jena repository.
     *
     * @param resources Updated resource
     * @return <code>true</code> if update was successful.
     */
    public boolean updateResource( CoreResourceRegisteredOrModifiedEventPayload resources );

    /**
     * Deletes resource representation in the Apache Jena repository.
     *
     * @param resourceId Id of the resource to be deleted
     * @return <code>true</code> if deletion was successful.
     */
    public boolean deleteResource( String resourceId );

}

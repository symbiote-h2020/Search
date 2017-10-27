package eu.h2020.symbiote.handlers;


import eu.h2020.symbiote.model.mim.Platform;

/**
 * Interface containing all relevant interfaces for Platform related events.
 *
 * Created by Mael on 11/01/2017.
 */
public interface IPlatformEvents {

    /**
     * Registers platform representation in the Apache Jena repository.
     *
     * @param platform Platform to be saved
     * @return <code>true</code> if registration was successful.
     */
    public boolean registerPlatform( Platform platform );

    /**
     * Updates platform representation in the Apache Jena repository.
     *
     * @param platform Platform to be updated
     * @return <code>true</code> if update was successful.
     */
    public boolean updatePlatform( Platform platform );

    /**
     * Deletes platform representation in the Apache Jena repository.
     *
     * @param platformId Id of the platform to be deleted
     * @return <code>true</code> if delete was successful.
     */
    public boolean deletePlatform( String platformId );

}
package eu.h2020.symbiote.handlers;


import eu.h2020.symbiote.cloud.model.ssp.SspRegInfo;
import eu.h2020.symbiote.model.mim.Platform;
import eu.h2020.symbiote.model.mim.SmartSpace;

/**
 * Interface containing all relevant interfaces for SmartSpace related events.
 *
 * Created by Mael on 11/01/2017.
 */
public interface ISspEvents {

    /**
     * Registers smart space representation in the Apache Jena repository.
     *
     * @param ssp Smart space to be saved
     * @return <code>true</code> if registration was successful.
     */
    public boolean registerSsp(SmartSpace ssp);

    /**
     * Updates smart space representation in the Apache Jena repository.
     *
     * @param ssp Smart space to be updated
     * @return <code>true</code> if update was successful.
     */
    public boolean updateSsp(SmartSpace ssp);

    /**
     * Deletes smart space representation in the Apache Jena repository.
     *
     * @param sspId Id of the ssp to be deleted
     * @return <code>true</code> if delete was successful.
     */
    public boolean deleteSsp(String sspId);


    /**
     * Registers smart space sdev representation in the Apache Jena repository.
     *
     * @param sdev Sdev to be saved
     * @return <code>true</code> if registration was successful.
     */
    public boolean registerSdev(SspRegInfo sdev);

    /**
     * Updates smart space sdev representation in the Apache Jena repository.
     *
     * @param sdev Sdev to be updated
     * @return <code>true</code> if update was successful.
     */
    public boolean updateSdev(SspRegInfo sdev);

    /**
     * Deletes smart space sdev representation in the Apache Jena repository.
     *
     * @param sdevId Id of the sdev to be deleted
     * @return <code>true</code> if delete was successful.
     */
    public boolean deleteSdev(String sdevId);


}
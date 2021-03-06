/*
 * Copyright 2012 hbz NRW (http://www.hbz-nrw.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.nrw.hbz.regal.sync.ingest;

import de.nrw.hbz.regal.sync.extern.DigitalEntity;

/**
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 * 
 */
public interface IngestInterface {
    /**
     * Ingests a digitool object in the archive
     * 
     * @param dtlBean
     *            the java representation of a digitool object
     */
    public abstract void ingest(DigitalEntity dtlBean);

    /**
     * Deletes a object from the archive
     * 
     * @param pid
     *            the pid of the object
     */
    public abstract void delete(String pid);

    /**
     * Updates a object in the archive
     * 
     * @param dtlBean
     *            the digitool object
     */
    public abstract void update(DigitalEntity dtlBean);

    /**
     * 
     * @param host
     *            the host of the webapi
     * @param user
     *            a valid user
     * @param password
     *            the users password
     * @param ns
     *            the namespace to operate on
     * @param keystore
     *            if keystore is not null, the connection will use a secured
     *            channel
     * 
     */
    public abstract void init(String host, String user, String password,
	    String ns, KeystoreConf keystore);

    /**
     * Gives the ingester a chance to test a newly created version
     */
    public abstract void test();

}
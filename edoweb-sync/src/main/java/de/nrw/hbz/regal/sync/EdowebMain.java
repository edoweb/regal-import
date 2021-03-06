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
package de.nrw.hbz.regal.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nrw.hbz.regal.sync.ingest.DigitoolDownloader;
import de.nrw.hbz.regal.sync.ingest.EdowebDigitalEntityBuilder;
import de.nrw.hbz.regal.sync.ingest.EdowebIngester;

/**
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 * 
 */
public class EdowebMain {
    final static Logger logger = LoggerFactory.getLogger(EdowebMain.class);

    /**
     * @param args
     *            standard args see Syncer
     */
    public static void main(String[] args) {
	Syncer syncer = new Syncer(new EdowebIngester(),
		new DigitoolDownloader(), new EdowebDigitalEntityBuilder());
	syncer.main(args);
    }
}

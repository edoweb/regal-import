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
package de.nrw.hbz.regal;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 * 
 */
public class PIDReporter {

    @SuppressWarnings({ "javadoc", "serial" })
    public class LoadPropertiesException extends RuntimeException {

	public LoadPropertiesException(String message) {
	    super(message);
	}

	public LoadPropertiesException(Throwable cause) {
	    super(cause);
	}
    }

    final static Logger logger = LoggerFactory.getLogger(PIDReporter.class);

    String pidFile = null;

    OaiPidGrabber mygrabber = null;

    /**
     * Default constructor
     * 
     */
    public PIDReporter() {

    }

    /**
     * @param oaiServer
     *            enpoint of a oai interface
     * @param timestampFile
     *            a file which contains a timestamp
     */
    public PIDReporter(String oaiServer, String timestampFile) {
	mygrabber = new OaiPidGrabber(oaiServer, timestampFile);
    }

    /**
     * @param sets
     *            sets to harvest
     * @param harvestFromScratch
     *            if true timestamp will be ignored.
     * @param collectPidStrategy
     *            defines how to extract pids from oai-identifiers
     * @param format
     *            the provided format to look for
     * @return a list of pids
     */
    public List<String> harvest(String sets, boolean harvestFromScratch,
	    CollectPidStrategy collectPidStrategy, String format) {
	return mygrabber.listPids(sets, harvestFromScratch, collectPidStrategy,
		format);
    }

    /**
     * Returns a list of pids which are provided over an oai interface. The
     * exact configuration is provided by a properties file
     * 
     * @param propFile
     *            a properties file
     * @return a list of pids
     */
    public List<String> getPids(String propFile) {
	Properties properties = new Properties();
	try {
	    properties.load(new BufferedInputStream(new FileInputStream(
		    propFile)));
	} catch (IOException e) {
	    throw new LoadPropertiesException("Could not open " + propFile
		    + "!");
	}

	String server = properties.getProperty("pidreporter.server");
	String set = properties.getProperty("pidreporter.set");
	String timestampFile = properties
		.getProperty("pidreporter.timestampFile");
	String[] sets = null;
	if (set.compareTo("null") != 0) {
	    sets = set.split(",");
	}

	boolean harvestFromScratch = false;
	String hfs = properties.getProperty("pidreporter.harvestFromScratch");
	if (hfs.compareTo("true") == 0) {
	    harvestFromScratch = true;
	}

	CollectPidStrategy collectPidStrategy = new DefaultCollectPidStrategy();
	String strategy = properties.getProperty("pidreporter.strategy");
	if (strategy.equals("Digitool")) {
	    collectPidStrategy = new DigitoolPidStrategy();
	}
	String format = properties.getProperty("pidreporter.format");
	if (format == null || format.isEmpty()) {
	    format = "oai_dc";
	}

	pidFile = properties.getProperty("pidreporter.pidFile");

	OaiPidGrabber grabber = new OaiPidGrabber(server, timestampFile);
	return grabber.listPids(sets, harvestFromScratch, collectPidStrategy,
		format);
    }

    private void run(String propFile) {
	PIDWriter writer = new PIDWriter();
	writer.print(getPids(propFile), pidFile);
    }

    /**
     * @param argv
     *            must contain one item which points to a property file
     */
    public static void main(String[] argv) {
	if (argv.length != 1) {
	    System.out.println("\nWrong Number of Arguments!");
	    System.out.println("Please specify a config.properties file!");
	    System.out
		    .println("Example: java -jar pidreporter.jar pidreporter.properties\n");
	    System.out
		    .println("Example Properties File:\n\tpidreporter.server=http://localhost/edowebOAI/\n\tpidreporter.set=null\n\tpidreporter.harvestFromScratch=true\n\tpidreporter.pidFile=pids.txt");
	    System.exit(1);
	}
	PIDReporter main = new PIDReporter();

	main.run(argv[0]);

    }

}

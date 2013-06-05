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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.nrw.hbz.regal.DownloaderInterface;
import de.nrw.hbz.regal.PIDReporter;

/**
 * Class DigitoolDownloader
 * 
 * http://193.30.112.23:9280/fedora/get/dipp:1001?xml=true
 * http://193.30.112.23:9280/fedora/listDatastreams/dipp:1001?xml=true
 * http://193.30.112.23:9280/fedora/get/dipp:1001/DiPPExt
 * 
 * <p>
 * <em>Title: </em>
 * </p>
 * <p>
 * Description:
 * </p>
 * 
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 * 
 */
public class DippDownloader implements DownloaderInterface
{
	final static Logger logger = LoggerFactory.getLogger(DippDownloader.class);

	// DigitalEntityBeanBuilder beanBuilder = null;
	String downloadLocation = null;
	String objectDirectory = null;
	String server = null;
	boolean updated = false;
	boolean downloaded = false;
	HashMap<String, String> map = new HashMap<String, String>();

	public DippDownloader()
	{
		// beanBuilder = new DigitalEntityBeanBuilder();

	}

	/**
	 * @param server
	 *            the digitool server to download from
	 * @param downloadLocation
	 *            a local directory to store the downloaded data
	 */
	public void init(String server, String downloadLocation)
	{
		this.downloadLocation = downloadLocation;
		this.server = server;
		// beanBuilder = new DigitalEntityBeanBuilder();
	}

	/**
	 * @param pid
	 *            the digitool pid
	 * @return a message for the user
	 * @throws IOException
	 *             if something goes wrong
	 */
	public String download(String pid) throws IOException
	{

		return download(pid, true);

	}

	/**
	 * @param pid
	 *            a valid digitool pid
	 * @param forceDownload
	 *            if true the data will be downloaded. if false the data will
	 *            only be downloaded if isn't there yet
	 * @return a message for the user
	 * @throws IOException
	 *             if something goes wrong
	 */
	public String download(String pid, boolean forceDownload)
			throws IOException
	{

		if (map.containsKey(pid))
			throw new IOException(pid + " already visited!");
		objectDirectory = downloadLocation + File.separator
				+ URLEncoder.encode(pid);
		File dir = new File(objectDirectory);
		if (!dir.exists())
		{
			logger.info("Create Directory " + dir.getAbsoluteFile()
					+ " and start to Download files");
			dir.mkdir();

			try
			{
				downloadObject(dir, pid);
			}
			catch (Exception e)
			{
				logger.debug(e.getMessage());
			}
			setUpdated(false);
			setDownloaded(true);
		}
		else if (forceDownload)
		{
			logger.info("Directory " + dir.getAbsoluteFile()
					+ " exists. Force override.");
			FileUtils.deleteDirectory(dir);
			dir.mkdir();

			try
			{
				downloadObject(dir, pid);
			}
			catch (Exception e)
			{
				logger.debug(e.getMessage());
			}

			setUpdated(true);
			setDownloaded(true);
		}
		else
		{
			logger.info("Directory " + dir.getAbsoluteFile()
					+ " exists. Step over.");
			setDownloaded(false);
			setUpdated(false);
		}
		map.clear();
		return dir.getAbsolutePath();

	}

	private void downloadObject(File dir, String pid) throws Exception
	{
		try
		{
			if (!map.containsKey(pid))
			{
				map.put(pid, pid);

			}
			else
			{
				throw new Exception(pid + " already visited!");
			}
			logger.debug(pid + " start download!");
			URL url = new URL(server + "get/" + pid + "?xml=true");
			File file = new File(dir.getAbsolutePath() + File.separator
					+ URLEncoder.encode(pid) + ".xml");
			String data = null;
			StringWriter writer = new StringWriter();
			IOUtils.copy(url.openStream(), writer);
			data = writer.toString();
			FileUtils.writeStringToFile(file, data, "utf-8");

			downloadStreams(dir, pid);
			downloadConstituent(dir, pid);
			downloadRelatedObject(dir, pid, "rel:hasPart");
			File downloadDir = new File(downloadLocation);

			// downloadRelatedObject(new File(downloadLocation), pid,
			// "rel:hasMember");
			// downloadRelatedObject(new File(downloadLocation), pid,
			// "rel:hasSubset");
			// downloadRelatedObject(new File(downloadLocation), pid,
			// "rel:hasCollectionMember");
			// downloadRelatedObject(new File(downloadLocation), pid,
			// "rel:hasDerivation");
			// downloadRelatedObject(new File(downloadLocation), pid,
			// "rel:hasDependent");
			// downloadRelatedObject(new File(downloadLocation), pid,
			// "rel:hasDescription");
			// downloadRelatedObject(new File(downloadLocation), pid,
			// "rel:hasMetadata");
			// downloadRelatedObject(new File(downloadLocation), pid,
			// "rel:hasAnnotation");
			//
			// downloadRelatedObject(new File(downloadLocation), pid,
			// "rel:hasEquivalent");

			downloadRelatedObject(dir, pid, "rel:isPartOf");
			downloadRelatedObject(new File(downloadLocation), pid,
					"rel:isMemberOf");
			downloadRelatedObject(new File(downloadLocation), pid,
					"rel:isSubsetOf");
			downloadRelatedObject(new File(downloadLocation), pid,
					"rel:isMemberOfCollection");
			// downloadRelatedObject(new File(downloadLocation), pid,
			// "rel:isDerivationOf");
			// downloadRelatedObject(new File(downloadLocation), pid,
			// "rel:isDependentOf");
			// downloadRelatedObject(new File(downloadLocation), pid,
			// "rel:isDescriptionOf");
			// downloadRelatedObject(new File(downloadLocation), pid,
			// "rel:isMetadataFor");
			// downloadRelatedObject(new File(downloadLocation), pid,
			// "rel:isAnnotationOf");

		}
		catch (MalformedURLException e)
		{
			logger.error(e.getMessage());
		}
		catch (IOException e)
		{
			logger.error(e.getMessage());
		}

	}

	private void downloadConstituent(File dir, String pid)
	{
		String relation = "rel:hasConstituent";

		try
		{
			URL url = new URL(server + "get/" + pid + "/RELS-EXT");
			String data = null;
			StringWriter writer = new StringWriter();
			IOUtils.copy(url.openStream(), writer);
			data = writer.toString();

			Element root = stringToElement(data);
			NodeList constituents = root.getElementsByTagName(relation);
			if (constituents == null || constituents.getLength() == 0)
				return;
			File zipDir = new File(dir.getAbsolutePath() + File.separator
					+ "content");
			for (int i = 0; i < constituents.getLength(); i++)
			{

				Element c = (Element) constituents.item(i);
				String cPid = c.getAttribute("rdf:resource").replace(
						"info:fedora/", "");
				if (cPid.contains("temp"))
				{
					logger.debug(cPid + " skip temporary object.");

				}
				else
				{
					File cDir = new File(dir.getAbsolutePath() + File.separator

					+ URLEncoder.encode(cPid));

					try
					{
						downloadObject(cDir, cPid);
					}
					catch (Exception e)
					{
						logger.warn(e.getMessage());
					}
					try
					{
						map.remove(cPid);
						downloadObject(zipDir, cPid);
					}
					catch (Exception e)
					{
						logger.warn(e.getMessage());
					}
				}

			}
			File cFile = new File(dir.getAbsolutePath() + File.separator
					+ "content.zip");
			logger.debug("I will zip now! " + zipDir.getAbsolutePath() + " to "
					+ cFile.getAbsolutePath());
			zip(zipDir, cFile);

		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
		}

	}

	private void downloadRelatedObject(File dir, String pid, String relation)
	{
		try
		{
			URL url = new URL(server + "get/" + pid + "/RELS-EXT");
			String data = null;
			StringWriter writer = new StringWriter();
			IOUtils.copy(url.openStream(), writer);
			data = writer.toString();

			Element root = stringToElement(data);
			NodeList constituents = root.getElementsByTagName(relation);
			for (int i = 0; i < constituents.getLength(); i++)
			{
				try
				{
					Element c = (Element) constituents.item(i);
					String cPid = c.getAttribute("rdf:resource").replace(
							"info:fedora/", "");

					logger.debug(pid + " " + relation + " " + cPid);
					// if (!cPid.contains("oai") && !cPid.contains("temp")
					// && !pid.contains("oai") && !pid.contains("temp"))
					logger.info("DOWNLOAD-GRAPH: \"" + pid + "\"->\"" + cPid
							+ "\" [label=\"" + relation + "\"]");

					if (cPid.contains("temp"))
					{
						logger.debug(cPid + " skip temporary object.");

					}
					else
					{
						File cDir = new File(dir.getAbsolutePath()
								+ File.separator

								+ URLEncoder.encode(cPid));

						downloadObject(cDir, cPid);
					}
				}
				catch (Exception e)
				{
					logger.debug(e.getMessage());
				}

			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
		}
	}

	private void downloadStreams(File dir, String pid)
	{
		try
		{
			URL url = new URL(server + "listDatastreams/" + pid + "?xml=true");
			String data = null;
			StringWriter writer = new StringWriter();
			IOUtils.copy(url.openStream(), writer);
			data = writer.toString();

			Element root = stringToElement(data);
			NodeList dss = root.getElementsByTagName("datastream");

			for (int i = 0; i < dss.getLength(); i++)
			{
				Element dsel = (Element) dss.item(i);
				String datastreamName = dsel.getAttribute("dsid");
				String fileName = dsel.getAttribute("label");
				String mimeType = dsel.getAttribute("mimeType");

				if (mimeType.contains("xml"))
				{
					fileName = datastreamName + ".xml";
				}
				if (mimeType.contains("html"))
				{
					fileName = fileName + ".html";
				}

				URL dataStreamUrl = new URL(server + "get/" + pid + "/"
						+ datastreamName);
				File dataStreamFile = new File(dir.getAbsolutePath()
						+ File.separator + "" + fileName);

				InputStream in = null;
				try
				{
					URLConnection uc = dataStreamUrl.openConnection();
					uc.connect();
					in = uc.getInputStream();
					FileOutputStream out = new FileOutputStream(dataStreamFile);

					byte[] buffer = new byte[1024];
					int bytesRead = -1;
					while ((bytesRead = in.read(buffer)) > -1)
					{
						out.write(buffer, 0, bytesRead);
					}
					in.close();

				}
				catch (IOException e)
				{
					logger.error(pid + " problem downloading stream "
							+ datastreamName);
				}
				finally
				{
					try
					{
						if (in != null)
							in.close();
					}
					catch (IOException e)
					{
						logger.error(pid + " problem downloading stream "
								+ datastreamName);
					}
				}

			}

		}
		catch (MalformedURLException e)
		{
			logger.error(e.getMessage());
		}
		catch (IOException e)
		{
			logger.error(e.getMessage());
		}
	}

	private Element stringToElement(String data)
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder;

			docBuilder = factory.newDocumentBuilder();

			Document doc;

			doc = docBuilder.parse(new ByteArrayInputStream(data
					.getBytes("utf-8")));
			Element root = doc.getDocumentElement();
			root.normalize();
			return root;
		}
		catch (FileNotFoundException e)
		{

			e.printStackTrace();
		}
		catch (SAXException e)
		{

			e.printStackTrace();
		}
		catch (IOException e)
		{

			e.printStackTrace();
		}
		catch (ParserConfigurationException e)
		{

			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return true if the downloader has updated an existing dataset
	 */
	public boolean hasUpdated()
	{
		return updated;
	}

	/**
	 * @return true if data has been downloaded
	 */
	public boolean hasDownloaded()
	{
		return downloaded;
	}

	private void setDownloaded(boolean downloaded)
	{
		this.downloaded = downloaded;
	}

	private void setUpdated(boolean updated)
	{
		this.updated = updated;
	}

	private File getXml(File file, URL url) throws IOException
	{

		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream(), "UTF-8"));

		StringWriter strOut = new StringWriter();

		// Copy stream to String
		char[] buf = new char[1024];
		int n;
		while ((n = in.read(buf)) != -1)
		{
			strOut.write(buf, 0, n);
		}

		String str = strOut.toString();
		strOut.close();
		in.close();

		// copy String to File
		in = new BufferedReader(new StringReader(str));
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		buf = new char[1024];
		while ((n = in.read(buf)) != -1)
		{
			out.write(buf, 0, n);
		}

		out.close();
		in.close();

		return file;
	}

	private Element getDocument(File digitalEntityFile)
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder;

			docBuilder = factory.newDocumentBuilder();

			Document doc;

			doc = docBuilder.parse(new BufferedInputStream(new FileInputStream(
					digitalEntityFile)));
			Element root = doc.getDocumentElement();
			root.normalize();
			return root;
		}
		catch (FileNotFoundException e)
		{

			e.printStackTrace();
		}
		catch (SAXException e)
		{

			e.printStackTrace();
		}
		catch (IOException e)
		{

			e.printStackTrace();
		}
		catch (ParserConfigurationException e)
		{

			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	void zip(File directory, File zipfile) throws IOException
	{
		URI base = directory.toURI();
		Deque<File> queue = new LinkedList<File>();
		queue.push(directory);
		OutputStream out = new FileOutputStream(zipfile);
		Closeable res = out;
		try
		{
			ZipOutputStream zout = new ZipOutputStream(out);
			res = zout;
			while (!queue.isEmpty())
			{
				directory = queue.pop();
				for (File kid : directory.listFiles())
				{
					String name = base.relativize(kid.toURI()).getPath();
					if (kid.isDirectory())
					{
						queue.push(kid);
						name = name.endsWith("/") ? name : name + "/";
						zout.putNextEntry(new ZipEntry(name));
					}
					else
					{
						zout.putNextEntry(new ZipEntry(name));
						copy(kid, zout);
						zout.closeEntry();
					}
				}
			}
		}
		finally
		{
			res.close();
		}
	}

	void copy(InputStream in, OutputStream out) throws IOException
	{
		byte[] buffer = new byte[1024];
		while (true)
		{
			int readCount = in.read(buffer);
			if (readCount < 0)
			{
				break;
			}
			out.write(buffer, 0, readCount);
		}
	}

	void copy(File file, OutputStream out) throws IOException
	{
		InputStream in = new FileInputStream(file);
		try
		{
			copy(in, out);
		}
		finally
		{
			in.close();
		}
	}

	void copy(InputStream in, File file) throws IOException
	{
		OutputStream out = new FileOutputStream(file);
		try
		{
			copy(in, out);
		}
		finally
		{
			out.close();
		}
	}

	private void run(String propFile) throws IOException
	{
		Properties properties = new Properties();
		try
		{
			properties.load(new BufferedInputStream(new FileInputStream(
					propFile)));
		}
		catch (IOException e)
		{
			throw new IOException("Could not open " + propFile + "!");
		}
		this.server = properties.getProperty("piddownloader.server");
		this.downloadLocation = properties
				.getProperty("piddownloader.downloadLocation");

		PIDReporter pidreporter = new PIDReporter();
		Vector<String> pids = pidreporter.getPids(propFile);

		for (int i = 0; i < pids.size(); i++)
		{
			String pid = pids.elementAt(i);
			logger.info((i + 1) + "/" + pids.size() + " Download " + pid + " !");
			download(pid);
		}

	}

	/**
	 * @param argv
	 *            the argument vector must contain exactly one item which points
	 *            to a valid property file
	 */
	public static void main(String[] argv)
	{
		if (argv.length != 1)
		{
			System.out.println("\nWrong Number of Arguments!");
			System.out.println("Please specify a config.properties file!");
			System.out
					.println("Example: java -jar dtldownloader.jar dtldownloader.properties\n");
			System.out
					.println("Example Properties File:\n\tpidreporter.server=http://urania.hbz-nrw.de:1801/edowebOAI/\n\tpidreporter.set=null\n\tpidreporter.harvestFromScratch=true\n\tpidreporter.pidFile=pids.txt\n\tpiddownloader.server=http://klio.hbz-nrw.de:1801\n\tpiddownloader.downloadLocation=/tmp/zbmed");
			System.exit(1);
		}

		DippDownloader main = new DippDownloader();
		try
		{
			main.run(argv[0]);
		}
		catch (IOException e)
		{
			logger.warn(e.getMessage());
			System.exit(2);
		}
	}

}
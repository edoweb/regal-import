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

import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Element;

import archive.fedora.XmlUtils;
import de.nrw.hbz.regal.sync.extern.DigitalEntity;
import de.nrw.hbz.regal.sync.extern.StreamType;

/**
 * @author Jan Schnasse schnasse@hbz-nrw.de
 * 
 */
public class EllinetDigitalEntityBuilder extends EdowebDigitalEntityBuilder {
    @SuppressWarnings("javadoc")
    public class DcNamespaceContext implements NamespaceContext {

	public String getNamespaceURI(String prefix) {
	    if (prefix == null)
		throw new NullPointerException("Null prefix");
	    else if ("dc".equals(prefix))
		return "http://purl.org/dc/elements/1.1/";
	    else if ("xml".equals(prefix))
		return XMLConstants.XML_NS_URI;
	    return XMLConstants.NULL_NS_URI;
	}

	// This method isn't necessary for XPath processing.
	public String getPrefix(String uri) {
	    throw new UnsupportedOperationException();
	}

	// This method isn't necessary for XPath processing either.
	@SuppressWarnings("rawtypes")
	public Iterator getPrefixes(String uri) {
	    throw new UnsupportedOperationException();
	}

    }

    @Override
    void setCatalogId(DigitalEntity dtlDe) {
	if (dtlDe.getStream(StreamType.DC) == null) {
	    throw new CatalogIdNotFoundException("Found no DC stream");
	}
	Element root = XmlUtils.getNamespaceAwareDocument(dtlDe.getStream(
		StreamType.DC).getFile());
	List<Element> elements = XmlUtils.getElements("//dc:alephsyncid", root,
		new DcNamespaceContext());
	if (elements.size() != 1) {
	    throw new CatalogIdNotFoundException("Found " + elements.size()
		    + " ids");
	}
	String id = elements.get(0).getTextContent();
	dtlDe.addIdentifier(id);
	logger.info(dtlDe.getPid() + " add id " + id);
    }
}

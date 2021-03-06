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
package de.nrw.hbz.regal.sync.extern;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import archive.fedora.XmlUtils;

/**
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 * 
 */
public class DigitalEntity {
    final static Logger logger = LoggerFactory.getLogger(DigitalEntity.class);

    @SuppressWarnings({ "javadoc", "serial" })
    public class NoPidException extends RuntimeException {

	public NoPidException(String message) {
	    super(message);
	}

	public NoPidException(Throwable cause) {
	    super(cause);
	}
    }

    private boolean isParent = true;
    private String pid = null;
    private String usageType = null;
    private String location = null;
    private List<String> identifier = null;
    private List<String> transformer = null;

    private HashMap<StreamType, Stream> streams = null;
    private List<RelatedDigitalEntity> related = null;

    private String parentPid;
    private String label = null;
    private String type;
    private int order = -1;

    private File xml = null;
    private String createdBy;
    private String importedFrom;
    private String legacyId;

    /**
     * @param location
     *            the directory of the entity
     */
    public DigitalEntity(String location) {
	this.location = location;

	related = new Vector<RelatedDigitalEntity>();
	streams = new HashMap<StreamType, Stream>();
	identifier = new Vector<String>();
	setTransformer(new Vector<String>());
    }

    /**
     * @param location
     *            the directory of the entity
     * @param pid
     *            The pid of the entity
     */
    public DigitalEntity(String location, String pid) {
	this.location = location;
	this.pid = pid;
	related = new Vector<RelatedDigitalEntity>();
	streams = new HashMap<StreamType, Stream>();
	identifier = new Vector<String>();
	setTransformer(new Vector<String>());
    }

    /**
     * A DigitalEntity has identifiers
     * 
     * @return a list of identifiers
     */
    public List<String> getIdentifier() {
	return identifier;
    }

    /**
     * @param identifier
     *            a list of identifiers
     */
    public void setIdentifier(List<String> identifier) {
	this.identifier = identifier;
    }

    /**
     * @return a pid for the entity
     */
    public String getPid() {
	return pid;
    }

    /**
     * @param pid
     *            a pid for the entity
     */
    public void setPid(String pid) {
	this.pid = pid;
    }

    /**
     * @param type
     *            stream type
     * @return the stream
     */
    public Stream getStream(StreamType type) {
	return streams.get(type);
    }

    /**
     * @param xml
     *            a xml file
     */
    public void setXml(File xml) {
	this.xml = xml;
    }

    /**
     * @return xml file
     */
    public File getXml() {
	return xml;
    }

    /**
     * 
     * @return a xml representation of myself
     */
    public File getMe() {
	return new File(location + File.separator + pid + ".xml");
    }

    /**
     * @param label
     *            a label for the entity
     */
    public void setLabel(String label) {
	this.label = label;
    }

    /**
     * @return a label for the entity
     */
    public String getLabel() {
	return label;
    }

    /**
     * @return do you have children?
     */
    public boolean isParent() {
	return isParent;
    }

    /**
     * @param isParent
     *            do you have children?
     */
    public void setIsParent(boolean isParent) {
	this.isParent = isParent;
    }

    /**
     * @param relPid
     *            set a parent
     */
    public void setParentPid(String relPid) {
	parentPid = relPid;
    }

    /**
     * @return if there is a hierarchy, return your parent
     */
    public String getParentPid() {
	return parentPid;
    }

    /**
     * @return the entities type
     */
    public String getType() {
	return type;
    }

    /**
     * @param type
     *            a user defined type for the entity
     */
    public void setType(String type) {
	this.type = type;
    }

    /**
     * @return all related entities
     */
    public List<RelatedDigitalEntity> getRelated() {
	return related;
    }

    /**
     * @param related
     *            alle related entities
     */
    public void setRelated(List<RelatedDigitalEntity> related) {
	this.related = related;
    }

    /**
     * @param entity
     *            a related entity
     * @param relation
     *            a user defined relation
     */
    public void addRelated(DigitalEntity entity, String relation) {
	addRelated(new RelatedDigitalEntity(entity, relation));
    }

    /**
     * @param relation
     *            a related entity
     */
    public void addRelated(RelatedDigitalEntity relation) {
	related.add(relation);
    }

    /**
     * @param file
     *            a file
     * @param mime
     *            a mimetype, e.g. application/pdf
     * @param type
     *            a stream type
     * @param fileId
     *            a id for the file
     * @param md5Hash
     *            an md5Hash to control transmission
     */
    public void addStream(File file, String mime, StreamType type,
	    String fileId, String md5Hash) {
	streams.put(type, new Stream(file, mime, type, fileId, md5Hash));

    }

    /**
     * @return the data stream
     */
    public File getStream() {
	return streams.get(StreamType.DATA).getFile();
    }

    /**
     * @return the base location of the streams in filesystem
     */
    public String getLocation() {
	return location;
    }

    /**
     * @return dc string if io fails
     */
    public String getDc() {
	return XmlUtils.fileToString(streams.get(StreamType.DC).getFile());
    }

    /**
     * @return all related Objects with part_of relation
     */
    public Vector<DigitalEntity> getParts() {
	Vector<DigitalEntity> links = new Vector<DigitalEntity>();
	for (RelatedDigitalEntity rel : related) {
	    if (rel.relation.equals(DigitalEntityRelation.part_of.toString()))
		links.add(rel.entity);
	}
	return links;
    }

    /**
     * @return the usage type is one more type
     */
    public String getUsageType() {
	return usageType;
    }

    /**
     * @param usageType
     *            set the usage type as you wish
     */
    public void setUsageType(String usageType) {
	this.usageType = usageType;
    }

    @Override
    public String toString() {
	return toString(this, 12, 0);
    }

    /**
     * @param digitalEntity
     *            the entity you want to convert to string
     * @param depth
     *            numbers of hierarchy
     * @param indent
     *            usually start with 0
     * @return a string representation of the digitalEntity
     */
    public String toString(DigitalEntity digitalEntity, int depth, int indent) {
	StringBuffer buffer = new StringBuffer();
	buffer.append(indent(indent));
	buffer.append(digitalEntity.getPid() + ", " + digitalEntity.getLabel()
		+ " , " + digitalEntity.getUsageType() + ",\n "
		+ indent(indent + 2) + digitalEntity.listStreams(indent + 2));
	if (depth != 0) {
	    for (RelatedDigitalEntity rel : digitalEntity.getRelated()) {
		buffer.append(rel.relation + ", "
			+ toString(rel.entity, depth - 1, indent + 1));
	    }
	}
	return buffer.toString();
    }

    private String listStreams(int indent) {
	StringBuilder sb = new StringBuilder();
	for (Entry<StreamType, Stream> e : streams.entrySet()) {
	    sb.append(e.getKey() + "," + e.getValue() + "\n" + indent(indent));
	}
	return sb.toString();
    }

    private String indent(int indent) {
	StringBuffer buffer = new StringBuffer();
	buffer.append("\n");
	for (int i = 0; i < indent; i++)
	    buffer.append("\t");
	return buffer.toString();
    }

    /**
     * Adds a identifier
     * 
     * @param id
     *            a identifier
     */
    public void addIdentifier(String id) {
	identifier.add(id);
    }

    /**
     * @return a list of transformers
     */
    public List<String> getTransformer() {
	return transformer;
    }

    /**
     * @param transformers
     *            a list of transformers
     */
    public void setTransformer(List<String> transformers) {
	this.transformer = transformers;
    }

    /**
     * @param transformer
     *            a single transformer
     */
    public void addTransformer(String transformer) {
	this.transformer.add(transformer);
    }

    /**
     * Removes a transformer from the collection
     * 
     * @param id
     *            id of a transformer
     */
    public void removeTransformer(String id) {
	Iterator<String> it = transformer.iterator();
	while (it.hasNext()) {
	    String curId = it.next();
	    if (id.equals(curId))
		it.remove();
	}
    }

    /**
     * @return the order attribute of the corresponding mets entry
     */
    public int getOrder() {
	return order;
    }

    /**
     * @param order
     *            the order attribute of the corresponding mets entry
     */
    public void setOrder(int order) {
	this.order = order;
    }

    /**
     * @param o
     *            the order attribute of the corresponding mets entry
     */
    public void setOrder(String o) {
	try {
	    order = Integer.parseInt(o);
	} catch (Exception e) {
	    logger.warn("Having problems with order of " + pid + " ", e);
	}
    }

    /**
     * @return createdBy
     */
    public String getCreatedBy() {
	return createdBy;
    }

    /**
     * @return importedFrom
     */
    public String getImportedFrom() {
	return importedFrom;
    }

    /**
     * @return legacyId
     */
    public String getLegacId() {
	return legacyId;
    }

    /**
     * @param legacyId
     */
    public void setLegacyId(String legacyId) {
	this.legacyId = legacyId;
    }

    /**
     * @param createdBy
     */
    public void setCreatedBy(String createdBy) {
	this.createdBy = createdBy;
    }

    /**
     * @param importedFrom
     */
    public void setImportedFrom(String importedFrom) {
	this.importedFrom = importedFrom;
    }

    /**
     * @return an id used for this object in a legacy system
     */
    public String getLegacyId() {
	return legacyId;
    }

}

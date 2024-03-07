/*
 *  Copyright 2023 OpenDCS Consortium
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opendcs.odcsapi.lrgsclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.opendcs.odcsapi.beans.ApiRawMessage;
import org.opendcs.odcsapi.beans.ApiRawMessageBlock;
import org.opendcs.odcsapi.util.ApiConstants;
import org.opendcs.odcsapi.util.ApiTextUtil;
import org.opendcs.odcsapi.xml.ElementIgnorer;
import org.opendcs.odcsapi.xml.PrintStreamErrorHandler;
import org.opendcs.odcsapi.xml.XmlHierarchyParser;
import org.opendcs.odcsapi.xml.XmlObjectParser;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class RawMessageBlockParser
	implements XmlObjectParser
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RawMessageBlockParser.class);
	private static final String module = "RawMessageParser";
	
	/** SAX parser object */
	private XMLReader parser;

	/** Manages hierarchy of parsers */
	private XmlHierarchyParser xhp;

	private ApiRawMessageBlock armb = null;

	public RawMessageBlockParser()
		throws ParserConfigurationException, SAXException
	{
		super();

        SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();
		parser = sp.getXMLReader();
		parser.setFeature("http://xml.org/sax/features/namespaces", true);

		ErrorHandler eh = new PrintStreamErrorHandler(LOGGER);
		xhp = new XmlHierarchyParser(eh);
		parser.setErrorHandler(eh);
		xhp.setErrorHandler(eh);
		parser.setContentHandler(xhp);
	}
	
	public ApiRawMessageBlock parse(byte[] data, int offset, int len, String inputName)
		throws IOException, SAXException
	{
		xhp.setFileName(inputName);
		armb = new ApiRawMessageBlock();
		ByteArrayInputStream bais = new ByteArrayInputStream(data, offset, len);
		
		xhp.pushObjectParser(this);
		parser.parse(new InputSource(bais));

		return armb;
	}

	/** No content characters expected -- only sub-elements. */
	public void characters(char[] ch, int start, int length)
		throws SAXException
	{
		if (!ApiTextUtil.isAllWhitespace(new String(ch, start, length)))
			throw new SAXException("No character data expected in MsgBlock data");
	}

	@Override
	public void startElement(XmlHierarchyParser hier, String namespaceURI, String localName, String qname,
			Attributes atts) throws SAXException
	{
		if (localName.equalsIgnoreCase("MsgBlock"))
		{
			// do nothing
		}
		else if (localName.equalsIgnoreCase("DcpMsg"))
		{
			ApiRawMessage msg = new ApiRawMessage();
			String platformId = getAttrIgnoreCase(atts, "platformId");
			if (platformId == null)
			{
				warning("DcpMsg element with no platformId -- skipped.");
				hier.pushObjectParser(new ElementIgnorer());
				return;
			}
			msg.setPlatformId(platformId);
			
			String s = getAttrIgnoreCase(atts, "flags");
			if (s == null)
			{
				warning("DcpMsg element with no flags.");
				msg.setFlags(1L);
			}
			else
			{
				if (s.toLowerCase().startsWith("0x"))
					s = s.substring(2);
				try { msg.setFlags(Long.parseLong(s, 16)); }
				catch(NumberFormatException ex)
				{
					warning("Invalid flags attribute for platformId '" + platformId 
						+ "' flag attr='" + s + "': msg skipped.");
					hier.pushObjectParser(new ElementIgnorer());
					return;
				}
			}
			
			hier.pushObjectParser(new RawMessageParser(armb, msg, this));
		}
		else
		{
			String msg = 
				"Unexpected tag '" + localName + "' at top level MsgBlock data.";
			warning(msg);
			throw new SAXException(msg);
		}
	}

	@Override
	public void endElement(XmlHierarchyParser hier, String namespaceURI, String localName, String qname)
			throws SAXException
	{
		if (localName.equalsIgnoreCase("MsgBlock"))
			hier.popObjectParser();
		else
			warning("Unexpected end element '" + localName + "'");
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
	{
	}
	
	public void warning(String msg)
	{
		LOGGER.warn("{} {}", module, msg);
	}

	public void info(String msg)
	{
		LOGGER.info("{} {}", module, msg);
	}

	public void debug(String msg)
	{
		LOGGER.debug("{} {}", module, msg);
	}
	
	public static String getAttrIgnoreCase( Attributes atts, String name )
	{
		int len = atts.getLength();
		for(int i=0; i<len; i++)
		{
			String nm = atts.getQName(i);
			if (nm.equalsIgnoreCase(name))
				return atts.getValue(i);
		}
		return null;
	}


}

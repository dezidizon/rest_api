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

/*
*  $Id: DdsProtocolError.java,v 1.1 2023/05/15 18:33:56 mmaloney Exp $
*/
package org.opendcs.odcsapi.lrgsclient;

/**
  ProtocolError is used by the various LRGS Servers and the corresponding
  client interfaces.  
  It either means that the message received could not be parsed (bad
  format) or that it was not the expected message type. 
*/
public class DdsProtocolError extends Exception
{
	/**
	  Constructor.
	  @param msg the message
	*/
	public DdsProtocolError(String msg)
	{
		super(msg);
	}

	public DdsProtocolError(String msg, Throwable throwable)
	{
		super(msg, throwable);
	}

	/** @return string representation of this exception. */
	public String toString()
	{
		return "Protocol Error: " + super.toString();
	}
}


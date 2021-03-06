/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jbpm;

import org.jbpm.jpdl.internal.activity.JpdlBinding;
import org.jbpm.jpdl.internal.model.JpdlProcessDefinition;
import org.jbpm.jpdl.internal.xml.JpdlParser;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.xml.Parse;
import org.w3c.dom.Element;

public class MuleReceiveBinding extends JpdlBinding
{
    public MuleReceiveBinding()
    {
        super("mule-receive");
    }

    public Object parseJpdl(Element element, Parse parse, JpdlParser parser)
    {
        MuleReceiveActivity activity;
        
        JpdlProcessDefinition processDefinition = parse.contextStackFind(JpdlProcessDefinition.class);        
        if (processDefinition.getInitial() == null) 
        {
            processDefinition.setInitial(parse.contextStackFind(ActivityImpl.class));          
            activity = new MuleReceiveActivity(true);
        } 
        else
        {
            activity = new MuleReceiveActivity(false);
        }

        // Note: The last method argument is the default value.
        activity.setVariableName(XmlUtil.attribute(element, "var", false, parse, null));
        activity.setEndpoint(XmlUtil.attribute(element, "endpoint", false, parse, null));
        activity.setPayloadClass(XmlUtil.attribute(element, "type", false, parse, null));

        return activity;
    }
}

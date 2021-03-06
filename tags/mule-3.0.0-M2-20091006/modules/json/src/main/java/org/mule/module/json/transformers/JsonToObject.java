/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.json.transformers;

import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.transformer.DiscoverableTransformer;
import org.mule.api.transformer.TransformerException;
import org.mule.config.i18n.CoreMessages;
import org.mule.message.DefaultMuleMessageDTO;
import org.mule.module.json.JsonData;
import org.mule.module.json.util.JsonUtils;
import org.mule.transformer.AbstractTransformer;
import org.mule.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ezmorph.bean.MorphDynaBean;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;

/**
 * A transformer that will convert a JSON encoded object graph to a java object. The object type is
 * determined by the 'returnClass' attribute. Note that this transformers supports Arrays and Lists. For
 * example, to
 * convert a JSON string to an array of org.foo.Person, set the the returnClass=[Lorg.foo.Person;.
 * <p/>
 * The JSON engine can be configured using the jsonConfig attribute. This is an object reference to an
 * instance of: {@link net.sf.json.JsonConfig}. This can be created as a spring bean.
 */
public class JsonToObject extends AbstractTransformer implements DiscoverableTransformer
{
    protected JsonConfig jsonConfig;

    protected Map dtoMappings;

    protected int weighting = DiscoverableTransformer.MAX_PRIORITY_WEIGHTING;

    public JsonToObject()
    {
        this.registerSourceType(JSONObject.class);
        this.registerSourceType(String.class);
        this.registerSourceType(InputStream.class);
        this.registerSourceType(byte[].class);
        setReturnClass(Object.class);
        dtoMappings = new HashMap(1);
        dtoMappings.put("payload", HashMap.class);
    }

    public int getPriorityWeighting()
    {
        return weighting;
    }

    public void setPriorityWeighting(int weighting)
    {
        this.weighting = weighting;
    }

    @Override
    public void initialise() throws InitialisationException
    {
        super.initialise();
        if (getReturnClass().equals(Object.class))
        {
            logger.warn("The return class is not set not type validation will be done");
        }
        if (getReturnClass().isArray())
        {
            getJsonConfig().setEnclosedType(getReturnClass());
            getJsonConfig().setArrayMode(JsonConfig.MODE_OBJECT_ARRAY);
        }
        else if (List.class.isAssignableFrom(getReturnClass()))
        {
            getJsonConfig().setEnclosedType(getReturnClass());
            getJsonConfig().setArrayMode(JsonConfig.MODE_LIST);
        }
        else if (Set.class.isAssignableFrom(getReturnClass()))
        {
            getJsonConfig().setEnclosedType(getReturnClass());
            getJsonConfig().setArrayMode(JsonConfig.MODE_SET);
        }
    }

    protected Object doTransform(Object src, String encoding) throws TransformerException
    {
        try
        {
            Object returnValue = null;

            if (src instanceof byte[])
            {
                src = new String((byte[]) src, encoding);
            }
            else if (src instanceof InputStream)
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copy((InputStream) src, baos);
                src = baos.toString();
            }

            if (src instanceof String)
            {
                if (getReturnClass().equals(JsonData.class))
                {
                    getJsonConfig().setEnclosedType(getReturnClass());
                    JSON json = JSONSerializer.toJSON(src.toString(), getJsonConfig());
                    if(json instanceof JSONArray)
                    {
                        getJsonConfig().setEnclosedType(List.class);
                        getJsonConfig().setArrayMode(JsonConfig.MODE_LIST);
                        List list = (List)JSONArray.toCollection((JSONArray)json, getJsonConfig());
                        returnValue = new JsonData(list);

                    }
                    else
                    {
                        returnValue = JSONObject.toBean((JSONObject)json, getJsonConfig());
                        returnValue = new JsonData((MorphDynaBean) returnValue);
                    }
                }
                else
                {
                    returnValue = JsonUtils.convertJsonToBean((String) src, getJsonConfig(), getReturnClass(),
                            (getReturnClass().equals(DefaultMuleMessageDTO.class) ? dtoMappings : null));
                }
            }
            else if (src instanceof JSONObject)
            {
                returnValue = JSONObject.toBean((JSONObject) src, getReturnClass(), new HashMap());
            }

            return returnValue;
        }
        catch (Exception e)
        {
            throw new TransformerException(CoreMessages.transformFailed("json", getReturnClass().getName()), this, e);
        }
        finally
        {
            if (src instanceof InputStream)
            {
                IOUtils.closeQuietly((InputStream) src);
            }
        }
    }


    public JsonConfig getJsonConfig()
    {
        if (jsonConfig == null)
        {
            setJsonConfig(new JsonConfig());
        }
        return jsonConfig;
    }

    public void setJsonConfig(JsonConfig jsonConfig)
    {
        this.jsonConfig = jsonConfig;
    }
}

/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.http;

import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.security.TlsDirectKeyStore;
import org.mule.umo.security.TlsDirectTrustStore;
import org.mule.umo.security.TlsIndirectKeyStore;
import org.mule.umo.security.provider.SecurityProviderFactory;
import org.mule.umo.security.tls.TlsConfiguration;
import org.mule.providers.ssl.SslSocketFactory;
import org.mule.providers.ssl.SslServerSocketFactory;

import java.io.IOException;
import java.security.Provider;
import java.net.ServerSocket;
import java.net.URI;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLServerSocket;

/**
 * <code>HttpsConnector</code> provides Https connectivity
 */
public class HttpsConnector extends HttpConnector 
implements TlsDirectKeyStore, TlsIndirectKeyStore, TlsDirectTrustStore
{
    private TlsConfiguration tls = new TlsConfiguration(TlsConfiguration.DEFAULT_KEYSTORE);

    public HttpsConnector()
    {
        setSocketFactory(new SslSocketFactory(tls));
        setServerSocketFactory(new SslServerSocketFactory(tls));
        // setting this true causes problems as socket closes before handshake finishes
        setCheckConnection(false);
    }

    // @Override
    protected ServerSocket getServerSocket(URI uri) throws IOException
    {
        SSLServerSocket serverSocket = (SSLServerSocket) super.getServerSocket(uri);
        serverSocket.setNeedClientAuth(isRequireClientAuthentication());
        return serverSocket;
    }

    protected void doInitialise() throws InitialisationException
    {
        tls.initialise(false, TlsConfiguration.JSSE_NAMESPACE);
        super.doInitialise();
    }

    public String getProtocol()
    {
        return "https";
    }

    public String getClientKeyStore()
    {
        return tls.getClientKeyStore();
    }

    public String getClientKeyStorePassword()
    {
        return tls.getClientKeyStorePassword();
    }

    public String getClientKeyStoreType()
    {
        return this.tls.getClientKeyStoreType();
    }

    public String getKeyManagerAlgorithm()
    {
        return tls.getKeyManagerAlgorithm();
    }

    public KeyManagerFactory getKeyManagerFactory()
    {
        return tls.getKeyManagerFactory();
    }

    public String getKeyPassword()
    {
        return tls.getKeyPassword();
    }

    public String getKeyStore()
    {
        return tls.getKeyStore();
    }

    public String getKeystoreType()
    {
        return tls.getKeystoreType();
    }

    public String getProtocolHandler()
    {
        return tls.getProtocolHandler();
    }

    public Provider getProvider()
    {
        return tls.getProvider();
    }

    public SecurityProviderFactory getSecurityProviderFactory()
    {
        return tls.getSecurityProviderFactory();
    }

    public String getSslType()
    {
        return tls.getSslType();
    }

    public String getStorePassword()
    {
        return tls.getStorePassword();
    }

    public String getTrustManagerAlgorithm()
    {
        return tls.getTrustManagerAlgorithm();
    }

    public TrustManagerFactory getTrustManagerFactory()
    {
        return tls.getTrustManagerFactory();
    }

    public String getTrustStore()
    {
        return tls.getTrustStore();
    }

    public String getTrustStorePassword()
    {
        return tls.getTrustStorePassword();
    }

    public String getTrustStoreType()
    {
        return tls.getTrustStoreType();
    }

    public boolean isExplicitTrustStoreOnly()
    {
        return tls.isExplicitTrustStoreOnly();
    }

    public boolean isRequireClientAuthentication()
    {
        return tls.isRequireClientAuthentication();
    }

    public void setClientKeyStore(String clientKeyStore) throws IOException
    {
        tls.setClientKeyStore(clientKeyStore);
    }

    public void setClientKeyStorePassword(String clientKeyStorePassword)
    {
        tls.setClientKeyStorePassword(clientKeyStorePassword);
    }

    public void setClientKeyStoreType(String clientKeyStoreType)
    {
        this.tls.setClientKeyStoreType(clientKeyStoreType);
    }

    public void setExplicitTrustStoreOnly(boolean explicitTrustStoreOnly)
    {
        tls.setExplicitTrustStoreOnly(explicitTrustStoreOnly);
    }

    public void setKeyManagerAlgorithm(String keyManagerAlgorithm)
    {
        tls.setKeyManagerAlgorithm(keyManagerAlgorithm);
    }

    public void setKeyPassword(String keyPassword)
    {
        tls.setKeyPassword(keyPassword);
    }

    public void setKeyStore(String keyStore) throws IOException
    {
        tls.setKeyStore(keyStore);
    }

    public void setKeystoreType(String keystoreType)
    {
        tls.setKeystoreType(keystoreType);
    }

    public void setProtocolHandler(String protocolHandler)
    {
        tls.setProtocolHandler(protocolHandler);
    }

    public void setProvider(Provider provider)
    {
        tls.setProvider(provider);
    }

    public void setRequireClientAuthentication(boolean requireClientAuthentication)
    {
        tls.setRequireClientAuthentication(requireClientAuthentication);
    }

    public void setSecurityProviderFactory(SecurityProviderFactory spFactory)
    {
        tls.setSecurityProviderFactory(spFactory);
    }

    public void setSslType(String sslType)
    {
        tls.setSslType(sslType);
    }

    public void setStorePassword(String storePassword)
    {
        tls.setStorePassword(storePassword);
    }

    public void setTrustManagerAlgorithm(String trustManagerAlgorithm)
    {
        tls.setTrustManagerAlgorithm(trustManagerAlgorithm);
    }

    public void setTrustManagerFactory(TrustManagerFactory trustManagerFactory)
    {
        tls.setTrustManagerFactory(trustManagerFactory);
    }

    public void setTrustStore(String trustStore) throws IOException
    {
        tls.setTrustStore(trustStore);
    }

    public void setTrustStorePassword(String trustStorePassword)
    {
        tls.setTrustStorePassword(trustStorePassword);
    }

    public void setTrustStoreType(String trustStoreType)
    {
        tls.setTrustStoreType(trustStoreType);
    }

}

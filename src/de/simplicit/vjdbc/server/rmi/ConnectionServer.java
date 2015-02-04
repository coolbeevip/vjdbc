// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.rmi;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.simplicit.vjdbc.rmi.SecureSocketFactory;
import de.simplicit.vjdbc.server.config.RmiConfiguration;
import de.simplicit.vjdbc.server.config.VJdbcConfiguration;

public class ConnectionServer {
	private static Log _logger = LogFactory.getLog(ConnectionServer.class);
	private RmiConfiguration _rmiConfiguration;
	private Registry _registry;
	private String hostname;
	private String clientport;
	public static void main(String[] args) {
		try {
			if (args.length == 1) {
				VJdbcConfiguration.init(args[0]);
			} else if (args.length == 2) {
				Properties props = new Properties();
				FileInputStream propsInputStream = null;
				try {
					propsInputStream = new FileInputStream(args[1]);
					props.load(propsInputStream);
					VJdbcConfiguration.init(args[0], props);
				} finally {
					if (propsInputStream != null)
						propsInputStream.close();
				}
			} else {
				throw new RuntimeException("You must specify a configuration file as the first parameter");
			}

			ConnectionServer connectionServer = new ConnectionServer();
			connectionServer.serve();
		} catch (Throwable e) {
			_logger.error(e.getMessage(), e);
		}
	}

	public void serve() throws IOException {
		this.hostname = System.getProperty("java.rmi.server.hostname");
		this.clientport = System.getProperty("java.rmi.client.port");
		_logger.info("bind hostname " + hostname);
		_logger.info("bind client port " + clientport);
		this._rmiConfiguration = VJdbcConfiguration.singleton().getRmiConfiguration();

		if (this._rmiConfiguration == null) {
			_logger.debug("No RMI-Configuration specified in VJdbc-Configuration, using default configuration");
			this._rmiConfiguration = new RmiConfiguration();
		}

		if (this._rmiConfiguration.isUseSSL()) {
			_logger.info("Using SSL sockets for communication");
			RMISocketFactory.setSocketFactory(new SecureSocketFactory());
		}

		if (this._rmiConfiguration.isCreateRegistry()) {
			_logger.info("Starting RMI-Registry on port " + this._rmiConfiguration.getPort());
			this._registry = LocateRegistry.createRegistry(this._rmiConfiguration.getPort(), new RMIClientSocketFactory() {
				public Socket createSocket(String host, int port) throws IOException {
					if(port == 0 && clientport!=null){
						port = Integer.valueOf(clientport);
					}
					System.out.println("RMIServerSocketFactory().createSocket()");
					InetAddress addr = InetAddress.getByName(host);
					if (addr.equals(InetAddress.getLocalHost())) {
						return new Socket(addr, port);
					} else {
						throw new IOException("remote socket bind forbidden.");
					}
				}
			}, new RMIServerSocketFactory() {
				public ServerSocket createServerSocket(int port) throws IOException {
					// String hostname =
					// System.getProperty("java.rmi.server.hostname");
					System.out.println("RMIServerSocketFactory().createServerSocket()");
					return new ServerSocket(port, 0, InetAddress.getByName(hostname));
				}
			});
		} else {
			_logger.info("Using RMI-Registry on port " + this._rmiConfiguration.getPort());
			this._registry = LocateRegistry.getRegistry(hostname, this._rmiConfiguration.getPort());
		}

		installShutdownHook();

		_logger.info("Binding remote object to '" + this._rmiConfiguration.getObjectName() + "'");
		this._registry.rebind(this._rmiConfiguration.getObjectName(), new ConnectionBrokerRmiImpl(this._rmiConfiguration.getRemotingPort()));
	}

	private void installShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				try {
					ConnectionServer._logger.info("Unbinding remote object");
					ConnectionServer.this._registry.unbind(ConnectionServer.this._rmiConfiguration.getObjectName());
				} catch (RemoteException e) {
					ConnectionServer._logger.error("Remote exception", e);
				} catch (NotBoundException e) {
					ConnectionServer._logger.error("Not bound exception", e);
				}
			}
		}));
	}
}

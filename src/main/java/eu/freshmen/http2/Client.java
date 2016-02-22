/*
 * Inspired by <a href="https://github.com/grro/http2">github.com/grro/http2</a>
 * 
 * Copyright 2016 Jan Ondrusek
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package eu.freshmen.http2;

import java.net.InetSocketAddress;

import org.eclipse.jetty.http.HostPortHttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.FuturePromise;
import org.eclipse.jetty.util.Promise;

import eu.freshmen.http2.Launcher.Args;

public class Client {

	// create a low-level Jetty HTTP/2 client
	private final HTTP2Client client = new HTTP2Client();
	private final InetSocketAddress address;

	public Client(InetSocketAddress address) throws Exception {
		this.address = address;
		client.start();
	}

	public static void run(Args params) throws Exception {
		final Client client = new Client(new InetSocketAddress(params.getHost(), params.getPort()));
		try {
			final FuturePromise<Session> connect = client.connect();
			client.request(connect);
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			client.stop();
		}
	}

	public FuturePromise<Session> connect() {
		// create a new session the represents a (multiplexed) connection to the server
		final FuturePromise<Session> sessionFuture = new FuturePromise<>();
		final Session.Listener.Adapter adapter = new Session.Listener.Adapter();
		client.connect(address, adapter, sessionFuture);
		return sessionFuture;
	}

	public void request(FuturePromise<Session> sessionPromise) throws Exception {
		// create the header frame
		final HeadersFrame head = frame(1, "/head");
		final HeadersFrame body = frame(2, "/body");

		final Session session = sessionPromise.get();
		session.newStream(head, new Promise.Adapter<Stream>(), new PrintingFramesHandler());
		session.newStream(body, new Promise.Adapter<Stream>(), new PrintingFramesHandler());
	}

	private HeadersFrame frame(int id, String path) {
		final String hostport = address.getHostName() + ":" + address.getPort();
		MetaData.Request metaData = new MetaData.Request("GET", HttpScheme.HTTP, new HostPortHttpField(hostport), path,
				HttpVersion.HTTP_2, new HttpFields());
		final HeadersFrame frame = new HeadersFrame(id, metaData, null, true);
		return frame;
	}

	public void stop() throws Exception {
		client.stop();
	}

	static class PrintingFramesHandler extends Stream.Listener.Adapter {

		@Override
		public void onHeaders(Stream stream, HeadersFrame frame) {
			System.out.println("[" + stream.getId() + "] HEADERS " + frame.getMetaData().toString());
		}

		@Override
		public void onData(Stream stream, DataFrame frame, Callback callback) {
			byte[] bytes = new byte[frame.getData().remaining()];
			frame.getData().get(bytes);
			System.out.println("[" + stream.getId() + "] DATA " + new String(bytes));
			callback.succeeded();
		}
	}
}

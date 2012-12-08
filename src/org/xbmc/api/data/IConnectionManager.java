package org.xbmc.api.data;

import org.xbmc.android.jsonrpc.api.AbstractCall;
import org.xbmc.android.jsonrpc.io.ApiCallback;
import org.xbmc.android.jsonrpc.io.ConnectionManager;

public interface IConnectionManager {

	public <T> ConnectionManager call(AbstractCall<T> call, ApiCallback<T> callback);
}

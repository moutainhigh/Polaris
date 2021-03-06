package com.polaris.config.zk;

import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import com.polaris.core.config.ConfClient;
import com.polaris.core.config.ConfHandlerListener;
import com.polaris.core.util.StringUtil;

public class ConfZkClient {
	private static int sessionTimeoutMs = Integer.parseInt(ConfClient.get("config.registry.zk.sessionTimeoutMs", "20000"));
	private static ZooKeeper zk = ZkClient.getInstance(ConfClient.getConfigRegistryAddress(), sessionTimeoutMs);
	
	public static String getConfig(String fileName, String group) {
		String path = getPath(fileName,group);
		ZkClient.createWithParent(zk,path,CreateMode.PERSISTENT);//创建路径
		return ZkClient.getPathData(zk,path);//获取数据
	}
	public static void addListener(String fileName, String group, ConfHandlerListener listener) {
		String path = getPath(fileName,group);
		ZkClient.addWatchForPath(zk, path, new ZkListener() {
			@Override
			public void listen(String path, EventType type) {
				if (type == EventType.NodeDeleted) {
					listener.receive(null);
				} else if (type == EventType.NodeDataChanged || type == EventType.NodeCreated) {
					listener.receive(ZkClient.getPathData(zk,path));
				}
			}
			
		});
	}
	
	private static String getPath(String fileName, String group) {
		StringBuilder groupSb = new StringBuilder();
		
		//rootPath
		groupSb.append(ConfClient.get("config.zk.root.path",Constant.CONF_ROOT_PATH));
		groupSb.append(Constant.SLASH);

		//namespace
		String nameSpace = ConfClient.getNameSpace();
		if (StringUtil.isNotEmpty(nameSpace)) {
			groupSb.append(nameSpace);
			groupSb.append(Constant.SLASH);
		}
		
		//cluster
		if (StringUtil.isNotEmpty(ConfClient.getGroup())) {
			groupSb.append(ConfClient.getGroup());
			groupSb.append(Constant.SLASH);
		}
		
		//service
		groupSb.append(group);
		groupSb.append(Constant.SLASH);
		
		//fileName
		groupSb.append(fileName);
		return groupSb.toString();
	}

}
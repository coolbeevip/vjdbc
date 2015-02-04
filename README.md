# 虚拟JDBC，可作JDBC代理服务

基于VJDBC，增加了可定义server rmi address 和 client rmi port

当服务器上存在多ip时，可以使用 -Djava.rmi.server.hostname=X.X.X.X 的方式绑定服务器ip
存在防火墙时可以使用 -Djava.rmi.client.port=XX 的方式指定客户端连接端口

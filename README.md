# jmx-caller
This is a simple utility to work with JMX from command line interface.

###Requirements:
- jshell from jdk is required.
- management agent have to be started. Can be started on running JVM with `jcmd PID ManagementAgent.start_local`

####Usage:
- `jshell -R-Dpid=PID -R-Dcommand=getBeans calljmx.jsh`
- `jshell -R-Dpid=PID -R-Dcommand=getBeanInfo -R-Dbean=java.lang:type=Memory calljmx.jsh`
- `jshell -R-Dpid=PID -R-Dcommand=getAttribute -R-Dbean=java.lang:type=Memory -R-Dmethod=HeapMemoryUsage calljmx.jsh`
- `jshell -R-Dpid=PID -R-Dcommand=invoke -R-Dbean=java.lang:type=Memory -R-Dmethod=gc calljmx.jsh`

####Available options:
- `-R-Dpid=PID` - java process id you want to attach
- `-R-Dcommand=help|getBeans|getBeanInfo|getAttribute|invoke` - specify what to do
- `-R-Dbean=BEAN_PATH` - full path to mbean object like: java.lang:type=Memory
- `-R-Dmethod=METHOD_NAME` - attribute or operation name
- `-R-Dargs=ARGUMENTS` - additional method arguments separated by space
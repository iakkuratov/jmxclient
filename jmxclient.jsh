import com.sun.tools.attach.VirtualMachine;

import javax.management.*;
import javax.management.remote.*;
import java.rmi.AccessException;
import java.util.Arrays;

{
  final String help = "This is a simple utility to work with JMX from command line interface.\n" +
    "Usage:\n" +
    "  jshell -R-Dpid=PID -R-Dcommand=getBeans jmxclient.jsh\n" +
    "  jshell -R-Dpid=PID -R-Dcommand=getBeanInfo -R-Dbean=java.lang:type=Memory jmxclient.jsh\n" +
    "  jshell -R-Dpid=PID -R-Dcommand=getAttribute -R-Dbean=java.lang:type=Memory -R-Dmethod=HeapMemoryUsage jmxclient.jsh\n" +
    "  jshell -R-Dpid=PID -R-Dcommand=invoke -R-Dbean=java.lang:type=Memory -R-Dmethod=gc jmxclient.jsh\n" +
    "Available options:\n" +
    "  -R-Dpid=PID - process id you want attach to\n" +
    "  -R-Dcommand=help|getBeans|getBeanInfo|getAttribute|invoke - specify what to do\n" +
    "  -R-Dbean=BEAN_PATH - full path to mbean object like: java.lang:type=Memory\n" +
    "  -R-Dmethod=METHOD_NAME - attribute or operation name\n" +
    "  -R-Dargs=ARGUMENTS - additional method arguments separated by space\n";

  String pid = System.getProperty("pid");
  String cmd = System.getProperty("command", "help");
  ObjectName mbeanName = System.getProperty("bean") != null ? new ObjectName(System.getProperty("bean")) : null;
  String mtd = System.getProperty("method", null);
  String arg = System.getProperty("args", null);

  if (pid == null || pid.isEmpty())
    throw new IllegalArgumentException("Pid have to be specified\n" + help);

  VirtualMachine vm = VirtualMachine.attach(pid);
  String connectorAddr = vm.getAgentProperties().getProperty("com.sun.management.jmxremote.localConnectorAddress");
  vm.detach();

  if (connectorAddr == null)
    throw new AccessException("RMI is off. You can turn it on using: jcmd " + pid + " ManagementAgent.start_local" );

  final JMXServiceURL jmxUrl = new JMXServiceURL(connectorAddr);

  final JMXConnector connector = JMXConnectorFactory.connect(jmxUrl);
  connector.connect();

  final MBeanServerConnection conn = connector.getMBeanServerConnection();

  switch (cmd){
    case "getBeans":
      conn.queryNames(mbeanName, null).forEach(System.out::println);
      break;
    case "getBeanInfo":
      if (mbeanName == null)
          throw new IllegalArgumentException("No bean name specified for getBeanInfo\n" + help);
      MBeanInfo info = conn.getMBeanInfo(mbeanName);
      System.out.println("Bean info:\n" + "  attributes:");
      Arrays.stream(info.getAttributes()).forEach(a->System.out.println("    " + a.getName()));
      System.out.println("  operations:");
      Arrays.stream(info.getOperations()).forEach(a->System.out.println("    " + a.getName()));
      break;
    case "invoke":
      String[] params = null;
      if (mbeanName == null || mtd == null)
          throw new IllegalArgumentException("You have to specify bean and method at least\n" + help);
      if (arg != null && !arg.isEmpty())
        params = arg.split(" ");

      System.out.println("Invoked successfully: " + conn.invoke(mbeanName, mtd, params,null));
      break;
    case "getAttribute":
      if (mbeanName == null || mtd.isEmpty())
          throw new IllegalArgumentException("You have to specify bean and method at least\n" + help);
      System.out.println(conn.getAttribute(mbeanName, mtd));
      break;
    case "help":
      System.out.println(help);
      break;
    default:
      throw new IllegalStateException("Unexpected value: " + mtd + "\n" + help);
  }
}
/exit

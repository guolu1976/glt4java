package monitor;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import glt.NIO.TcpServer;



/**
 * Servlet implementation class SocketServer
 */
@WebServlet(
		urlPatterns = { "/SocketServer" }, 
		initParams = { 
				@WebInitParam(name = "port", value = "9000", description = "�����˿�")
		})
public class MonitorServer extends GenericServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see GenericServlet#GenericServlet()
     */
    public MonitorServer() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#service(ServletRequest request, ServletResponse response)
	 */
	public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	private TcpServer _server; 
	
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		if(_server!=null)
			return;
		
		try {
			String port = config.getInitParameter("port");
			
			_server = new TcpServer();
	
			_server.open(Integer.parseInt(port));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void destory()
	{
		System.out.println("destory");
	}
}

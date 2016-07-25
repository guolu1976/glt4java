package monitor;
import com.opensymphony.xwork2.ActionSupport;

public class hello extends ActionSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public String execute() throws Exception {

       System.out.println("Actionִ helloworld");

       return SUCCESS;
    }
}

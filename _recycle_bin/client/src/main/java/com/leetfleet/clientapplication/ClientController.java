
// Importing required classes
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;


@Controller
public class ClientController {
	@CrossOrigin(origins="akka-http/10.4.0")
	@RequestMapping("/")
	public String homepage() {
		return "homepage";
	}

}
package prototyp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import prototyp.model.UserRepository;

/**
* <h1>AdminManagementController</h1>
* The AdminManagementController is used to modify UserAccounts or User with administrative rights.
* 
*
* @author Friederike Kitzing
* 
*/

@Controller
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

private final UserRepository userRepository;

	/**
	 * Autowire.
	 * @param userRepository The repository for the users
	 */
	@Autowired
	public AdminController(UserRepository userRepository){
		this.userRepository=userRepository;
	}
	
	@RequestMapping("/modify")
	public String modify(){
		
		return "modify";
	}
}

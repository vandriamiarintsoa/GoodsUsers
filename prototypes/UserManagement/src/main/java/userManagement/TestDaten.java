package userManagement;

import org.salespointframework.core.DataInitializer;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import userManagement.model.Address;
import userManagement.model.User;
import userManagement.model.UserRepository;



@Component
public class TestDaten implements DataInitializer {

	private final UserAccountManager userAccountManager;
	private final UserRepository userRepository;

	@Autowired
	public TestDaten (UserAccountManager userAccountManager, UserRepository userRepository){
		
		Assert.notNull(userAccountManager, "UserAccountManager must not be null!");
		Assert.notNull(userRepository, "UserRepository must not be null!");
		this.userAccountManager=userAccountManager;
		this.userRepository=userRepository;
	}
	
	@Override
	public void initialize() {

		initializeUsers(userAccountManager, userRepository);

	}
	private void initializeUsers(UserAccountManager userAccountManager, UserRepository userRepository){
		
		if (userAccountManager.findByUsername("boss").isPresent()) {
			return;
		}

		UserAccount bossAccount = userAccountManager.create("admin", "123", new Role("ROLE_ADMIN"));
		userAccountManager.save(bossAccount);
		
		Role normalUserRole = new Role("ROLE_NORMAL");
		UserAccount u1=userAccountManager.create("Lisa", "pw", normalUserRole);
		u1.setFirstname("Lisa-Marie");
		u1.setLastname("Maier");
		u1.setEmail("Maier@gmail.com");
		userAccountManager.save(u1);
		UserAccount u2=userAccountManager.create("Peter", "pw", normalUserRole);
		u2.setFirstname("Peter");
		u2.setLastname("U.");

		userAccountManager.save(u2);
		
		Address address1=new Address("Mittelstraße 1", "11587","Dresden");
		
		User user1= new User(address1, "D", u1);
		user1.setLanguage("Deutsch");
		user1.setOrigin("Deutschland");
		User user2= new User(address1, "D", u2);
		user2.setLanguage("Arabisch");
		userRepository.save(user1);
		userRepository.save(user2);
	}
}
	

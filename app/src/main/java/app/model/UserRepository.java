package app.model;

import org.salespointframework.useraccount.UserAccount;
import org.springframework.data.repository.CrudRepository;

/**
 * <h1>UserRepository</h1> The UserRepository is a Spring-repository in which
 * all registered Users are saved.
 * 
 * @author Friederike Kitzing
 */

public interface UserRepository extends CrudRepository<User, Long> {
	/**
	 * This method finds the User connected to a certain UserAccount.
	 * 
	 * @param UserAccount
	 * @return User The one User the given UserAccount is assigned to.
	 */
	User findByUserAccount(UserAccount userAccount);

	/**
	 * This method finds the User to a certain Id.
	 * 
	 * @param lond
	 *            Id
	 * @return User The one User the given Id
	 */
	User findById(long Id);
}
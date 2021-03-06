package app.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManager;
import org.salespointframework.useraccount.web.LoggedIn;
import org.salespointframework.useraccount.AuthenticationManager;
import org.salespointframework.useraccount.Password;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import app.model.Address;
import app.model.Language;
import app.model.User;
import app.model.User.AddresstypEnum;
import app.model.UserRepository;
import app.repository.LanguageRepository;

/**
 * <h1>UserManagementController</h1> The UserManagementController is used to
 * find, show and modify a certain registered User.
 * 
 *
 * @author Friederike Kitzing
 * 
 */
@Controller
public class UserManagementController {

	private final UserRepository userRepository;
	private final UserAccountManager userAccountManager;
	private final LanguageRepository languageRepository;
	private final AuthenticationManager authenticationManager;

	@Autowired
	public UserManagementController(UserRepository userRepository, UserAccountManager userAccountManager,
			LanguageRepository languageRepository, AuthenticationManager authenticationManager) {
		this.userRepository = userRepository;
		this.userAccountManager = userAccountManager;
		this.languageRepository = languageRepository;
		this.authenticationManager = authenticationManager;
	}

	/**
	 * This method is the answer for the request to '/userDetails'. It finds and
	 * retrieves all the user in the UserRepository.
	 * 
	 * @param ModelMap
	 *            The modelmap to add the user
	 * @return String The name of the view to be shown after processing
	 */
	/*
	 * @RequestMapping("/userDetails") String userDetails(ModelMap map) {
	 * map.addAttribute("userDetails", userRepository.findAll()); return
	 * "userDetails"; }
	 */

	/**
	 * This method is the answer for the request to '/data'. It finds the user
	 * that is connected to the logged-in UserAccount in the UserRepository. Is
	 * UserAccount found the method redirects to 'noUser'.
	 * 
	 * @param Model
	 *            The model to add the present User
	 * @return String The name of the view to be shown after processing
	 */
	@RequestMapping("/data")
	String data(Model model, @LoggedIn Optional<UserAccount> userAccount) {
		if (userAccount.isPresent()) {
			User LoggUser = userRepository.findByUserAccount(userAccount.get());
			model.addAttribute("user", LoggUser);
			if (LoggUser.getAddresstypString().equals("Wohnung")) {
				return "data";
			}
			return "data_refugee";
		}

		return "noUser";
	}

	/**
	 * This method is the answer for the request to '/changePassword/{user}'. It
	 * adds the userAccount of the loggedIn User or (if the admin is present)
	 * the userAccount with the name off the given path parameter
	 * 
	 * @param Model
	 *            The model to add the present User
	 * @param Optional<UserAccount>
	 *            The loggedIn userAccount
	 * @return String The name of the view to be shown after processing
	 */
	@RequestMapping("/changePassword/{user}")
	public String changePassword(@PathVariable final String user, Model model,
			@LoggedIn Optional<UserAccount> userAccount) {
		if (userAccount.isPresent() && userAccount.get().hasRole(new Role("ROLE_NORMAL")))
			model.addAttribute("userAccount", userAccount.get());
		if (userAccount.isPresent() && userAccount.get().hasRole(new Role("ROLE_ADMIN")))
			model.addAttribute("userAccount", userAccountManager.findByUsername(user).get());
		System.out.println(model.toString());
		return "changePassword";
	}

	/**
	 * This method is the answer for the request to
	 * '/changePassword_submit/{user}'. It changes the password if the old
	 * password is correct, both new passwords are equal and the new password is
	 * valid.
	 * 
	 * @param Optional<UserAccount>
	 *            The loggedIn userAccount
	 * @return String The name of the view to be shown after processing
	 */
	@RequestMapping(value = "/changePassword_submit/{user}", method = RequestMethod.POST)
	public String changePassword_submit(@PathVariable final String user, @LoggedIn Optional<UserAccount> userAccount,
			@RequestParam("actualPassword") final String ActualPassword,
			@RequestParam("newPassword1") final String NewPassword1,
			@RequestParam("newPassword2") final String NewPassword2) {
		if (!userAccount.isPresent())
			return "noUser";
		User user_xyz;
		if (userAccount.get().hasRole(new Role("ROLE_ADMIN")))
			user_xyz = userRepository.findByUserAccount(userAccountManager.findByUsername(user).get());
		else
			user_xyz = userRepository.findByUserAccount(userAccount.get());

		if (ActualPassword != null && authenticationManager.matches(new Password(ActualPassword),
				user_xyz.getUserAccount().getPassword())) {
			if (NewPassword1.equals(NewPassword2) && (HelpFunctions.checkPasswordStrength(NewPassword1) != 0)) {
				userAccountManager.changePassword(user_xyz.getUserAccount(), NewPassword1);
				;
			} else {
				System.out.println("Fehler PW-Validation");
				return "redirect:/changePassword/{user}";
			}
		} else {
			System.out.println("Fehler altes Password");
			return "redirect:/changePassword/{user}";
		}
		userRepository.save(user_xyz);
		userAccountManager.save(user_xyz.getUserAccount());

		System.out.println("Passwort geÃ¤ndert");

		if (userAccount.get().hasRole(new Role("ROLE_ADMIN"))) {
			return "redirect:/userDetails";
		}
		return "redirect:/data";
	}

	/**
	 * This method is the answer for the request to '/modifyUserAccount/{user}'.
	 * It adds the userAccount of the loggedIn User.
	 * 
	 * @param Model
	 *            The model to add the present User
	 * @param Optional<UserAccount>
	 *            The loggedIn userAccount
	 * @return String The name of the view to be shown after processing
	 */
	@RequestMapping("/modifyUserAccount/{user}")
	public String modifyUserAccount(ModelMap model, @LoggedIn Optional<UserAccount> userAccount) {
		if (userAccount.isPresent()) {
			model.addAttribute("userAccount", userAccount.get());
		}
		return "modifyUserAccount";
	}

	/**
	 * This method is the answer for the request to
	 * '/modifyUserAccount_submit/{user}'. It changes the user information saved
	 * in the userAccount.
	 * 
	 * @param Optional<UserAccount>
	 *            The loggedIn userAccount
	 * @return String The name of the view to be shown after processing
	 */
	@RequestMapping(value = "/modifyUserAccount_submit/{user}", method = RequestMethod.POST)
	public String modifyUserAccount(@LoggedIn Optional<UserAccount> userAccount,
			@RequestParam(value = "firstname", required = false) final String Firstname,
			@RequestParam(value = "lastname", required = false) final String Lastname,
			@RequestParam(value = "email", required = false) final String Email) {
		if (!userAccount.isPresent())
			return "noUser";
		User user_xyz = userRepository.findByUserAccount(userAccount.get());

		if ((Firstname != null) && (!Firstname.equals("")))
			user_xyz.getUserAccount().setFirstname(Firstname);
		if ((Lastname != null) && (!Lastname.equals("")))
			user_xyz.getUserAccount().setLastname(Lastname);
		if ((Email != null) && (!Email.equals("")) && HelpFunctions.emailValidator(Email))
			user_xyz.getUserAccount().setEmail(Email);

		userAccountManager.save(user_xyz.getUserAccount());
		userRepository.save(user_xyz);
		return "redirect:/data";
	}

	/**
	 * This method is the answer for the request to '/modifyAddress'. It adds
	 * the userAccount of the loggedIn User.
	 * 
	 * @param Model
	 *            The model to add the present User
	 * @param Optional<UserAccount>
	 *            The loggedIn userAccount
	 * @return String The name of the view to be shown after processing
	 */
	@RequestMapping("/modifyAddress")
	public String modifyAddress(Model model, @LoggedIn Optional<UserAccount> userAccount) {
		if (userAccount.isPresent()) {
			model.addAttribute("user", userRepository.findByUserAccount(userAccount.get()));
			if (userRepository.findByUserAccount(userAccount.get()).getAddresstypString().equals("Refugees_home")) {
				model.addAttribute("isRefugee", "refugee");
			}
		}
		return "modifyAddress";
	}

	/**
	 * This method is the answer for the request to '/modifyAddress_submit'. It
	 * changes the address data of the loggedIn User.
	 * 
	 * @param Optional<UserAccount>
	 *            The loggedIn userAccount
	 * @return String The name of the view to be shown after processing
	 */
	@RequestMapping(value = "/modifyAddress_submit", method = RequestMethod.POST)
	public String modifyAddress_submit(@LoggedIn Optional<UserAccount> userAccount,
			@RequestParam("wohnen") final String Adresstyp,
			@RequestParam("flh_name") final Optional<String> Flh_name_OPT,
			@RequestParam("citypart") final Optional<String> Citypart_OPT,
			@RequestParam("street") final Optional<String> Street_OPT,
			@RequestParam("housenr") final Optional<String> Housenr_OPT,
			@RequestParam("postcode_R") final Optional<String> Postcode_R,
			@RequestParam("city_R") final Optional<String> City_R,
			@RequestParam("postcode_H") final Optional<String> Postcode_H,
			@RequestParam("city_H") final Optional<String> City_H) {
		if (!userAccount.isPresent())
			return "noUser";
		User user_xyz = userRepository.findByUserAccount(userAccount.get());
		Address lastAddress = user_xyz.getLocation();
		if (Adresstyp.equals("refugee")) {
			System.out.println("refugee");
			user_xyz.getLocation().setStreet("");
			user_xyz.getLocation().setHousenr("");
			user_xyz.setAddresstyp(AddresstypEnum.Refugees_home);
			if (Flh_name_OPT.isPresent() && !Flh_name_OPT.get().isEmpty()) {
				user_xyz.getLocation().setFlh_name(Flh_name_OPT.get());
			}
			if ((Postcode_R.isPresent() && !Postcode_R.get().isEmpty()) && (Postcode_R.get().matches("[0-9]{5}"))) {
				user_xyz.getLocation().setZipCode(Postcode_R.get());
			}
			if (City_R.isPresent() && !City_R.get().isEmpty()) {
				user_xyz.getLocation().setCity(City_R.get());
			}
			if (Citypart_OPT.isPresent() && !Citypart_OPT.get().isEmpty()) {
				user_xyz.getLocation().setCityPart(Citypart_OPT.get());
			}
		} else {
			System.out.println("helper");
			if (Street_OPT.isPresent() && !Street_OPT.get().isEmpty()) {
				user_xyz.getLocation().setStreet(Street_OPT.get());
				user_xyz.setAddresstyp(AddresstypEnum.Wohnung);
				userRepository.save(user_xyz);
			}
			if (Housenr_OPT.isPresent() && !Housenr_OPT.get().isEmpty()) {
				user_xyz.getLocation().setHousenr(Housenr_OPT.get());
				user_xyz.setAddresstyp(AddresstypEnum.Wohnung);
				userRepository.save(user_xyz);
			}
			if ((Postcode_H.isPresent()) && !Postcode_H.get().isEmpty() && (Postcode_H.get().matches("[0-9]{5}")
					&& userRepository.findOne(user_xyz.getId()).getAddresstypString().equals("Wohnung"))) {
				user_xyz.getLocation().setZipCode(Postcode_H.get());
			}
			if (City_H.isPresent() && !City_H.get().isEmpty()
					&& userRepository.findOne(user_xyz.getId()).getAddresstypString().equals("Wohnung")) {
				user_xyz.getLocation().setCity(City_H.get());
			}
			if (user_xyz.getAddresstypString().equals("Wohnung")) {
				user_xyz.getLocation().setFlh_name("");
				user_xyz.getLocation().setCityPart("");
			}
		}
		userRepository.save(user_xyz);
		userRepository.save(user_xyz);
		if (!user_xyz.isOldLocation(lastAddress)) {
			user_xyz.setCoordinates(user_xyz.createCoordinates());
			userRepository.save(user_xyz);
		}
		return "redirect:/data";
	}

	/**
	 * This method is the answer for the request to '/modifyLanguages'. It adds
	 * all the necessary attributs for the modifyLanguages-template
	 * 
	 * @param Model
	 *            The model to add the present User and his languages
	 * @param Optional<UserAccount>
	 *            The loggedIn userAccount
	 * @return String The name of the view to be shown after processing
	 */
	@RequestMapping("/modifyLanguages")
	public String modifyLanguages(ModelMap model, @LoggedIn Optional<UserAccount> userAccount) {
		User user_xyz;
		if (userAccount.isPresent()) {
			user_xyz = userRepository.findByUserAccount(userAccount.get());
			model.addAttribute("user", user_xyz);
		} else {
			return "noUser";
		}
		model.addAttribute("languages", languageRepository.findAll());
		List<String> otherLanguages = new ArrayList<>();
		for (Language language : user_xyz.getLanguages()) {
			otherLanguages.add(language.getkennung());
		}
		otherLanguages.remove(user_xyz.getPrefLanguage().getkennung());
		if (!otherLanguages.isEmpty()) {
			model.addAttribute("language2", otherLanguages.get(0));
			otherLanguages.remove(0);
			if (!otherLanguages.isEmpty()) {
				model.addAttribute("language3", otherLanguages.get(0));
				otherLanguages.remove(0);
			}
		}
		return "modifyLanguages";
	}

	/**
	 * This method is the answer for the request to '/modifyLanguages_submit'.
	 * It changes the users chosen languages
	 * 
	 * @param Optional<UserAccount>
	 *            The loggedIn userAccount
	 * @return String The name of the view to be shown after processing
	 */
	@RequestMapping(value = "/modifyLanguages_submit", method = RequestMethod.POST)
	public String modify(@LoggedIn Optional<UserAccount> userAccount,
			@RequestParam(value = "nativelanguage") final String Nativelanguage,
			@RequestParam(value = "otherlanguages") final String OtherLanguages) {
		if (!userAccount.isPresent())
			return "noUser";
		User user_xyz = userRepository.findByUserAccount(userAccount.get());

		if (!Nativelanguage.isEmpty()) {
			user_xyz.removeLanguage(user_xyz.getPrefLanguage());
			Language PreferredLanguage = languageRepository.findByKennung(Nativelanguage);
			user_xyz.setPrefLanguage(PreferredLanguage);
		}
		if (!OtherLanguages.isEmpty() && !OtherLanguages.toString().equals("-1")) {
			Boolean nichtleer = false;
			for (String test : OtherLanguages.split(",")) {
				if (!test.equals("-1")) {
					nichtleer = true;
				}
			}
			if (nichtleer) {
				user_xyz.removeAllLanguages();
			}
			if (OtherLanguages != null && !OtherLanguages.isEmpty()) {
				for (String languageName : OtherLanguages.split(",")) {
					if (languageRepository.findByKennung(languageName) != null) {
						user_xyz.setLanguage(languageRepository.findByKennung(languageName));
						userRepository.save(user_xyz);
					}
				}
			}
		}
		userRepository.save(user_xyz);

		return "redirect:/data";
	}
}

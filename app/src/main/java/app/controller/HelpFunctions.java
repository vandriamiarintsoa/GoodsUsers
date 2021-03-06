package app.controller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.HttpsURLConnection;

import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.util.Assert;

import app.model.UserRepository;
import app.repository.LanguageRepository;

/**
 * <h1>CreateNewUserController</h1> The CreateNewUserController is used for
 * registration and create UserAccounts.
 * 
 *
 * @author Kilian Heret
 * 
 */

public class HelpFunctions {
	private final UserAccountManager userAccountManager;
	private final UserRepository userRepository;
	private final LanguageRepository languageRepository;
	private final MailSender mailSender;

	/**
	 * Autowire.
	 * 
	 * @param Helpfunctions
	 */
	@Autowired
	public HelpFunctions(UserAccountManager userAccountManager, UserRepository userRepository,
			LanguageRepository languageRepository, MailSender mailSender) {
		Assert.notNull(userAccountManager, "UserAccountManager must not be null!");
		Assert.notNull(userRepository, "UserRepository must not be null!");

		this.userAccountManager = userAccountManager;
		this.userRepository = userRepository;
		this.languageRepository = languageRepository;
		this.mailSender = mailSender;
	}

	public static String sha256(String base) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(base.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();

			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}

			return hexString.toString();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static boolean containsString(String s, String subString) {
		return s.indexOf(subString) > -1 ? true : false;
	}

	public static boolean emailValidator(String email) {
		boolean isValid = false;

		if (containsString(email, "@") == false) {
			return false;
		}

		if (containsString(email, ".") == false) {
			return false;
		}

		/*
		 * if (email.equals("test@test.test")) { return false; }
		 */

		try {
			//
			// Create InternetAddress object and validated the supplied
			// address which is this case is an email address.
			InternetAddress internetAddress = new InternetAddress(email);
			internetAddress.validate();
			isValid = true;
		} catch (AddressException e) {
			System.out.println("You are in catch block -- Exception Occurred for: " + email);
		}
		return isValid;
	}

	public static int checkPasswordStrength(String password) {
		if (password.equals("")) {
			return 0;
		}

		boolean hasLower = false;
		boolean hasUpper = false;
		boolean hasDigits = false;
		boolean hasSymbols = false;

		float strengthPercentage = 0;
		String[] partialRegexChecks = { ".*[a-z]+.*", // lower
				".*[A-Z]+.*", // upper
				".*[\\d]+.*", // digits
				".*[@#§$%&/()=?{}#+-~.,;:<>|\\!]+.*" // symbols
		};

		int i = 0;
		while (i < (password.length())) {

			if (password.substring(i, i + 1).matches(partialRegexChecks[0])) {
				hasLower = true;
				strengthPercentage += 2;
			}
			if (password.substring(i, i + 1).matches(partialRegexChecks[1])) {
				hasUpper = true;
				strengthPercentage += 3;
			}
			if (password.substring(i, i + 1).matches(partialRegexChecks[2])) {
				hasDigits = true;
				strengthPercentage += 5;
			}
			if ((!password.substring(i, i + 1).matches(partialRegexChecks[0]))
					&& (!password.substring(i, i + 1).matches(partialRegexChecks[1]))
					&& (!password.substring(i, i + 1).matches(partialRegexChecks[2]))) {
				if (password.substring(i, i + 1).matches(partialRegexChecks[3])) {
					hasSymbols = true;
					strengthPercentage += 8;
				}
			}
			i = i + 1;
		}

		// 12 Zeichen empfohlen
		int pwlengthsec = (int) Math.floor(Math.floor(Math.log((password.length() - 6) * 0.00002)) + 11);
		strengthPercentage = strengthPercentage * (1 + (pwlengthsec / 10)) * 3.0f;

		if (!((hasLower && hasUpper) && (hasDigits && hasSymbols) && (password.length() >= 8))) {
			strengthPercentage = 0;
		}
		return Math.round(strengthPercentage);
	}

	public static String sendPost(String CaptchaResponse, String Secret) throws Exception {

		String url = "https://www.google.com/recaptcha/api/siteverify" + "?response=" + CaptchaResponse + "&secret="
				+ Secret;
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		// add reuqest header
		con.setRequestMethod("POST");

		String urlParameters = "";

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		// System.out.println("\nSending 'POST' request to URL : " + url);
		// System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		System.out.println(response.toString());

		return response.toString();

	}

	public static void mailSenden(String SendTo, String Subject, String Text) throws MessagingException, IOException {

		// InputStream inputStream = null;
		FileInputStream inputStream = null;

		String mailhost = "";
		String mailport = "";
		String mailusername = "";
		String mailpassword = "";
		/*
		 * try { Properties prop = new Properties(); String propFileName =
		 * "application.properties"; //
		 * D:\\Repo\\swt15w9\\app\\target\\classes\\app
		 * 
		 * inputStream = new FileInputStream(".." + File.separator +
		 * propFileName);
		 * 
		 * if (inputStream != null) { prop.load(inputStream); } else { throw new
		 * FileNotFoundException("E: property file '" + propFileName +
		 * "' not found in the classpath"); }
		 * 
		 * Date time = new Date(System.currentTimeMillis());
		 * 
		 * // get the property value and print it out mailhost =
		 * prop.getProperty("spring.mail.host"); mailport =
		 * prop.getProperty("spring.mail.port"); mailusername =
		 * prop.getProperty("spring.mail.username"); mailpassword =
		 * prop.getProperty("spring.mail.password");
		 * 
		 * inputStream.close(); } catch (FileNotFoundException ex) {
		 * ex.printStackTrace(); } catch (IOException ex) {
		 * ex.printStackTrace(); } catch (Exception e) { System.out.println(
		 * "Exception: " + e); } finally { inputStream = null; }
		 */
		mailhost = "smtp.gmail.com"; // Aus application.properties auslesen.
		mailusername = "refugeesapp@gmail.com"; // Aus application.properties
												// auslesen.
		mailpassword = "refugeesapp#"; // Aus application.properties auslesen.
		mailport = "587"; // Aus application.properties auslesen.
		String recipient = SendTo;

		Properties props = new Properties();

		props.put("mail.smtp.host", mailhost);
		props.put("mail.from", mailusername);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.port", mailport);
		props.setProperty("mail.debug", "false");

		Session session = Session.getInstance(props, null);
		MimeMessage msg = new MimeMessage(session);

		msg.setRecipients(Message.RecipientType.TO, recipient);
		msg.setSubject(Subject);
		msg.setSentDate(new Date());
		msg.setContent(Text, "text/html; charset=utf-8");

		Transport transport = session.getTransport("smtp");

		transport.connect(mailusername, mailpassword);
		transport.sendMessage(msg, msg.getAllRecipients());
		transport.close();

		return;

	}

	public static String aktivationkeyCreation(String username, String mail, Integer Zufallszahl1,
			Integer Zufallszahl2) {
		float fl = Zufallszahl1 / Zufallszahl2;
		String starttext = HelpFunctions.sha256("s" + HelpFunctions.sha256(
				HelpFunctions.sha256("Aktivierungskey" + username + "123" + mail + "XYZ" + Float.toString(fl) + "fff")
						+ Zufallszahl1.toString())
				+ Zufallszahl2.toString());
		return HelpFunctions.sha256(
				HelpFunctions.sha256(HelpFunctions.sha256(HelpFunctions.sha256(HelpFunctions.sha256(starttext)))));
	}

	public static boolean checkCaptcha(String CaptchaResponse) {
		String Secret = "6LcBYBATAAAAAPHUZfB4OFpbdwrVxp08YEaVX3Dr";
		String Returnstring = "";

		System.out.println("## Validate:");
		System.out.println(
				"https://www.google.com/recaptcha/api/siteverify?response=" + CaptchaResponse + "&secret=" + Secret);

		try {
			Returnstring = HelpFunctions.sendPost(CaptchaResponse, Secret);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (Returnstring.equals("{  \"success\": true}")) {
			return true;
		} else {
			return false;
		}
	}

	public static String getOptionalString(Optional<String> optString) {
		String S;
		if (optString.isPresent()) {
			S = optString.get();
		} else {
			S = "";
		}
		return S;
	}

	public static String getOldData(final String firstname, final String name, final String mail, final String username,
			final String Adresstyp, final Optional<String> flh_name_OPT, final Optional<String> citypart_OPT,
			final Optional<String> street_OPT, final Optional<String> housenr_OPT, final Optional<String> postcode_R,
			final Optional<String> city_R, final Optional<String> postcode_H, final Optional<String> city_H,
			String nativelanguage, final String otherLanguages, final String origin) {

		String result = "";

		result = result + "&firstnameOld=" + firstname;
		result = result + "&nameOld=" + name;
		result = result + "&mailOld=" + mail;
		result = result + "&usernameOld=" + username;

		if (Adresstyp.equals("helper")) {
			result = result + "&checked1Old=checked";
		}
		if (Adresstyp.equals("refugee")) {
			result = result + "&checked2Old=checked";
		}

		// Wohnung
		result = result + "&streetOld=" + getOptionalString(street_OPT);
		result = result + "&housenrOld=" + getOptionalString(housenr_OPT);
		result = result + "&postcodeHOld=" + getOptionalString(postcode_H);
		result = result + "&cityHOld=" + getOptionalString(city_H);

		// Flüchtlingsheim

		result = result + "&fhl_nameOld=" + getOptionalString(flh_name_OPT);
		result = result + "&citypartOld=" + getOptionalString(citypart_OPT);
		result = result + "&postcodeROld=" + getOptionalString(postcode_R);
		result = result + "&cityROld=" + getOptionalString(city_R);
		// --

		// System.out.println("begin1");
		if (nativelanguage.isEmpty() || nativelanguage.equals("---- Select ----")) {
			result = result + "&nativelanguageOld=-1";
		} else {
			result = result + "&nativelanguageOld=" + nativelanguage;
		}
		// System.out.println("end1");

		String L2 = "";
		String L3 = "";
		String L4 = "";
		String L5 = "";
		int i = 2;
		if (!otherLanguages.isEmpty()) {
			// System.out.println("otherLanguages: '" + otherLanguages + "'");
			String S = otherLanguages + ",";
			while (S.length() > 0) {
				if (S.indexOf(",") >= 0) {
					if (i == 2) {
						L2 = S.substring(0, S.indexOf(","));
						if ((!L2.isEmpty()) && (!L2.equals("-1"))) {
							result = result + "&language2Old=" + L2;
						}
					}
					if (i == 3) {
						L3 = S.substring(0, S.indexOf(","));
						if ((!L3.isEmpty()) && (!L3.equals("-1"))) {
							result = result + "&language3Old=" + L3;
						}
					}
					if (i == 4) {
						L4 = S.substring(0, S.indexOf(","));
						if ((!L4.isEmpty()) && (!L4.equals("-1"))) {
							result = result + "&language2Old=" + L4;
						}
					}
					if (i == 5) {
						L5 = S.substring(0, S.indexOf(","));
						if ((!L5.isEmpty()) && (!L5.equals("-1"))) {
							result = result + "&language2Old=" + L5;
						}
					}

					S = S.substring(S.indexOf(",") + 1);
					// System.out.println("oL: '" + S + "' >L2:" + L2 + "L3:" +
					// L3 + "L4:" + L4 + "L5:" + L5);

				} else
					S = "";
				i++;
			}
		}

		if (origin.isEmpty()) {
			result = result + "&originOld=---- Select ----";
		} else {
			result = result + "&originOld=" + origin;
		}

		return result;

	}
}

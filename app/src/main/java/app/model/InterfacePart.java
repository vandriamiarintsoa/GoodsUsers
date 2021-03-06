package app.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

	/**
	 * <h1>InterfacePart</h1> The InterfacePart connects the corresponding {@link Module} to one of its translations.
	 * 
	 * @author Lennart Schmidt
	 *
	 */

@Entity
public class InterfacePart {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	// Maybe Persistence Problems
	private long languageId, moduleId;

	private String text;

	/**
	 * Private Constructor
	 */
	@SuppressWarnings("unused")
	private InterfacePart() {
	}

	public InterfacePart(String text, long languageId, long moduleId) {
		this.text = text;
		this.languageId = languageId;
		this.moduleId = moduleId;
	}

	/**
	 * getter for the id
	 * 
	 * @return
	 */
	public long getId() {
		return id;
	}

	/**
	 * getter for the LanguageId
	 * 
	 * @return
	 */
	public long getLanguageId() {
		return languageId;
	}

	/**
	 * getter for the ModuleId
	 * 
	 * @return
	 */
	public long getModuleId() {
		return moduleId;
	}

	/**
	 * getter for the Text
	 * 
	 * @return
	 */
	public String getText() {
		return text;
	}

	 /**
	  * setter for the Text
	  * 
	  * @param text
	  */
	 
	public void setText(String text) {
		this.text = text;
	}
}

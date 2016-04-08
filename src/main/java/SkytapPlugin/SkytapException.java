/* Copied from Skytap Plugin
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SkytapPlugin;

/**
 * Custom exception class to handle errors/exceptions 
 * returned from Skytap
 * 
 * @author ptoma
 *
 */
public class SkytapException extends Exception {

	  private String skytapError;

	  public SkytapException()
	  {
	    super();             // call superclass constructor
	    skytapError = "Unknown Error";
	  }

	  public SkytapException(String err)
	  {
	    super(err);     // call super class constructor
	    skytapError = err;  // save message
	  }

	  public String getError()
	  {
	    return skytapError;
	  }

}

//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.tools;

/**
 * Signals an anomalous situation parsing a command line.
 *
 * @see	Command
 *
 * @version	Last modified on Fri Apr 13 19:43:24 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class CommandException extends RuntimeException
{
  /**
   * Constructs a new CommandException with a message.
   */
  public CommandException (String msg)
   {
     super(msg);
   }
}

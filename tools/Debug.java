//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.tools;

/**
 * This implements a (very) primitive debugging class.
 *
 * @version     Last modified on Sat Nov 10 20:15:13 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

import java.io.InputStreamReader;
import java.io.IOException;

public class Debug
{
  private static boolean _debugFlag;

  public static final void setDebugFlag (boolean flag)
    {
      _debugFlag = flag;
    }

  public static final boolean flagIsOn ()
    {
      return _debugFlag;
    }

  public Debug ()
    {
      step();
    }

  public Debug (Object o)
    {
      step(o);
    }

  private static int step = 0;

  private static String quitString = "quit";

  /* ************************************************************************ */

  /**
   * Resets the quit string.
   */
  public static final void setQuitString (String s)
    {
      quitString = s.intern();
    }
    
  /* ************************************************************************ */

  /**
   * Returns the quit string.
   */
  public static final String getQuitString ()
    {
      return quitString;
    }

  /* ************************************************************************ */

  /**
   * Returns <tt>true</tt> iff the specified string is a non-empty
   * prefix of the quit string.
   */
  public static final boolean matchesQuitString (String s)
    {
      return s.length() > 0 && quitString.startsWith(s);
    }
    
  /* ************************************************************************ */

  /**
   * Returns <tt>true</tt> iff the specified string is the quit string.
   */
  public static final boolean isQuitString (String s)
    {
      return (s.intern() == quitString);
    }
    
  public final static void stepIfFlag (Object info)
    {
      if (_debugFlag) step(info);
    }

  /* ************************************************************************ */

  /**
   * Prompts the user using the specified info and returns
   * the string entered by the user.
   */
  public final static String step (Object info)
    {
      System.err.println
        ("\n-------------------------------------------------------------------");

      String msg = info instanceof int[] ? Misc.arrayToString((int[])info)
                   : info instanceof double[] ? Misc.arrayToString((double[])info)
                     : info instanceof Object[] ? Misc.arrayToString((Object[])info)
                       : info.toString();

      return Misc.prompt(msg+"\nSTEP ["+(++step)+"] >", System.err);
    }

  /* ************************************************************************ */

  public final static String step ()
    {
      return step("");
    }

  /* ************************************************************************ */

}



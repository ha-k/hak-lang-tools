//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.tools;

/**
 *
 * This class implements a grab-bag of useful static methods. If you
 * need something that is not defined where you'd expect it although it
 * is useful, can be reused many times, and is generic enough to warrant
 * being used in arbitrary contexts, chances are it's in here - and if
 * it isn't, it should be!
 *
 *
 * @version     Last modified on Fri Sep 10 10:28:14 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

import hlt.language.io.IO;
import hlt.language.io.FileTools;

import hlt.language.util.ViewableStack;
import hlt.language.util.Queue;
import hlt.language.util.ArrayList;
import hlt.language.util.IntStack;
import hlt.language.util.Locatable;
import hlt.language.util.Location;
import hlt.language.util.Comparable;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.Calendar;
import java.util.Date;
import java.io.IOException;
import java.io.PrintStream;
import java.io.InputStream;

// import com.rits.cloning.Cloner;	// For deep cloning of any object.
					// Also needs objenesis package for compiling and running.

public class Misc
{
  /* ************************************************************************ */

  /**
   * Return the correct ordinal suffix for the specified number.
   */
  public static String ordinal (int n)
    {
      switch (n)
	{
	case 1:
	  return "st";
	case 2:
	  return "nd";
	case 3:
	  return "rd";
	}

      return "th";
    }

  /* ************************************************************************ */

  /**
   * Return the current date as a <tt>java.util.Calendar</tt> object.
   */
  public static Calendar now ()
    {
      return Calendar.getInstance();
    }

  /* ************************************************************************ */

  /**
   * Return the current year as an int.
   */
  public static int currentYear ()
    {
      return now().get(Calendar.YEAR) - 1900;
    }

  /* ************************************************************************ */

  /**
   * Returns a string of <tt>n</tt> <tt>c</tt>s.
   */
  public static String repeat (int n, char c)
    {
      StringBuilder buf = new StringBuilder();
      for (int i = n; i-->0;) buf.append(c);
      return buf.toString();
    }

  /* ************************************************************************ */

  /**
   * Returns the specified string surrounded with the specified char (with <tt>n</tt>
   * on the left, and as many as needed on the right to make the <tt>length</tt>.
   */
  public static String title (String string, char c, int n, int length)
    {
      StringBuilder buf = new StringBuilder();
      for (int i = n; i-->0;) buf.append(c);
      buf.append(string);
      for (int i=length-n-string.length(); i-->0;) buf.append(c);
      return buf.toString();
    }

  /* ************************************************************************ */

  /**
   * Returns a string describing in min/sec the time specified in milliseconds.
   */       
  public static final String minsec (long time)
    {
      time = time/1000;
      int mins = (int)(time/60);
      int secs = (int)(time%60);
      return "" + mins + " minute" + (mins > 1 ? "s " : " ") +
	secs + " second" + (secs > 1 ? "s" : "");
    }
  
  /* ************************************************************************ */

  /**
   * Returns a legible "view" of a stack as a String.
   */
  public static final String view (ViewableStack stack, String name, int offset, int stackWidth)
    {
      if (stack == null) return "null";

      int marginWidth = offset + name.length() + 5;     // 5 for " ==> "

      StringBuilder margin = new StringBuilder(marginWidth);
      for (int i=marginWidth; i-->0;) margin.append(' ');

      StringBuilder sep = new StringBuilder(stackWidth);
      for (int i=stackWidth; i-->0;) sep.append('-');

      StringBuilder stackBuffer = new StringBuilder(stack.size()*(marginWidth+stackWidth+1)).append('\n');

      stackBuffer.append(margin).append(sep).append('\n');
      stackBuffer.append(margin.substring(0,offset)).append(name).append(" ==> ");

      Iterator i = stack.iterator();
      if (i.hasNext())
	stackBuffer.append(etc(stackWidth,i.next()));
      stackBuffer.append('\n');
      while (i.hasNext())
	stackBuffer.append(margin).append(etc(stackWidth,i.next())).append('\n');

      stackBuffer.append(margin).append(sep).append('\n');

      return stackBuffer.toString();
    }

  /* ************************************************************************ */

  /**
   * Returns a legible "view" of a queue as a String.
   */
  public static final String view (Queue queue, String name, int offset, int queueWidth)
    {
      if (queue == null) return "null";

      int marginWidth = offset + name.length() + 5;     // 5 for " ==> "

      StringBuilder margin = new StringBuilder(marginWidth);
      for (int i=marginWidth; i-->0;) margin.append(' ');

      StringBuilder sep = new StringBuilder(queueWidth);
      for (int i=queueWidth; i-->0;) sep.append('-');

      StringBuilder queueBuffer = new StringBuilder(queue.size()*(marginWidth+queueWidth+1)).append('\n');

      queueBuffer.append(margin).append(sep).append('\n');
      queueBuffer.append(margin.substring(0,offset)).append(name).append(" ==> ");
      Iterator q = queue.iterator();
      if (q.hasNext())
	queueBuffer.append(etc(queueWidth,q.next()));
      queueBuffer.append('\n');
      while (q.hasNext())
	queueBuffer.append(margin).append(etc(queueWidth,q.next())).append('\n');

      queueBuffer.append(margin).append(sep).append('\n');

      return queueBuffer.toString();
    }

  /* ************************************************************************ */

  /**
   * Returns the passed argument string deprived of any single or double
   * quoting if any. A repeatedly quoted form will be stripped of all
   * its layers of quotes - <i>i.e.</i>, <tt>unquotify("'"foo"'")</tt>
   * will return <tt>foo</tt>.
   */
  public static final String unquotify (String s)
    {
      if (s.length() < 1)
	return s;
      
      if ((s.charAt(0) == '"' && s.charAt(s.length()-1) == '"')
	  || (s.charAt(0) == '\'' && s.charAt(s.length()-1) == '\''))
	return unquotify(s.substring(1,s.length()-1));

      return s;
    }

  /* ************************************************************************ */

  /**
   * Returns a possibly truncated string form no longer than the specified width
   * for the specified object (including trailing " ..." if the string form is
   * longer than the specified width).
   */
  public static final String etc (int width, Object object)
    {
      if (object == null) return "null";
      String s = stringify(object.toString(),'\\','\\'); // leave double quotes unescaped
      return s.length() <= width ? s : s.substring(0,Math.min(width-4,s.length())) + " ...";
    }  

  /* ************************************************************************ */

  /**
   * Returns a string for the specified array of ints, using the specified
   * start, separator, and end strings.
   */
  public static final String arrayToString (int[] a, String start, String separator, String end)
    {
      if (a == null) return "null";

      StringBuilder buf = new StringBuilder(start);

      for (int i=0; i<a.length; i++)
	buf.append(a[i]+(i==a.length-1?"":separator));

      return buf.append(end).toString();
    }

  /* ************************************************************************ */

  /**
   * Returns a string for the specified array of ints as a
   * square-bracketed comma-sparated list of its elements.
   */
  public static final String arrayToString (int[] a)
    {
      return arrayToString(a,"[",",","]");
    }

  /* ************************************************************************ */

  /**
   * Returns a string for the specified array of doubles.
   */
  public static final String arrayToString
    (double[] a, String start, String separator, String end)
    {
      if (a == null) return "null";

      StringBuilder buf = new StringBuilder(start);

      for (int i=0; i<a.length; i++)
	buf.append(a[i]+(i==a.length-1?"":separator));

      return buf.append(end).toString();
    }

  /* ************************************************************************ */

  /**
   * Returns a string for the specified array of doubles as a
   * square-bracketed comma-sparated list of its elements.
   */
  public static final String arrayToString (double[] a)
    {
      return arrayToString(a,"[",",","]");
    }

  /* ************************************************************************ */

  /**
   * Returns a string for the specified array of objects.
   */
  public static final String arrayToString
    (Object[] a, String start, String separator, String end)
    {
      if (a == null) return "null";

      StringBuilder buf = new StringBuilder(start);

      for (int i=0; i<a.length; i++)
	{
	  Object o = a[i];
	  String s
	    = o == null             ? "null"
	    : o instanceof int[]    ? arrayToString((int[])o,start,separator,end)
	    : o instanceof double[] ? arrayToString((double[])o,start,separator,end)
	    : o instanceof Object[] ? arrayToString((Object[])o,start,separator,end)
	    : o instanceof String   ? "\""+o.toString()+"\""
	                            : o.toString();

	  buf.append(s+(i==a.length-1?"":separator));
	}

      return buf.append(end).toString();
    }
  
  /* ************************************************************************ */

  /**
   * Returns a string for the specified array of Objects as a
   * square-bracketed comma-sparated list of its elements.
   */
  public static final String arrayToString (Object[] a)
    {
      return arrayToString(a,"[",",","]");
    }

  /* ************************************************************************ */

  /**
   * Returns <tt>true</tt> iff the two arrays have equal elements.
   * <b>NB:</b> <tt>null</tt> arrays are considered equal.
   */
  public static final boolean equal (int[] a1, int[] a2)
    {
      if (a1 == null)
	return a2 == null;

      if (a2 == null)
	return false;

      if (a1 == a2)
	return true;

      if (a1.length != a2.length)
	return false;

      for (int i=0; i<a1.length; i++)
	if (a1[i] != a2[i])
	  return false;

      return true;
    }

  /* ************************************************************************ */

  /**
   * Returns <tt>true</tt> iff the two arrays have equal elements.
   * <b>NB:</b> <tt>null</tt> arrays are considered equal.
   */
  public static final boolean equal (double[] a1, double[] a2)
    {
      if (a1 == null)
	return a2 == null;

      if (a2 == null)
	return false;

      if (a1 == a2)
	return true;

      if (a1.length != a2.length)
	return false;

      for (int i=0; i<a1.length; i++)
	if (a1[i] != a2[i])
	  return false;

      return true;
    }

  /* ************************************************************************ */

  /**
   * Returns <tt>true</tt> iff the two arrays have equal elements.
   * <b>NB:</b> <tt>null</tt> arrays are considered equal.
   */
  public static final boolean equal (Object[] a1, Object[] a2)
    {
      if (a1 == null)
	return a2 == null;

      if (a2 == null)
	return false;

      if (a1 == a2)
	return true;

      if (a1.length != a2.length)
	return false;

      for (int i=0; i<a1.length; i++)
	{
	  if (a1[i] instanceof int[] && a2[i] instanceof int[])
	    {
	      if (!equal((int[])a1[i],(int[])a2[i]))
		return false;
	      continue;
	    }

	  if (a1[i] instanceof double[] && a2[i] instanceof double[])
	    {
	      if (!equal((double[])a1[i],(double[])a2[i]))
		return false;
	      continue;
	    }

	  if (a1[i] instanceof Object[] && a2[i] instanceof Object[])
	    {
	      if (!equal((Object[])a1[i],(Object[])a2[i]))
		return false;
	      continue;
	    }

	  if (!a1[i].equals(a2[i]))
	    return false;
	}

      return true;
    }

  /* ************************************************************************ */

  /**
   * Returns the qualified name of the class of the specified object.
   */
  public static final String className (Object object)
    {
      return object.getClass().toString().substring(6);
    }

  /* ************************************************************************ */

  /**
   * Returns the unqualified name of the class of the specified object.
   */
  public static final String simpleClassName (Object object)
    {
      return FileTools.suffixIfDot(className(object));
    }

  /* ************************************************************************ */

  /**
   * Returns an <tt>ArrayList</tt> containing the elements of specified array.
   */
  public static final ArrayList list (Object[] obs)
    {
      ArrayList list = new ArrayList();
      for (int i=0; i<obs.length; i++)
	list.add(obs[i]);
      return list;
    }

  /* ************************************************************************ */

  /**
   * Return <tt>true</tt> iff the two specified locations are the same.
   */
  public static final boolean sameLocation (Location l1, Location l2)
    {
      if (l1 == l2)
	return true;

      if (l1 == null)
	if (l2 == null)
	  return true;
	else
	  return false;
      else
	if (l2 == null)
	  return false;
	else
	  return l1.getFile() == l2.getFile()
	    && l1.getLine() == l2.getLine()
	    && l1.getColumn() == l2.getColumn();
    }

  /* ************************************************************************ */

  /**
   * Return <tt>true</tt> iff the two specified locatables have the same extent.
   */
  public static final boolean sameExtent (Locatable l1, Locatable l2)
    {
      if (l1 == l2)
	return true;

      if (l1 == null)
	if (l2 == null)
	  return true;
	else
	  return false;
      else
	if (l2 == null)
	  return false;
	else
	  return sameLocation(l1.getStart(),l1.getEnd())
	    && sameLocation(l2.getStart(),l2.getEnd());
    }

  /* ************************************************************************ */

  /**
   * Returns <tt>true</tt> if the first location "precedes" the second one.
   * That is, if the second one is nested in, or to the right of, the first
   * one. This makes sense only for if the locatables span a the same file.
   */
  public static final boolean precedes (Locatable l1, Locatable l2)
    {
      if (l1 == null) return true;
      if (l2 == null) return true;

      if (l1.getStart() == null || l1.getEnd() == null)
	return true;
      if (l2.getStart() == null || l2.getEnd() == null)
	return true;

      return l1.getEnd().precedes(l2.getStart())
	|| l1.getStart().precedes(l2.getStart()) && l2.getEnd().precedes(l1.getEnd());
    }

  /* ************************************************************************ */

  /**
   * Returns the locatable that is "further down". That is, the one that is
   * nested in, or to the right of, the other. This makes sense only for if
   * the locatables are within the same file.
   */
  public static final Locatable latestExtent (Locatable l1, Locatable l2)
    {
      if (l1 == null) return l2;
      if (l2 == null) return l1;

      return precedes(l1,l2) ? l2 : l1;
    }

  /* ************************************************************************ */

  /**
   * Returns an explicit string for the specified locatable.
   */
  public static final String locationString (Locatable extent)
    {
      String s = "<unlocated>";

      if (extent != null)
	{
	  Location start = extent.getStart();
	  Location end = extent.getEnd();

	  if (start == null)
	    if (end == null)
	      ;
	    else
	      s = locationString(end);
	  else
	    if (end == null || start.equals(end))
	      s = locationString(start);
	    else
	      if (start.getFile() == end.getFile())
		if (start.getLine() == end.getLine())
		  s = start.getFile() + " (" + "line " + start.getLine() + ", "
		    + "columns " + start.getColumn() + ".." +end.getColumn() + ")";
		else
		  s = locationString(start) + ".." + " (" + "line " + end.getLine() + ", "
		    + "column " + end.getColumn() + ")";
	      else
		s = locationString(start) + ".." + locationString(end);
	}

      return s;
    }

  /* ************************************************************************ */

  /**
   * Returns an explicit string for the specified location.
   */
  public static final String locationString (Location location)
    {
      return location.getFile() +
	" (" + "line "   + location.getLine() + ", " + "column " + location.getColumn()+")";
    }

  /* ************************************************************************ */

  /**
   * A method that beeps.
   */ 
  public static final void beep ()
    {
      System.err.print(IO.BIP);
      System.err.flush();
    }

  /* ************************************************************************ */

  /**
   * Returns the string obtained from the specified string as an
   * identical one except that special characters are rendered as their
   * escape sequences.
   */
  public static final String stringify (String s)
    {
      return stringify(s,'"','\\');
    }

  /* ************************************************************************ */

  /**
   * Returns the string obtained from the specified string as an
   * identical one except that special characters are rendered as their
   * escape sequences. The last two arguments are the quote and escape
   * characters.
   */
  public static final String stringify (String s, char quote, char escape)
    {
      StringBuilder buf = new StringBuilder();

      int length = s.length();
      for (int i=0; i<length; i++)
	{
	  char c = s.charAt(i);

	  if (c == quote || c == escape)
	    buf.append(escape);
	  buf.append(pform(c));
	}

      return buf.toString();
    }
  
  /* ************************************************************************ */

  /**
   * Returns the string obtained from the specified string
   * transformed so that it would give the original string
   * when double quoted and using backslash as the escape character.
   */
  public static final String quotify (String s)
    {
      return quotify(s,'"','\\');
    }

  /* ************************************************************************ */

  /**
   * Returns the string obtained from the specified string
   * transformed so that it would give the original string
   * when quoted. The specified <tt>quote</tt> and <tt>escape</tt>
   * are the quote and escape characters, respectively.
   */
  public static final String quotify (String s, char quote, char escape)
    {
      StringBuilder buf = new StringBuilder();

      int length = s.length();
      for (int i=0; i<length; i++)
	{
	  char c = s.charAt(i);
	  if (c == quote || c == escape)
	    buf.append(escape);
	  buf.append(c);
	}

      return buf.toString();
    }

  /* ************************************************************************ */

  /**
   * Returns the string obtained from the specified string
   * after changing its first character to a capital.
   */
  public static final String capitalize (String s)
    {
      return Character.toUpperCase(s.charAt(0))+s.substring(1);
    }

  /* ************************************************************************ */

  /**
   * Returns <tt>true</tt> iff the given string contains at least one
   * letter, and all its letters are lower case letters.
   */
  public static final boolean isLowerCase (String s)
    {
      boolean hasOneLetter = false;
      for (int i = s.length(); i-->0;)
	{
	  char l = s.charAt(i);
	  hasOneLetter |= Character.isLetter(l);
	  if (Character.isUpperCase(l))
	    return false;
	}

      return hasOneLetter;
    }

  /* ************************************************************************ */

  /**
   * Returns <tt>true</tt> iff the given string contains at least one
   * letter, and all its letters are upper case letters.
   */
  public static final boolean isUpperCase (String s)
    {
      boolean hasOneLetter = false;
      for (int i = s.length(); i-->0;)
	{
	  char l = s.charAt(i);
	  hasOneLetter |= Character.isLetter(l);
	  if (Character.isLowerCase(l))
	    return false;
	}

      return hasOneLetter;
    }

  /* ************************************************************************ */

  /**
   * Returns the string obtained from the specified string
   * after substituting characters that are special to HTML
   * by their HTML encoding string.
   */
  public static final String htmlString (String s)
    {
      StringBuilder buff = new StringBuilder();

      for (int i=0; i<s.length(); i++)
	buff.append(htmlCode(s.charAt(i)));

      return buff.toString();
    }
  
  /* ************************************************************************ */

  /**
   * Returns the HTML encoding string of the specified character.
   */
  public static final String htmlCode (char c)
    {
      switch (c)
	{
	case '<': return "&lt;";
	case '>': return "&gt;";
	case '&': return "&amp;";
	}
      return String.valueOf((char)c);
    }

  /* ************************************************************************ */

  /**
   * Asks the user for a Y/N answer and returns it boolean
   * interpretation - the default answer is yes.
   */
  public static final boolean askYesNo (String s)
    {
      return askYesNo(s,true);
    }
    
  /* ************************************************************************ */

  /**
   * Asks the user for a Y/N answer with the specified default answer,
   * with default answer specified as boolean <tt>yesno</tt>, using
   * standard input/output streams.
   */
  public static final boolean askYesNo (String prompt, boolean yesno)
    {
      return askYesNo(System.out,System.in,System.err,prompt,yesno);
    }

  /* ************************************************************************ */

  /**
   * Asks the user for a Y/N answer with the specified default answer,
   * with default answer specified as boolean <tt>yesno</tt>.
   */
  public static final boolean askYesNo (PrintStream out, InputStream in, PrintStream err, String prompt, boolean yesno)
    {
      out.print(prompt+"? (y/n) ["+(yesno?"y":"n")+"] > ");
      out.flush();

      try
	{
	  switch (in.read())
	    {
	    case '\n':
	      return yesno;
	    case 'y': case 'Y':
	      while (in.read() != '\n');
	      return true;
	    case 'n': case 'N':
	      while (in.read() != '\n');
	      return false;
	    default:
	      while (in.read() != '\n');
	    }
	}
      catch (IOException e)
	{
	  err.println("*** IO Exception when asking: \""+prompt+"\"");
	  e.printStackTrace();
	}

      out.println("Please answer yes or no!...");
      return askYesNo(prompt,yesno);
    }

  /* ************************************************************************ */

  /**
   * Prompts the user with the specified string on the specified
   * PrintStream, then returns the string entered up to carriage
   * return on stdin.
   */
  public static final String prompt (String s, PrintStream out)
    {
      out.print(s+" ");

      StringBuilder ans = new StringBuilder();
      int ch;

      try
	{
	  while ((ch=System.in.read()) != '\n')
	    ans.append((char)ch);
	}
      catch (IOException e)
	{
	  System.err.println("*** IO Exception when prompting: \""+s+"\"");
	  e.printStackTrace();
	}

      return ans.toString();
    }

  /* ************************************************************************ */

  /**
   * Prompts the user with the specified string on stdout, then
   * returns the string entered up to carriage return on stdin.
   */
  public static final String prompt (String s)
    {
      return prompt(s,System.out);
    }

  /* ************************************************************************ */

  /**
   * Returns a string of <tt>n</tt> blank spaces.
   */
  public static final String spaces (int n)
  {
    StringBuffer buf = new StringBuffer();
    for (int i=n; n-->0;)
      buf.append(" ");
    return buf.toString();
  }

  /* ************************************************************************ */

  /**
   * Prints the given string on the standard output
   * then backs over the length of it. This is useful
   * for an in-place counter.
   */
  public static final void printErase (String s)
    {
      System.out.print(s);
      for (int i=0; i<s.length(); i++) System.out.print("\b");
    }    

  /* ************************************************************************ */

  /**
   * Returns the substring of the given string that starts
   * at the first letter occurrence in the string. If none
   * returns the full string.
   */
  public static final String letterSubstring (String s)
    {
      int i = 0;

      while (i<s.length() && !Character.isLetter(s.charAt(i))) i++;

      if (i == s.length()) return s;

      return s.substring(i);
    }    

  /* ************************************************************************ */

  /**
   * Returns a printable form of the given character code.
   */
  public static final String pform (int c)
    {
      switch (c)
	{
	case IO.EOF: return "EOF"; // End of file
	case IO.EOI: return "EOI"; // End of input
	case IO.SOI: return "SOI"; // Start of input
	case IO.WRD: return "WRD"; // Word
	case IO.NUM: return "NUM"; // Number
	case IO.NTG: return "NTG"; // Nothing
	case IO.SPL: return "SPL"; // Special
	case IO.BIP: return "BIP"; // Beep
	case IO.EOL: return "\\n"; // End of line
	case IO.TAB: return "\\t"; // Tab
	case IO.CRT: return "\\r"; // Carriage return
	case IO.BSP: return "\\b"; // Backspace
	case IO.FFD: return "\\f"; // Form feed
	case IO.BSL: return "\\";  // Backslash
	case IO.SQT: return "'";   // Single quote
	case IO.DQT: return "\"";  // Double quote
	case IO.BQT: return "`";   // Back quote
	default:     return String.valueOf((char)c);
	}
    }

  /* ************************************************************************ */

  /**
   * Returns the string representation of the given integer <tt>n</tt>
   * using as many <tt>pad</tt> characters on the left, using at least
   * <tt>width</tt> digits in total.
   */ 
  public static final String numberString (long n, int width, char pad)
    {
      int n_width = numWidth(n);
      StringBuilder str = new StringBuilder();

      for (int i=0; i<width-n_width; i++)
	str.append(pad);

      str.append(n);

      return str.toString();
    }

  /* ************************************************************************ */

  /**
   * Returns the string representation of the given long integer <tt>n</tt>
   * using as many blank spaces on the left, using at least <tt>width</tt>
   * digits in total.
   */ 
  public static final String numberString (long n, int width)
    {
      return numberString(n,width,' ');
    }

  /* ************************************************************************ */

  /**
   * Returns the string representation of the given integer <tt>n</tt>
   * using as many zeros on the left, using at least <tt>width</tt>
   * digits in total.
   */ 
  public static final String zeroPaddedString (int n, int width)
    {
      if (n < 0)
	return "-"+zeroPaddedString(-n,width);

      return numberString(n,width,'\060');        // '\060' = ascii code of 0
    }

  /* ************************************************************************ */

  /**
   * Returns the number of decimal digits necessary to write the
   * given long integer <tt>n</tt>.
   */
  public static final int numWidth (long n)
    {
      if (n < 0)
	return 1+numWidth(-n);

      int w = 1;
      while ((n /= 10) != 0) w++;
      return w;
    }
      
  /* ************************************************************************ */

  /**
   * Returns <tt>true</tt> iff the given string is a positive
   * word (<i>i.e.</i>, true, t, yes, y). It is case independent.
   */ 
  public static final boolean booleanValueOf (String s)
    {
      return ((s != null)
	      && (s.toLowerCase().equals("t")
		  || s.toLowerCase().equals("true")
		  || s.toLowerCase().equals("y")
		  || s.toLowerCase().equals("yes")));
    }
  
  /* ************************************************************************ */

  /**
   * Returns the string representation of the given integer in
   * the given radix.
   */ 
  public static final String valueInRadix(int n, int radix)
    {
      if (radix < 1 || radix > 36)
	// there are only 36 alphanumerical digits
	{ System.err.println("Cannot handle radix: "+radix+
			     " (must be between 1 and 36)");
	throw new NumberFormatException();
	}

      if (n < 0) return "-"+valueInRadix(-n,radix);

      if (radix == 1) return ones(n);

      String numeral = "";

      do
	{ numeral = digit(n % radix) + numeral;
	n /= radix;
	}
      while (n != 0);

      return numeral;
    }

  /* ************************************************************************ */

  /**
   * Returns the character whose code is the given integer.
   */ 
  private static final char digit (int c)
    {
      if (c >= 0 && c <= 9) return (char)('0'+c);
      return (char)('A'+c-10);
    }      

  /* ************************************************************************ */

  /**
   * Returns a string of <tt>n</tt> 1's.
   */ 
  private static final String ones (int n)
    {
      StringBuilder s = new StringBuilder(n);
      for (int i=0; i<n; i++) s.append("1");
      return s.toString();
    }      

  /* ************************************************************************ */

  /**
   * Quietly forces garbage collection.
   */
  public static final void forceGC ()
    {
      forceGC(false);
    }

  /* ************************************************************************ */

  /**
   * Forces garbage collection, and reports it on the standard error stream if the
   * given boolean argument is <tt>true</tt>.
   */
  public static final void forceGC (boolean sayit)
    {
      forceGC(sayit,System.err);
    }

  /* ************************************************************************ */

  /**
   * Forces garbage collection, and reports it on the specified stream if the
   * given boolean argument is <tt>true</tt>.
   */
  public static final void forceGC (boolean sayit, PrintStream stream)
    {
      Runtime rt = Runtime.getRuntime();
      long total = rt.totalMemory();
      long currentFree = rt.freeMemory();
      long used = total-currentFree;
      long originalFree = currentFree;
      long previousFree;
      long time;
      int max_width;

      if (sayit)
	{
	  max_width = Math.max(numWidth(used),
			       Math.max(numWidth(currentFree),
					numWidth(total)));
	  stream.println("*** Memory usage before garbage collection:");
	  stream.println("\t"+numberString(total,max_width)+" bytes total");
	  stream.println("\t"+numberString(used,max_width)+" bytes used");
	  stream.println("\t"+numberString(currentFree,max_width)+" bytes available");
	}

      time = System.currentTimeMillis();

      do
	{
	  rt.runFinalization();
	  rt.gc();
	  previousFree = currentFree;
	  currentFree = rt.freeMemory();
	}
      while (currentFree > previousFree);

      time = System.currentTimeMillis()-time;

      if (sayit)
	{
	  long reclaimed = currentFree-originalFree;
	  used = total-currentFree;
	  max_width = Math.max(numWidth(used),
			       Math.max(numWidth(currentFree),
					numWidth(reclaimed)));
	  stream.println("*** Garbage collection done in "+time+" ms");
	  stream.println("*** Memory usage after garbage collection:");
	  stream.println("\t"+numberString(reclaimed,max_width)+" bytes reclaimed");
	  stream.println("\t"+numberString(used,max_width)+" bytes used");
	  stream.println("\t"+numberString(currentFree,max_width)+" bytes available");
	}
    }

  /* ************************************************************************ */

  /**
   * Sorts the specified array of comparable objects in place. It
   * assumes that the class of objects stored in the array implements
   * the <a href="../util/Comparable.html">
   * <tt>hlt.language.util.Comparable</tt></a> interface. The algorithm
   * used is (non-recursive) QuickSort. It returns the specified array.
   */
  final public static Comparable[] sort (Comparable[] a)
    {
      return sort(a,0,a.length-1);
    }

  /* ************************************************************************ */

  /**
   * Sorts the elements between index fst and lst (both inclusive) in
   * the specified array of comparable objects in place. It assumes that
   * the class of objects stored in the array implements the <a
   * href="../util/Comparable.html">
   * <tt>hlt.language.util.Comparable</tt></a> interface. The algorithm
   * used is (non-recursive) <a
   * href="http://en.wikipedia.org/wiki/Quicksort">QuickSort</a>. It
   * returns the specified array.
   */
  final public static Comparable[] sort (Comparable[] a, int fst, int lst)
    {
      int i, j;
      Comparable m, temp;

      int lo = fst;
      int hi = lst;

      IntStack stack = new IntStack();
      stack.push(hi);
      stack.push(lo);

      do
	{
	  lo = stack.pop();
	  hi = stack.pop();

	  while (lo < hi)
	    {
	      i = lo;
	      j = hi;
	      m = a[lo+(hi-lo)/2]; // instead of (lo+hi)/2 to avoid potential overflow

	      do
		{
		  while (i<a.length && a[i].lessThan(m))
		    i++;

		  while (j>=0 && m.lessThan(a[j]))
		    j--;

		  if (i <= j)
		    {
		      if (i != j)
			{
			  temp = a[i];
			  a[i] = a[j];
			  a[j] = temp;
			}
		      i++;
		      j--;
		    }
		}
	      while (i <= j);

	      if (i < hi)
		{
		  stack.push(hi);
		  stack.push(i);
		}

	      hi = j;
	    }
	}
      while (!stack.isEmpty());

      return a;
    }

  /* ************************************************************************ */

  /**
   * Sorts the specified <tt>AbstractList</tt> in place. It assumes that the
   * class of objects stored in the <tt>AbstractList</tt> implements the
   * <a href="../util/Comparable.html"><tt>hlt.language.util.Comparable</tt></a>
   * interface. The algorithm used is (non-recursive) QuickSort. It returns the
   * specified <tt>AbstractList</tt>.
   */
  final public static AbstractList sort (AbstractList a)
    {
      return sort(a,0,a.size()-1);
    }

  /* ************************************************************************ */

  /**
   * Sorts the elements between index fst and lst (both inclusive) in
   * the specified <tt>AbstractList</tt> in place. It assumes that the
   * class of objects stored in the <tt>AbstractList</tt> implements the
   * <a
   * href="../util/Comparable.html"><tt>hlt.language.util.Comparable</tt></a>
   * interface. The algorithm used is (non-recursive) <a
   * href="http://en.wikipedia.org/wiki/Quicksort">QuickSort</a>. It
   * returns the specified <tt>AbstractList</tt>.
   */
  final public static AbstractList sort (AbstractList a, int fst, int lst)
    {
      int i, j;
      Comparable m;
      Object temp;

      int lo = fst;
      int hi = lst;

      IntStack stack = new IntStack();
      stack.push(hi);
      stack.push(lo);

      do
	{
	  lo = stack.pop();
	  hi = stack.pop();

	  while (lo < hi)
	    {
	      i = lo;
	      j = hi;
	      m = (Comparable)a.get(lo+(hi-lo)/2); // instead of (lo+hi)/2 to avoid potential overflow

	      do
		{
		  while (i<a.size() && ((Comparable)a.get(i)).lessThan(m))
		    i++;

		  while (j>=0 && m.lessThan((Comparable)a.get(j)))
		    j--;

		  if (i <= j)
		    {
		      if (i != j)
			{
			  temp = a.get(i);
			  a.set(i,a.get(j));
			  a.set(j,temp);
			}
		      i++;
		      j--;
		    }
		}
	      while (i <= j);


	      if (i < hi)
		{
		  stack.push(hi);
		  stack.push(i);
		}

	      hi = j;
	    }
	}
      while (!stack.isEmpty());

      return a;
    }

  /* ************************************************************************ */

  /**
   * An empty iterator that may come handy.
   */
  public static final Iterator EMPTY_ITERATOR = new EmptyIterator();

  private static class EmptyIterator implements Iterator
    {
      public final boolean hasNext()
        {
	  return false;
	}

    public final Object next ()
        {
	  return null;
	}

    public final void remove ()
        {
	}
    }

  /* ************************************************************************ */
  
  // /**
  //  * This creates a single generic cloner for any object using the
  //  * deepClone(Object) and deepClone(Object) methods.
  //  */
  // private static Cloner cloner = new Cloner();

  // /**
  //  * Returns a shallow clone copy of the given object.
  //  */
  // public static Object shallowClone (Object o)
  //   {
  //     return cloner.shallowClone(o);
  //   }    

  // /**
  //  * Returns a deep clone copy of the given object.
  //  */
  // public static Object deepClone (Object o)
  //   {
  //     return cloner.deepClone(o);
  //   }    

  /* ************************************************************************ */
}

//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.tools;

/**
 * This class implements a simple UNIX style command parser. It is meant to
 * be subclassed or used directly through its static methods (which is why it
 * has no constructor). Its method <tt>parseCommandLine(String[])</tt> takes
 * the array of strings on command line (<i>i.e.</i>, the argument of a
 * <tt>main</tt> method) and determines which options are used with what
 * values.  It also defines a few utilities (1) to set and access the values of
 * the options and arguments specified in the parsed command, (2) to determine
 * if these are present, and (3) to do simple file name completion.
 *
 * @version     Last modified on Sat Aug 04 05:11:45 2018 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

import java.util.*;
import hlt.language.io.FileTools;

public class Command
{
  /**
   * Options storage.
   */
  private static HashMap optionTable = new HashMap();
  /**
   * Command arguments.
   */
  private static String[] arguments = null;
  /**
   * Default argument.
   */
  private static String defaultArgument = null;

  /**
   * The widest length of option names.
   */
  private static int optionWidth = " Option".length();

  /**
   * The widest length of option defaults.
   */
  private static int defaultWidth = "(Default)".length();

  /**
   * Sets the default argument to the specified string.
   *
   * @param arg value to use if the command argument is missing
   */
  public final static void optionalArgument (String arg)
    {
      defaultArgument = arg;
    }

  /**
   * Used locally to check whether the specified option is legal.
   * If this option shares a prefix with one already defined,
   * a <b><tt>CommandException</tt></b> is reported.
   */
  private final static void checkOption (String option)
    {
      for (Iterator e = optionTable.values().iterator(); e.hasNext();)
        {
          OptionEntry entry = (OptionEntry)e.next();
          if (entry.name.startsWith(option) || option.startsWith(entry.name))
            throw new CommandException
              ("\nAttempt to define option '"+option+
               "' which conflicts with defined option '"+entry.name+"'");
        }
    }     

  /**
   * Defines an option with a default value.
   *
   * @param option the option string
   * @param defaultValue the option's default
   */
  public final static void defineOption (String option, String defaultValue)
    {
      checkOption(option);
      optionTable.put(option,new OptionEntry(option,defaultValue));
      optionWidth = Math.max(optionWidth,option.length()+1);
      defaultWidth = Math.max(defaultWidth,defaultValue.length()+2);
    }

  /**
   * Defines an option with a default value and description.
   *
   * @param option the option string
   * @param defaultValue the option's default
   * @param help option's description 
   */
  public final static void defineOption (String option, String defaultValue, String help)
    {
      checkOption(option);
      optionTable.put(option,new OptionEntry(option,defaultValue,help));
      optionWidth = Math.max(optionWidth,option.length()+1);
      defaultWidth = Math.max(defaultWidth,defaultValue.length()+2);
    }

  /**
   * Returns the value associated with an option. If the option is
   * undefined, a <b><tt>CommandException</tt></b> is reported.
   *
   * @param option the option
   */
  public final static String getOption (String option)
    {
      OptionEntry entry = (OptionEntry)optionTable.get(option);
      if (entry == null)
        throw new CommandException("Unknown command option ("
                                   +option+")");
      if (entry.value == null)
        return entry.defaultValue;
      return entry.value;
    }

  /**
   * Returns true iff the option is actually present on the command line.
   * If the option is undefined, a <b><tt>CommandException</tt></b>
   * is reported.
   *
   * @param option the option
   */
  public final static boolean optionIsPresent (String option)
    {
      OptionEntry entry =(OptionEntry)optionTable.get(option);
      if (entry == null)
        throw new CommandException("Unknown command option ("
                                   +option+")");
      return (entry.value != null);
    }

  /**
   * Returns the value of the argument. If none is specified, returns the
   * default argument. If many are specified, the first is returned.
   */
  public final static String getArgument ()
    {
      if (arguments == null)
        return defaultArgument;
      return arguments[0];
    }

  /**
   * Returns the array of values of all the arguments. If none is specified,
   * returns a one element array containing the default argument.
   */
  public final static String[] getArguments ()
    {
      if (arguments == null)
        {
          String[] a = {defaultArgument};
          return a;
        }
      return arguments;
    }

  /**
   * Returns true iff an argument is actually present on the command line.
   */
  public final static boolean argumentIsPresent ()
    {
      return arguments != null;
    }

  /**
   * Returns a file's name without its extension, but including its directory path.
   *
   * @param name the file's name
   */
  public final static String fullFileNamePrefix (String name)
    {
      return FileTools.fullPrefix(name);
    }

  /**
   * Returns a file's name without its extension nor its directory path.
   *
   * @param name the file's name
   */
  public final static String fileNamePrefix (String name)
    {
      return FileTools.prefix(name);
    }

  /**
   * Returns a file's name's directory part.
   *
   * @param name the file's name
   */
  public final static String fileNameDir (String name)
    {
      return FileTools.dir(name);
    }

  /**
   * Returns a file's name extension.
   *
   * @param name the file's name
   */
  public final static String fileNameSuffix (String name)
    {
      return FileTools.suffix(name);
    }

  /**
   * Returns a file's name completed with its extension if the latter
   * is missing.
   *
   * @param name the file's name
   * @param ext the file's extension
   */
  public final static String completeFileName (String name, String ext)
    {
      String suffix = "."+ext;
      if (name.endsWith(suffix)) return name;
      return name+suffix;         
    }

  /**
   * Prints an item on a given maximum field width.
   */
  private static void padWrite (String item, int width)
    {
      System.err.print(item);
      for (int i=item.length(); i<=width; i++) System.err.print(" ");
    }
  
  /**
   * Prints a line description of an option.
   */
  private static void printHelpLine (String option, String value, String description)
    {
      padWrite(option,optionWidth);
      padWrite(value,defaultWidth);
      System.err.println(description);
    }  

  private static String usage = null;

  public static String getUsage ()
    {
      return usage;
    }
  
  public static void setUsage (String usg)
    {
      usage = usg;
    }

  public static void printUsage ()
    {
      if (usage != null) System.err.println(usage);
    }

  /**
   * Prints a listing of known options, their defaults, and descriptions.
   */
  public final static void printHelp ()
    {
      printUsage();
      
      if (optionTable.size() == 0)
        System.err.println("Sorry - no help available!");
      else
        {
          printHelpLine("Option ","(Default)","Description");
          printHelpLine("------ ","---------","-----------");
          for (Iterator e = optionTable.values().iterator(); e.hasNext();)
            {
              OptionEntry entry = (OptionEntry)e.next();
              printHelpLine("-"+entry.name,"("+entry.defaultValue+")",entry.help);
            }
          System.err.println();
        }
    }

  /**
   * Parses the command line recognizing the defined options and the
   * arguments, and storing their specified values as appropriate.
   * Note that if the same option is specified more than once on the
   * command line, only the last one is effective.
   */    
  public final static boolean parseCommandLine (String args[])
    {
      OptionEntry option = null;
      int i = 0;

      while (i < args.length)                   // as long as there are arguments
        {
          if (args[i].charAt(0) == '-')         // if this argument starts with '-', it is an option
            {
              boolean known = false;            // a flag to indicate that this option is known
              boolean hasValue = false;         // a flag to indicate that this option is the prefix of its value (no space separates them)
              args[i] = args[i].substring(1);   // keep what follows the '-'
              int full = args[i].length();      // the full length of the argument
              if (full > 0)                     // if there is something after the '-'
                for (Iterator e = optionTable.values().iterator(); e.hasNext();)      // for all known options
                  {
                    option = (OptionEntry)e.next();      // let option be the next known option
                    if (args[i].startsWith(option.name))        // if it matches the start of this argument
                      {
                        int part = option.name.length();        // let part be the length of the option name
                        if (hasValue = (part < full))           // the option has a value if is name is shorter than the argument
                          option.value = args[i].substring(part,full);  // extracts the value as the tail of the argument
                        known = true;                           // remember that we found this option
                        break;                                  // no need to look further.
                      }
                  }

              if (!known)                       // this is not a known option
                {
                  printHelp();                  // tell what options are known
                  return false;                 // the parsing fails
                }
              // this is a known option
              if (hasValue)                     // and its value is part of it
                {
                  i++;                          // proceed to the next argument
                  continue;
                }
              // is there a value for this option?
              if (i < args.length-1)            // if there are more arguments
                {
                  if (args[i+1].charAt(0) != '-')       // if the next argument arg is not an option
                    if (option.defaultValue.length() > 0)       // then it is the value of the previous option
                      {
                        option.value = args[i+1];               // take this argument as that value
                        i += 2;                                 // and skip this argument
                        continue;
                      }
                }

              if (option.value == null && option.defaultValue.length() == 0)    // this option does not need a value
                option.value = "";                                              // so just record it as present
              i++;
            }
          else
            {
              arguments = new String[args.length-i];
              System.arraycopy(args,i,arguments,0,args.length-i);
              break;
            }
        }
      return true;              // the parse succeeds!
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * This defines a class gathering information about an an option.
   * An object of this class will be stored in a Hashmap. Note that the
   * name of the option is part of this stucture. This is to avoid a
   * superfluous lookup for the key when iterating over the elements of
   * the option tables.
   */
  static class OptionEntry
    {
      /**
       * Name of the option.
       */
      String name;
      /**
       * Value of the option specified on the command line.
       */
      String value;
      /**
       * Value to use if none is specified for this option.
       */
      String defaultValue;
      /**
       * Some description of the purpose of this option.
       */
      String help = "";
      
      /**
       * Constructs an OptionEntry for option name. It provides a default value.
       *
       * @param name option
       * @param defaultValue option's default
       */
      OptionEntry (String name, String defaultValue)
        {
          this.name = name;
          this.defaultValue = defaultValue;
        }

      /**
       * Constructs an OptionEntry for option name. It provides a default value,
       * and some description for help.
       *
       * @param name option
       * @param defaultValue option's default
       * @param help option's description
       */
      OptionEntry (String name, String defaultValue, String help)
        {
          this.name = name;
          this.defaultValue = defaultValue;
          this.help = help;
        }
    }

}


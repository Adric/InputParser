/**
 * @author Adric
 * @created 2014-10-13
 * @brief A wrapper to Scanner that blocks the program until a single input is entered
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public 
 * License along with this program.  If not, see 
 * <http://www.gnu.org/licenses/>.
 */

import java.util.Scanner;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

/**
 * This class is actually a factory, storing different InputParser objects
 * for different InputStreams. Different character encodings are not supported.
 * Base-10 is assumed throughout.
 * 
 * Normally this kind of code duplication would be prime territory for generics,
 * but java doesn't have a way of knowing what type of generic it has at runtime
 * See: https://docs.oracle.com/javase/tutorial/java/generics/erasure.html
 * 
 * Increment this number with each version change:
 * Version 1.4
 * 
 * @author Adric
 */

class InputParser
{
	/**
	 * Scanner for each parser
	 */
	private Scanner mScanner;
	/**
	 * Instance count. Acts as the key for the instance map
	 */
	private static int mInstanceCount = 0;
	/**
	 * Special key for when an instance isn't found
	 */
	private static final int KEY_NOT_FOUND = -1;
	/**
	 * Map of InputParsers for InputStreams
	 * The key is actually the instance count, which is iterated with each instance added.
	 * It's an integer because InputStreams can't be used as keys.
	 */
	private static TreeMap<Integer, Map.Entry<InputStream, InputParser> > mInputStreams = new TreeMap<Integer, Map.Entry<InputStream, InputParser> >();
	/**
	 * Enum representing which type of input to get
	 */
	private enum EType { STRING, BOOL, CHAR, BYTE, SHORT, INT, UINT, LONG, ULONG, FLOAT, DOUBLE, LINE }
	/**
	 * Map of input types and their String representations
	 */
	private static TreeMap<EType, String> mEMap = new TreeMap<EType, String>();
	/**
	 * Allows a string to be empty (Strings only. Not Line)
	 */
	private boolean mAllowEmptyStrings = false;
	
	private InputParser(InputStream in)
	{
		/**
		 * Scanner's readLine() function doesn't always read an entire line
		 * so make sure we set the last newline as our delimiter 
		 * (\f and \r will be stripped by trim())
		 */
		mScanner = new Scanner(in).useDelimiter("\\b\\n\\b$");
		if (mEMap.isEmpty())
		{
			mEMap.put(EType.STRING,	"String");
			mEMap.put(EType.CHAR, 	"Character");
			mEMap.put(EType.BOOL, 	"Boolean");
			mEMap.put(EType.BYTE, 	"Byte");
			mEMap.put(EType.SHORT, 	"Short");
			mEMap.put(EType.INT, 	"Integer");
			mEMap.put(EType.UINT,	"Unsigned Integer");
			mEMap.put(EType.LONG, 	"Long");
			mEMap.put(EType.ULONG, 	"Unsigned Long");
			mEMap.put(EType.FLOAT, 	"Float");
			mEMap.put(EType.DOUBLE, "Double");
			mEMap.put(EType.LINE, 	"Line");
		}
	}
	
	private InputParser()
	{
		this(System.in);
	}
	
	/**
	 * Creates a new InputParser object that pulls values from System.in
	 * If an InputParser already exists, returns a reference to that parser
	 */
	public static InputParser create()
	{
		return create(System.in);
	}
	
	/**
	 * Removes the InputParser associated with InputStream in from the instance list
	 * (but does not close the stream itself)
	 * Be sure to set any InputParser variables to null to delete them
	 * @return Returns true if successfully found and removed, false if not found
	 */
	public boolean destroy(InputStream in)
	{
		for (Map.Entry<Integer, Map.Entry<InputStream, InputParser>> entry : mInputStreams.entrySet())
		{
			if (entry.getValue().getKey().equals(in))
			{
				mInputStreams.remove(entry.getKey());
				if (mInputStreams.isEmpty())
				{
					mEMap.clear();
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Removes all InputParser instances from the instance list (but does not close any InputStreams)
	 */
	public void destroyAll()
	{
		mInputStreams.clear();
		mEMap.clear();
	}
	
	/**
	 * Returns true if the InputStream was found and closed
	 */
	public boolean destroyClose(InputStream in)
	{
		boolean ret = destroy(in);
		if (ret)
		{
			try
			{
				in.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
				ret = false;
			}
		}
		return ret;
	}
	
	/**
	 * Closes all InputStreams and remove all InputParsers from the list
	 */
	public void destroyCloseAll()
	{
		for (Map.Entry<Integer, Map.Entry<InputStream, InputParser>> entry : mInputStreams.entrySet())
		{
			try
			{
				entry.getValue().getKey().close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		destroyAll();
	}
	
	/**
	 * Creates a new InputParser object that pulls values from an InputStream
	 * If an InputParser already exists, returns a reference to that parser
	 * If an InputParser already exists but is set to a different stream,
	 * returns a new InputParser set to the new InputStream
	 * @param in	InputStream to be associated with an InputParser. If an InputParser doesn't exist, 
	 * 				a new one will be created and added to the instance list.
	 */
	public static InputParser create(InputStream in)
	{
		int key = KEY_NOT_FOUND;
		if (mInputStreams.isEmpty() || ((key = getInstanceKey(in)) == KEY_NOT_FOUND))
		{
			mInstanceCount++;
			mInputStreams.put(mInstanceCount, new AbstractMap.SimpleImmutableEntry<>(in, new InputParser(in)));
			return mInputStreams.get(mInstanceCount).getValue();
		}
		return mInputStreams.get(key).getValue();
	}
	
	/**
	 * Returns the map key associated with an InputStream
	 * Returns KEY_NOT_FOUND if not found 
	 * @param in	The InputStream must be in the instance list. It won't be created if not
	 */
	private static int getInstanceKey(InputStream in)
	{
		if (!mInputStreams.isEmpty())
		{
			for (Map.Entry<Integer, Map.Entry<InputStream, InputParser>> entry : mInputStreams.entrySet())
			{
				if (entry.getValue().getKey().equals(in))
				{
					return entry.getKey();
				}
			}
		}
		return KEY_NOT_FOUND;
	}
	
	/**
	 * Returns the max characters in a type's string, including negatives
	 * Returns Integer.MAX (strlen max) if no max exists
	 */
	private int getMaxStrLen(EType type)
	{
		switch(type)
		{
			case CHAR:
				return 1;
			case BYTE:
				return 4;
			case SHORT:
				return 6;
			case INT:
			case UINT:
				return 11;
			case LONG:
			case ULONG:
				return 20;
			case FLOAT:
				return 154;
			case DOUBLE:
				return 1079;
			/*case STRING:
				return Integer.MAX_VALUE;*/ // Just for reference
			default:
				return Integer.MAX_VALUE;
		}
	}
	
	/**
	 * Prompt the user to enter a new input if type conversion failed
	 */
	private static void promptInvalidType(EType type, String value)
	{
		System.err.println("Invalid " + mEMap.get(type) + " entered: " + value);
		System.out.print("Try again: ");
	}
	
	/**
	 * Does the actual work of getting a value from the Scanner's buffer
	 * Note that this is not threadsafe
	 */
	private String getValue(EType type)
	{
		if (type != EType.LINE)
		{
			do
			{
				try
				{
					/**
					 * Have to call nextLine() in order to get the full line, otherwise functions
					 * like nextInt() will leave newlines in the buffer. Scanner's hasNext() also won't
					 * do what we want: it only returns false only when an EOL is entered, e.g. CTRL+C
					 */
					String value = mScanner.nextLine().trim();
					if (value == null)
					{
						throw new InputStringEmpty(value);
					}
					String[] split = value.split("\\s++");
					if (split.length != 1)
					{
						throw new InputRangeError(value);
				    }
					// special input length rules
					if (split[0] == null || (value = split[0].trim()).isEmpty())
					{
						throw new InputStringEmpty(value);
					}
					if ((value.length() > getMaxStrLen(type)) || 
						(value.length()+1 < 0)) // Integer overflow, can this happen with strings?
					{
						throw new InputRangeError(value);
					}
					return value; // should never be null
				}
				catch (NoSuchElementException e)
				{
					e.printStackTrace();
				}
				catch (InputRangeError e)
				{
					switch (type)
					{
						// Note LINE should be checked well before here
						case STRING:
							try
							{
								System.in.skip(System.in.available());
							}
							catch (IOException e1)
							{
								e1.printStackTrace();
							}
							System.err.println("Enter one " + mEMap.get(type) + " at a time");
							break;
						case CHAR:
							System.err.println("Must enter a single " + mEMap.get(type));
							break;
						case BYTE:
							System.out.println("Invalid " + mEMap.get(type) + " length (must be -128 to 127)");
							break;
						case SHORT:
							System.err.println("Must enter a " + mEMap.get(type) + " between " + Short.MIN_VALUE + " and " + Short.MAX_VALUE);
							break;
						case INT:
							System.err.println("Must enter an " + mEMap.get(type) + " between " + Integer.MIN_VALUE + " and " + Integer.MAX_VALUE);
							break;
						case UINT:
							System.err.println("Must enter an " + mEMap.get(type) + " between " + 0 + " and " + Integer.MAX_VALUE);
						case LONG:
							System.err.println("Must enter a " + mEMap.get(type) + " between " + Long.MIN_VALUE + " and " + Long.MAX_VALUE);
							break;
						case ULONG:
							System.err.println("Must enter a " + mEMap.get(type) + " between " + 0 + " and " + Long.MAX_VALUE);
							break;
						case FLOAT:
							System.err.println("Must enter a " + mEMap.get(type) + " between " + Float.MIN_VALUE + " and " + Float.MAX_VALUE);
							break;
						case DOUBLE:
							System.err.println("Must enter a " + mEMap.get(type) + " between " + Double.MIN_VALUE + " and " + Double.MAX_VALUE);
							break;
						case BOOL:
						default:
							System.err.println("Must enter a valid " + mEMap.get(type));
							break;
					}
					System.out.print("Try again: ");
					continue;
				}
				catch (InputStringEmpty e)
				{
					if (type == EType.STRING && mAllowEmptyStrings)
					{
						// Didn't enter a string or everything was trimmed, assume blank
						return "";
					}
					String article = (type == EType.INT) ? "an " : "a ";
					System.err.println("Must enter " + article + mEMap.get(type));
					System.out.print("Try again: ");
					continue;
				}
			}
			while (true);
		}
		else // EType.LINE
		{
			while (true)
			{
				try
				{
					return mScanner.nextLine().trim();
				}
				catch(Exception e)
				{
					System.err.println(e);
					continue;
				}
			}
		}
	}
	
	/**
	 * Gets a single String from the input buffer
	 * @return
	 */
	public String getString()
	{
		mAllowEmptyStrings = false;
		return getValue(EType.STRING);
	}
	
	/**
	 * Gets a single String from the input buffer
	 * If allow_empty_strings is true, empty inputs are allowed (and return "")
	 * If allow_empty_strings is false, an inputs must be at least length 1
	 */
	public String getString(boolean allow_empty_strings)
	{
		mAllowEmptyStrings = allow_empty_strings;
		return getValue(EType.STRING);
	}
	
	/**
	 * Gets a full line from the input buffer
	 */
	public String getLine()
	{
		return getValue(EType.LINE);
	}
	
	/**
	 * Returns a boolean. Can enter True, T, or 1 (case insensitive) for true, 
	 * 					  False, F, or 0 (case insensitive) for false
	 */
	public boolean getBool()
	{
		String value = null;
		while (true)
		{
			try
			{
				value = getValue(EType.BOOL);
				if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("t") || value.equals("1"))
				{
					return true;
				}
				else if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("f") || value.equals("0"))
				{
					return false;
				}
				else
				{
					// Boolean.valueOf() doesn't do what we want here
					throw new NumberFormatException();
				}	
			}
			catch (NumberFormatException e)
			{
				promptInvalidType(EType.BOOL, value);
				continue;
			}
		}
	}
	
	/**
	 * Gets a byte from the input stream
	 */
	public Byte getByte()
	{
		String value = null;
		while (true)
		{
			try
			{
				value = getValue(EType.BYTE);
				return Byte.valueOf(value);
			}
			catch (NumberFormatException e)
			{
				promptInvalidType(EType.BYTE, value);
				continue;
			}
		}
	}
	
	/**
	 * Gets a single char from the input stream
	 */
	public char getChar()
	{
		String value = null;
		while (true)
		{
			try
			{
				value = getValue(EType.CHAR);
				return Character.valueOf(value.charAt(0));
			}
			catch (NumberFormatException | NullPointerException e)
			{
				promptInvalidType(EType.CHAR, value);
				continue;
			}
		}
	}
	
	/**
	 * Gets a short from the input stream
	 */
	public short getShort()
	{
		String value = null;
		while (true)
		{
			try
			{
				value = getValue(EType.SHORT);
				return Short.valueOf(value);
			}
			catch (NumberFormatException e)
			{
				promptInvalidType(EType.SHORT, value);
				continue;
			}
		}
	}
	
	/**
	 * Gets an integer from the input stream
	 */
	public int getInt()
	{
		String value = null;
		while (true)
		{
			try
			{
				value = getValue(EType.INT);
				return Integer.valueOf(value);
			}
			catch (NumberFormatException e)
			{
				promptInvalidType(EType.INT, value);
				continue;
			}
		}
	}
	
	/**
	 * Gets an unsigned integer from the input stream
	 */
	public int getUInt()
	{
		String value = null;
		while (true)
		{
			try
			{
				value = getValue(EType.UINT);
				return Integer.parseUnsignedInt(value);
			}
			catch (NumberFormatException e)
			{
				promptInvalidType(EType.UINT, value);
				continue;
			}
		}
	}
	
	/**
	 * Gets a long from the input stream
	 */
	public long getLong()
	{
		String value = null;
		while (true)
		{
			try
			{
				value = getValue(EType.LONG);
				return Long.valueOf(value);
			}
			catch (NumberFormatException e)
			{
				promptInvalidType(EType.LONG, value);
				continue;
			}
		}
	}
	
	/**
	 * Gets an unsigned long from the input stream
	 */
	public long getULong()
	{
		String value = null;
		while (true)
		{
			try
			{
				value = getValue(EType.ULONG);
				return Long.parseUnsignedLong(value);
			}
			catch (NumberFormatException e)
			{
				promptInvalidType(EType.ULONG, value);
				continue;
			}
		}
	}
	
	/**
	 * Gets a float from the input stream
	 */
	public float getFloat()
	{
		String value = null;
		while (true)
		{
			try
			{
				value = getValue(EType.FLOAT);
				return Float.valueOf(value);
			}
			catch (NumberFormatException e)
			{
				promptInvalidType(EType.FLOAT, value);
				continue;
			}
		}
	}
	
	/**
	 * Gets a double from the input stream
	 */
	public double getDouble()
	{
		String value = null;
		while (true)
		{
			try
			{
				value = getValue(EType.DOUBLE);
				return Double.valueOf(value);
			}
			catch (NumberFormatException e)
			{
				promptInvalidType(EType.DOUBLE, value);
				continue;
			}
		}
	}
	
	/**
	 * Prompts the user to enter y/n, for example to exit.
	 * Returns true if the first string the parser finds is "y" or "yes" (case-insensitive).
	 * Returns false if the first string the parser finds is "n" or "no" (case-insensitive).
	 * @param prompt String to display to the user until they enter a valid answer,
	 * 				 for example "Are you sure you want to exit? (Y/N): ".
	 * 				 Can be null, but not recommended when using System.in.
	 */
	public boolean getYesNo(String prompt)
	{
		while (true)
		{
			if (prompt != null)
			{
				System.out.print(prompt);
			}
			String str = getString().toUpperCase();
			switch (str)
			{
				case "N":
				case "NO":
					return false;
				case "Y":
				case "YES":
					return true;
				default:
					//System.err.println("Must enter either \"Y\" or \"N\"");
					break;
			}
		}
	}
	
	/**
	 * Returns true if the first string the parser finds is "y" or "yes" (case-insensitive).
	 * Returns false if the first string the parser finds is "n" or "no" (case-insensitive).
	 */
	public boolean getYesNo()
	{
		return getYesNo(null);
	}
}

@SuppressWarnings("serial")
class InputStringEmpty extends Exception
{
	String msg = null;
	InputStringEmpty(String msg)
	{
		super(msg);
		this.msg = msg;
	}
	
	InputStringEmpty(Throwable cause)
	{
		super(cause);
	}
	
	@Override
	public String toString()
	{
		return msg;
	}
}

@SuppressWarnings("serial")
class InputRangeError extends Exception
{
	String msg = null;
	InputRangeError(String msg)
	{
		super(msg);
		this.msg = msg;
	}
	
	InputRangeError(Throwable cause)
	{
		super(cause);
	}
	
	@Override
	public String toString()
	{
		return msg;
	}
}
/**
 * @author Adric
 * @created 2014-10-13
 * @brief Sample code for how to use the Input Parser class
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

public class InputParserSample 
{
	public static void main(String[] args) 
	{
		// Create the parser
		InputParser parser = InputParser.create();
		
		// Main loop
		while (true)
		{
			// Get a string
			System.out.print("What's your first name? ");
			String name = parser.getString();
			
			// Get a yes/no
			System.out.print("Hello " + name + ". Would you like to solve a puzzle? (y/n) ");
			if (!parser.getYesNo())
			{
				System.out.println("I'm sorry you're no fun.");
				break;
			}
			
			// Get an unsigned integer
			System.out.print("What's your age? ");
			System.out.println(parser.getUInt() + " is a good year!");
			
			// Get char
			System.out.println("Now the puzzle...");
			do
			{
				System.out.print("Is this a 1 or an l? ");
			}
			while (parser.getChar() != 'l');
			
			// Get bool
			System.out.print("Congrats! You got it!\nAlthough... was that a silly puzzle? (t/f) ");
			if (parser.getBool())
			{
				System.out.println("I'm glad you think so too.");
			}
			else
			{
				System.out.println("You're more forgiving than I am...");
			}
			
			// Get a yes/no with a prompt
			if (!parser.getYesNo("Go again? (YES/NO) "))
			{
				System.out.println("Goodbye!");
				break;
			}
		}
		
		// Clean up
		parser.destroyCloseAll();
		parser = null;
	}
}

package hashingQuestion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class hashingRunner
{
	static ArrayList<String> indirectionArray;
	static ArrayList<ArrayList<String>> values;
	static Random rand;
	public static int size = 2;
	static final int valueMax  = 2;
	public static void main(String args[])
	{
		//initialize arrays ////
		indirectionArray = new ArrayList<>();
		indirectionArray.add("0");
		indirectionArray.add("1");
		values = new ArrayList<>();
		values.add(new ArrayList<String>());
		values.add(new ArrayList<String>());  //two for 0 and 1
		////////////////////////////////	
		rand = new Random();
		ArrayList<Integer> randList = new ArrayList<Integer>();
		//Student ID is 013472914, so the number of values to be inserted is 11
		
		//This slow method is to guarantee that each value is unique.
		for(int i = 0; i < 11; i++)
		{
			int v = rand.nextInt(16);
			boolean flag = false;
			for(int j = 0; j < randList.size(); j++)
			{
				if(v == randList.get(j))
				{
					flag = true;
				}
			}
			if(!flag)
			{
				randList.add(v);
			}
			else
			{
				i--;
			}
			
		}
		Collections.shuffle(randList);
		
		
		for(int i = 0; i < 11; i++)
		{
			String formatPattern = "%" + 4 + "s";
			insert(String.format(formatPattern, Integer.toBinaryString(randList.get(i))).replace(' ', '0'));
		}
		for(int i = 0; i < indirectionArray.size(); i++)
		{
			ArrayList<String> temp = values.get(i);
			System.out.println("This is bucket: " + indirectionArray.get(i));
			for(int j = 0; j < temp.size(); j++)
			{
				System.out.println(temp.get(j));
			}
		}

	}
	public static void insert(String val)
	{
		for(int i = 0; i < indirectionArray.size(); i++)
		{
			String indStr = indirectionArray.get(i);
			//System.out.println("This is indStr: " + indStr);
			
			//get the size of the current string held in indirection array,
			//will determine how many characters to look at 
			int length = indStr.length(); 
	
			if(indStr.equals(val.substring(0, length)))
			{
				ArrayList<String> temp = values.get(i);
				int currentSize = temp.size();
				if(currentSize >= valueMax)
				{
					resize(val, i);
					break;
				}
				else
				{
					temp.add(val);
					break;
				}	
			}
		}
	}
	public static void resize(String val, int index)
	{
		updateIndirectionArray(index);
		updateValueArray();
		insert(val);
	}
	public static void updateIndirectionArray(int index)
	{
		String binary = indirectionArray.get(index);
		indirectionArray.set(index, binary + "0");
		indirectionArray.add(index+1, binary+ "1");
		size = indirectionArray.size() + 1; //should increase by one each time it is increased. Just split the entry 
	}
	public static void updateValueArray()
	{
		ArrayList<ArrayList<String>> temp = values;
		values = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < size; i++)
		{
			values.add(new ArrayList<String>());
		}
		moveValues(temp);
	}
	public static void moveValues(ArrayList<ArrayList<String>> oldList)
	{
		for(int i = 0; i < oldList.size(); i++)
		{
			ArrayList<String> temp = oldList.get(i);
			for(int j = 0; j < temp.size(); j++)
			{
				//System.out.println(temp.get(j));
				insert(temp.get(j));
			}
		}

	}
}



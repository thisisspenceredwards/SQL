package cs157bhw2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class QuadTreeManager {

	public static void main(String[] args)
	{
		if(args.length != 2)
		{
			System.out.println("Wrong number of arguments");
			System.exit(-1);
		}
		QuadTreeManagerObject manager = new QuadTreeManagerObject(args[0]);
		File file = new File(args[1]);
		
		Scanner scan;
		String input;
		try {
			scan = new Scanner(file);
			
			while(scan.hasNext())
			{
				input = scan.nextLine();
				String[] inputArray = input.split("\\s+");
				if(inputArray.length > 4)
				{
					String operation = inputArray[0];
					String fileName = inputArray[1];
					operation = operation.toLowerCase();
					switch(operation)
					{
						case "c":
							if(inputArray.length != 6)
							{
								System.out.println("Improper formatting");
								break;
							}
							Float low_x = Float.parseFloat(inputArray[2]);
							Float low_y = Float.parseFloat(inputArray[3]);
							Float high_x = Float.parseFloat(inputArray[4]);
							Float high_y = Float.parseFloat(inputArray[5]);
							//System.out.println("C!");
							manager.createQuadTree(fileName, low_x, low_y, high_x, high_y);
							break;
							case "i":
							if(inputArray.length != 5)
							{
								System.out.println("Improper formatting");
								break;
							}
							String label = inputArray[2];
							float x_value = Float.parseFloat(inputArray[3]);
							float y_value = Float.parseFloat(inputArray[4]);
							QuadRecord quad = new QuadRecord(label, x_value, y_value);
							manager.add(fileName,  quad);
							break;
						case "l":
							if(inputArray.length != 8)
							{
								System.out.println("Improper formatting");
								break;
							}
							Float p1_x = Float.parseFloat(inputArray[2]);
							Float p1_y = Float.parseFloat(inputArray[3]);
							Float p2_x = Float.parseFloat(inputArray[4]);
							Float p2_y = Float.parseFloat(inputArray[5]);
							int lim_offset = Integer.parseInt(inputArray[6]);
							int lim_count = Integer.parseInt(inputArray[7]);
							QuadRectangle rec = new QuadRectangle(-9, new QuadPoint(p2_x, p2_y), new QuadPoint(p1_x, p1_y));
							QuadRectangle[] list = manager.lookupRectangle(fileName, rec);	
							QuadRecord[] points = manager.lookupPoint(fileName, new QuadPoint(p1_x, p1_y),  new QuadPoint(p2_x, p2_y), lim_offset, lim_count);
							manager.cleanArrayList();
							if(list == null || list.length == 0)
							{
								System.out.println("No Rectangles Intersect");
	
							}
							else
							{
								System.out.println("Query intersects with the following rectangles in the quad tree:");
								for(int i  = 0; i < list.length; i++)
								{
									System.out.println("Rect_" + (i+1) + ":" +list[i].toString());
								}
							}
							if(points == null || points.length == 0)
							{
								System.out.println("No Points Found");
								break;
							}
							System.out.println("Quad tree records satisfying the query:");
							for(int i  = 0; i < points.length; i++)
							{
								System.out.println("(" + points[i].label + ", " + points[i].point.x + ", " + points[i].point.y + ")");
							}
							
							
							break;
						default:
							System.out.println("Invalid operation: Options are c, i, l");
					}
				}
				else
				{
					System.out.println("bad input");
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}	
	}

}

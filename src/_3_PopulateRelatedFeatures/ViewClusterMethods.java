package _3_PopulateRelatedFeatures;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;

import DataObjects.Method;
import db_access_layer.DatabaseAccessLayer;

public class ViewClusterMethods {

	public static void main(String args[]) throws ClassNotFoundException, SQLException, IOException
	{	//15 and 8 is a really great suggestion of related features
		//input cluster ID
		int clusterID =27;
		//get the method IDs in this cluster along with projectID and fileName 
		DatabaseAccessLayer dbLayer = DatabaseAccessLayer.getInstance();
		dbLayer.initializeConnectorToDisplayMethodBodies();				
		ArrayList<DataObjects.Method> methods = dbLayer.getMethods(clusterID);
		//and display their bodies
		//iterate over methodIDs and display each method body
		for(DataObjects.Method m: methods)
		{
			ArrayList<String> body = getMethodBody(m);
			ArrayList<String> api_calls = dbLayer.getMethodAPICalls(m);
			System.out.println("-----------------------------");
			System.out.println("Project ID:" + m.projectID);
			System.out.println("Method ID:" + m.id);
			System.out.println("Method Name:" + m.name);
			System.out.println("File Name:" + m.file_name);
			//System.out.println("Method Body:");
			//for(String s: body)
			//{
			//	System.out.println(s);
			//}
			System.out.println("Method API calls:");
			for(String s: api_calls)
			{
				System.out.println(s);
			}
		}
		dbLayer.closeConnector();	
		
		
	}
	
	

	public static ArrayList<String> getMethodBody(DataObjects.Method m) throws IOException
	{
		ArrayList<String> s = new ArrayList<String>();
				
			String file_path = m.file_name;
			int from_line = m.from_line_num;
			int to_line = m.to_line_num;
			
			int line_num = from_line;
			try{
			while(line_num <= to_line)
			{
				String line = Files.readAllLines(Paths.get(file_path)).get(line_num-1);
				s.add(line);
				line_num++;
			}
			}catch(Exception ex)
			{
				System.out.println("No data available...something wrong with file");
			}
		
		return s; 
		
	}
	
}

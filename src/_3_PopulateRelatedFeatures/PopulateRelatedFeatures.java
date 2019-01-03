package _3_PopulateRelatedFeatures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import db_access_layer.DatabaseAccessLayer;

public class PopulateRelatedFeatures {

	public static void main(String args[]) throws Exception
	{
		//read the file output by spmf fpmax miner
		//detect #SUP:
		//the output is clusterIDs that appear together across projects
		
		//read file line by line and add to an arraylist of strings
		String fileName = "output_musicplayer_fpmax_all.txt";
		int minSup = 3;
		int minDepth = 2;
		ArrayList<String> relatedFeaturesList = getCoOccurringFeatures(fileName, minSup);
		
		//save the clusterIDs in a related_features table (id, feature_id, cluster_id)
		//each itemset that has a min support of 2 a min depth of two should be stored 
		DatabaseAccessLayer dbLayer = DatabaseAccessLayer.getInstance();
		dbLayer.initializeConnectorToPopulateRelatedFeatures();				
		dbLayer.populateRelatedFeaturesTable(relatedFeaturesList, minDepth);
		dbLayer.closeConnector();		
	}

	private static ArrayList<String> getCoOccurringFeatures(String fileName, int minSup) throws IOException {
		
		ArrayList<String> relatedFeaturesList = new ArrayList<String> ();
		File file = new File(fileName);
		 
		BufferedReader br = new BufferedReader(new FileReader(file));
		 
		String st;
		while ((st = br.readLine()) != null)
		{
			int support = Integer.parseInt(st.substring(st.indexOf(":")+2));
			if(support >= minSup)
			{
			st = st.substring(0, st.indexOf("#")-1);			
			System.out.println(st);
			relatedFeaturesList.add(st);
			}
		}
		 		
		return relatedFeaturesList;
	}
}

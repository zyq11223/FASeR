package _4_RetrieveRelatedFeatures;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import APIUsageSequenceExtraction.APIUsageSequenceExtraction;
import DataObjects.Method;
import db_access_layer.DatabaseAccessLayer;
import _3_PopulateRelatedFeatures.ViewSampleMethodForClusters;;

/**
 * @author shamsa
 * 
 */
public class RetrieveRelatedFeatures {

	public static void main(String args[]) throws Exception
	{		
		int methodID = 49028;//48734;//20064;//58708;
		int clusterID = 559;//1576;//1584;//1129;//1987;//731;//617;//76;//58;//2249;//69;//484;//443
		int projectID = 68;//68;//32;//90;//85;//100;//18;//53;//90;
		
		//retrieveStrategy1(clusterID);
		//retrieveStrategy2(clusterID, projectID);
		retrieveFromCallGraph(methodID, projectID);
		
	}

	private static void retrieveFromCallGraph(int methodID, int projectID) throws Exception {
			
		DatabaseAccessLayer dbLayer = DatabaseAccessLayer.getInstance();		
		dbLayer.initializeConnectorToRetrieveRelatedFeatures();	
		
		ArrayList<Integer> cumulatingMethodIDs = new ArrayList<Integer>();
		LinkedHashMap<Integer, Integer> calledMethodsList = APIUsageSequenceExtraction.getCalledMethods2(methodID);
		ArrayList<Integer> calledMethodIDsList = new ArrayList<Integer>();
		// Get a set of the entries
	      Set set = calledMethodsList.entrySet();
	      
		// Get an iterator
	      Iterator i = set.iterator();
	      
	      // Display elements
	      while(i.hasNext()) {
	         Map.Entry me = (Map.Entry)i.next();
	         calledMethodIDsList.add((Integer) me.getKey());
	      }
		//comment below line to allow only immediate call methods
	    calledMethodIDsList = APIUsageSequenceExtraction.getDescendantMethodIDs(methodID, cumulatingMethodIDs);
		ArrayList<Integer> ancestorMethodsList = APIUsageSequenceExtraction.getHostMethods(methodID);
		calledMethodIDsList.addAll(ancestorMethodsList);
		//if descendant methods list is null then take teh methods in the same file
		if(calledMethodIDsList.size()==0)
		{
			//System.out.println("here");
			calledMethodIDsList = dbLayer.getSameFileMethods(methodID);
		}
		
		//get clusterIDs against the descendant methods
		ArrayList<Integer> clusterIDsList = new ArrayList<Integer>();
		for (int mID: calledMethodIDsList)
		{
			int cID = dbLayer.getClusterID(mID);
			if(cID!= -1)
			{
				ArrayList<Integer> featureIDs = dbLayer.getFeatureIDs(cID);
				if(featureIDs != null)
				{
					//add the clusterID to clusterIDsList
					if(!clusterIDsList.contains(cID))
						{
							clusterIDsList.add(cID);
						}
				}
			}
			
		}

		int recommendations = 0;
		for(int cID: clusterIDsList)
		{
			//display the related features/functions
			Integer[] array;		
			array = retrieveRelatedClusterIDs(cID);
			recommendations += array.length;
			//if(recommendations>=30)
			//{
				//recommendations -= array.length;
				//break;
			//}
			ViewSampleMethodForClusters.viewMethodsAgainstClusterIDs(array, projectID);
			
		}
		System.out.println("No. of results:"+ recommendations);
		
	}

	private static void retrieveStrategy1(int clusterID) throws SQLException,
			ClassNotFoundException, IOException {
		
		System.out.println("Starting!");		
		Integer[] array;		
		array = retrieveRelatedClusterIDs(clusterID);
		ViewSampleMethodForClusters.viewMethodsAgainstClusterIDs(array);

	}
	
	private static void retrieveStrategy2(int clusterID, int projectID) throws SQLException,
	ClassNotFoundException, IOException {
		
		System.out.println("Starting!");		
		Integer[] array;		
		array = retrieveRelatedClusterIDs(clusterID);
		ViewSampleMethodForClusters.viewMethodsAgainstClusterIDs(array, projectID);
	}
	

	

	public static Integer[] retrieveRelatedClusterIDs(
			int clusterID) throws SQLException, ClassNotFoundException {
		DatabaseAccessLayer dbLayer = DatabaseAccessLayer.getInstance();
		dbLayer.initializeConnectorToRetrieveRelatedFeatures();	
		//1. retrieve all featureIDs against a selected clusterID
		ArrayList<Integer> featureIDs = dbLayer.getFeatureIDs(clusterID);
		//2. for each feature, get the member clusterIDs except for user selected clusterID
		String[] clustersList;
		ArrayList<Integer> clusterIDsList = new ArrayList<Integer>();
		clusterIDsList.add(clusterID);//adding the input cluster first
		for(Integer fID: featureIDs)
		{
			clusterIDsList.addAll(dbLayer.getclusterIDs(clusterID, fID));
		}
		
		Integer[] array = new Integer[clusterIDsList.size()];
		array = clusterIDsList.toArray(array);
		//3. call the viewMethodsAgainstClusterIDs method to get related methods 
		//(these clusters contain the top representative method for now)
		dbLayer.closeConnector();	
		return array;
	}
	
}
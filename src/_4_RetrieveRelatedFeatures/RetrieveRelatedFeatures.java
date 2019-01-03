package _4_RetrieveRelatedFeatures;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import DataObjects.Method;
import db_access_layer.DatabaseAccessLayer;
import _3_PopulateRelatedFeatures.ViewSampleMethodForClusters;;

/**
 * @author shamsa
 * 
 */
public class RetrieveRelatedFeatures {

	public static void main(String args[]) throws ClassNotFoundException, SQLException, IOException
	{		
		System.out.println("Starting!");
		int clusterID = 484;//443
		Integer[] array = retrieveRelatedClusterIDs(clusterID);	
		ViewSampleMethodForClusters.viewMethodsAgainstClusterIDs(array);	
		
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
package automated_evaluation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import _3_PopulateRelatedFeatures.ViewSampleMethodForClusters;
import _4_RetrieveRelatedFeatures.RetrieveRelatedFeatures;
import APIUsageSequenceExtraction.APIUsageSequenceExtraction;
import DataObjects.Method;
import db_access_layer.DatabaseAccessLayer;
import db_access_layer.EvaluationDAL;

/**
 * @author shamsa
 * calculate the precision automatically
 *
 */
public class AutomatedEvaluation {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception{
				
		int inputClusterID = 731;
		int projectID = 12;
		int methodID = 3118;
		//evaluationStrategy1(inputClusterID);
		//evaluationStrategy2(inputClusterID, projectID);
		evaluationStrategy3(methodID, projectID);
		
	}

	private static void evaluationStrategy3(int methodID, int projectID) throws Exception, SQLException, ClassNotFoundException {
		//1.get distinct API usages from the API call table of the held out project
		
		ArrayList<String> HeldOutAPIUsages = new ArrayList<String>();
		EvaluationDAL dbLayer = EvaluationDAL.getInstance();
		dbLayer.initializeHeldoutDatabaseConnector();
		HeldOutAPIUsages = dbLayer.getAPIUsages();	
		
		//2.get the distinct API names from from the related methods recommended.  
		ArrayList<String> RecommendedAPIUsages = new ArrayList<String>();
		dbLayer.initializeFoldDatabaseConnector();
		
		DatabaseAccessLayer dbLayer2 = DatabaseAccessLayer.getInstance();		
		dbLayer2.initializeConnectorToRetrieveRelatedFeatures();	
		
		LinkedHashMap<Integer, Integer> calledMethodsList = APIUsageSequenceExtraction.getCalledMethods2(methodID);
		ArrayList<Integer> calledMethodIDsList = new ArrayList<Integer>();
		// Get a set of the entries
	      Set set = calledMethodsList.entrySet();
	      
		// Get an iterator
	      Iterator it = set.iterator();
	      
	      // Display elements
	      while(it.hasNext()) {
	         Map.Entry me = (Map.Entry)it.next();
	         calledMethodIDsList.add((Integer) me.getKey());
	      }
		ArrayList<Integer> cumulatingMethodIDs = new ArrayList<Integer>();
		//ArrayList<Integer> descendantMethodsList = APIUsageSequenceExtraction.getDescendantMethodIDs(methodID, cumulatingMethodIDs);
	    calledMethodIDsList = APIUsageSequenceExtraction.getDescendantMethodIDs(methodID, cumulatingMethodIDs);
		ArrayList<Integer> ancestorMethodsList = APIUsageSequenceExtraction.getHostMethods(methodID);
		calledMethodIDsList.addAll(ancestorMethodsList);
		//if descendant methods list is null then take teh methods in the same file
		if(calledMethodIDsList.size()==0)
		{
			//System.out.println("here");
			calledMethodIDsList = dbLayer2.getSameFileMethods(methodID);
		}
		
		//get clusterIDs against the descendant methods
		ArrayList<Integer> clusterIDsList = new ArrayList<Integer>();
		for (int mID: calledMethodIDsList)
		{
			int cID = dbLayer2.getClusterID(mID);
			if(cID!= -1)
			{
				ArrayList<Integer> featureIDs = dbLayer2.getFeatureIDs(cID);
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

		Integer[] array = null;
		ArrayList<Integer> merged = new ArrayList<Integer>();
		for(int cID: clusterIDsList)
		{
			//index+=1;
			//display the related features/functions
					
			array = (RetrieveRelatedFeatures.retrieveRelatedClusterIDs(cID));
			for(int x: array)
			{
				merged.add(x);
			}
			//ViewSampleMethodForClusters.viewMethodsAgainstClusterIDs(array, projectID);
		}
		
		Integer[] ret = new Integer[merged.size()];
	    for (int i=0; i < ret.length; i++)
	    { 
	        ret[i] = merged.get(i);
	    } 
		RecommendedAPIUsages = viewMethodsAgainstClusterIDsFromSameProject(ret, projectID);	
		
		//compare the API usages in hold out project with the API usages recommended
		int matchingAPICalls = 0;
		int totalAPICallsRecommended = RecommendedAPIUsages.size();
		//HeldOutAPIUsages.contains(RecommendedAPIUsages.get(0));
		for(String usage: RecommendedAPIUsages)
		{
			if(HeldOutAPIUsages.contains(usage))
			{
				matchingAPICalls += 1;
			}
				
			
		}
		
		//calculate precision		
		float precision = (float)matchingAPICalls/totalAPICallsRecommended;
		System.out.println(precision);
		
	}

	private static void evaluationStrategy1(int inputClusterID)
			throws Exception, SQLException, ClassNotFoundException {
		//1.get distinct API usages from the API call table of the held out project
		
		ArrayList<String> HeldOutAPIUsages = new ArrayList<String>();
		EvaluationDAL dbLayer = EvaluationDAL.getInstance();
		dbLayer.initializeHeldoutDatabaseConnector();
		HeldOutAPIUsages = dbLayer.getAPIUsages();	
		
		//2.get the distinct API names from from the related methods recommended.  
		ArrayList<String> RecommendedAPIUsages = new ArrayList<String>();
		dbLayer.initializeFoldDatabaseConnector();
		Integer[] array = RetrieveRelatedFeatures.retrieveRelatedClusterIDs(inputClusterID);
		
		RecommendedAPIUsages = viewMethodsAgainstClusterIDs(array);	
		
		//compare the API usages in hold out project with the API usages recommended
		int matchingAPICalls = 0;
		int totalAPICallsRecommended = RecommendedAPIUsages.size();
		//HeldOutAPIUsages.contains(RecommendedAPIUsages.get(0));
		for(String usage: RecommendedAPIUsages)
		{
			if(HeldOutAPIUsages.contains(usage))
			{
				matchingAPICalls += 1;
			}
				
			
		}
		
		//calculate precision		
		float precision = (float)matchingAPICalls/totalAPICallsRecommended;
		System.out.println(precision);
	}
	
	private static void evaluationStrategy2(int inputClusterID, int projectID)
			throws Exception, SQLException, ClassNotFoundException {
		//1.get distinct API usages from the API call table of the held out project
		
		ArrayList<String> HeldOutAPIUsages = new ArrayList<String>();
		EvaluationDAL dbLayer = EvaluationDAL.getInstance();
		dbLayer.initializeHeldoutDatabaseConnector();
		HeldOutAPIUsages = dbLayer.getAPIUsages();	
		
		//2.get the distinct API names from from the related methods recommended.  
		ArrayList<String> RecommendedAPIUsages = new ArrayList<String>();
		dbLayer.initializeFoldDatabaseConnector();
		Integer[] array = RetrieveRelatedFeatures.retrieveRelatedClusterIDs(inputClusterID);
		
		RecommendedAPIUsages = viewMethodsAgainstClusterIDsFromSameProject(array, projectID);	
		
		//compare the API usages in hold out project with the API usages recommended
		int matchingAPICalls = 0;
		int totalAPICallsRecommended = RecommendedAPIUsages.size();
		//HeldOutAPIUsages.contains(RecommendedAPIUsages.get(0));
		for(String usage: RecommendedAPIUsages)
		{
			if(HeldOutAPIUsages.contains(usage))
			{
				matchingAPICalls += 1;
			}
				
			
		}
		
		//calculate precision		
		float precision = (float)matchingAPICalls/totalAPICallsRecommended;
		System.out.println(precision);
	}
	
	private static ArrayList<String> viewMethodsAgainstClusterIDsFromSameProject(
			Integer[] clustersList, int projectID) throws Exception {
				
				ArrayList<String> RecommendedAPIUsages = new ArrayList<String>();
				
				EvaluationDAL dbLayer = EvaluationDAL.getInstance();
				dbLayer.initializeFoldDatabaseConnector();	
				
				//get the first method  in each cluster along with projectID and fileName 
				ArrayList<DataObjects.Method> methodsList = new ArrayList<DataObjects.Method>();
				for(Integer clusterID: clustersList)
				{
					Method method = dbLayer.getMethodFromProject(clusterID, projectID);
					if(method.id == 0)
						method = dbLayer.getFirstMethod(clusterID);
					methodsList.add(method);
					
				}
				
				//
				for(DataObjects.Method m: methodsList)
				{			
					RecommendedAPIUsages.addAll(dbLayer.getMethodAPICalls(m));			
					
				}
				dbLayer.closeConnector();
				return RecommendedAPIUsages;
	}

	public static ArrayList<String> viewMethodsAgainstClusterIDs(Integer[] clustersList)
			throws Exception {
		
		ArrayList<String> RecommendedAPIUsages = new ArrayList<String>();
		
		EvaluationDAL dbLayer = EvaluationDAL.getInstance();
		dbLayer.initializeFoldDatabaseConnector();	
		
		//get the first method  in each cluster along with projectID and fileName 
		ArrayList<DataObjects.Method> methodsList = new ArrayList<DataObjects.Method>();
		for(Integer clusterID: clustersList)
		{
			Method method = dbLayer.getFirstMethod(clusterID);
			methodsList.add(method);
			
		}
		
		//
		for(DataObjects.Method m: methodsList)
		{			
			RecommendedAPIUsages.addAll(dbLayer.getMethodAPICalls(m));			
			
		}
		dbLayer.closeConnector();
		return RecommendedAPIUsages;
	}
}

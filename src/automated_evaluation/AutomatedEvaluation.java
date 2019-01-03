package automated_evaluation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import _4_RetrieveRelatedFeatures.RetrieveRelatedFeatures;
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
				
		//1.get distinct API usages from the API call table of the held out project
		
		ArrayList<String> HeldOutAPIUsages = new ArrayList<String>();
		EvaluationDAL dbLayer = EvaluationDAL.getInstance();
		dbLayer.initializeHeldoutDatabaseConnector();
		HeldOutAPIUsages = dbLayer.getAPIUsages();	
		
		//2.get the distinct API names from from the related methods recommended.  
		ArrayList<String> RecommendedAPIUsages = new ArrayList<String>();
		dbLayer.initializeFoldDatabaseConnector();
		Integer[] array = RetrieveRelatedFeatures.retrieveRelatedClusterIDs(259);
		
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
